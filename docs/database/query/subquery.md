# Subquery


Subqueries in Molecule allow you to query an outer table with per-row or globally aggregated or filtered data from a sub table.

> Examples here use the simple `sync` API for brevity - see [4 APIs](/database/query/attributes.html#_4-apis) for `async`, `zio`, `io` APIs
> 
## Per-row Aggregate

Aggregate one or more attributes from a sub-table per outer row.

Count cities per country:
```scala
Country.name.join( // outer query
  City             // subquery
    .id(count).d1  // aggregate count of cities per row, sort descending
    .country_(Country.id_) // correlate fk country with Country.id
).query.limit(3).get ==> List(
  ("China", 363),
  ("India", 341),
  ("United States", 274),
)
```

Or aggregate multiple attributes: get city count and max city population per country:
```scala
Country.name.join(
  City.id(count).d1.population(max).country_(Country.id_)
).query.limit(3).get ==> List(
  ("China", (363, 9696300)),
  ("India", (341, 10500000)),
  ("United States", (274, 8008278))
)
```
<details>
<summary>SQL (H2)</summary>

```sql
SELECT DISTINCT
  Country.name,
  subquery1.City_id_count,
  subquery1.City_population_max
FROM Country INNER JOIN (
    SELECT
      COUNT(City.id) AS City_id_count,
      MAX(City.population) AS City_population_max,
      City.country
    FROM City
    GROUP BY City.country
    ORDER BY City_id_count DESC NULLS LAST
  ) subquery1 ON Country.id = subquery1.country
WHERE
  Country.name IS NOT NULL
ORDER BY subquery1.City_id_count DESC
LIMIT 3;
```
</details>

### Correlation

In the examples above, we "connect" - or correlate - the subquery with the outer query by applying the outer `Country.id_` to the foreign key value `City.country_` of the subquery. 

We could also correlate any other matching sub/outer attribute pair:

```scala
Country.name.join(
  City.id(count).d1
    // correlate City.countryCode with Country.code
    .countryCode_(Country.code_) 
).query.limit(3).get ==> List(
  ("China", 363),
  ("India", 341),
  ("United States", 274),
)
```



## Per-row limit & offset

Use `limit(n)` to get the first N rows of a subquery.


```scala
// 3 most populous countries each with their 2 biggest cities:
Country.name.population.d1.join(
  City.name.population.d1.country_(Country.id_).query.limit(2)
).query.limit(6).get ==> List(
  ("China", 1277558000, ("Shanghai", 9696300)),
  ("China", 1277558000, ("Peking", 7472000)),
  ("India", 1013662000, ("Mumbai (Bombay)", 10500000)),
  ("India", 1013662000, ("Delhi", 7206704)),
  ("United States", 278357000, ("New York", 8008278)),
  ("United States", 278357000, ("Los Angeles", 3694820))
)
```

Setting limit to 1 can conveniently get the first/last row of a subquery depending on sorting.

```scala
// 3 most populous countries each with their biggest city:
Country.name.population.d1.join(
  City.name.population.d1 // descending
    .country_(Country.id_).query.limit(1)
).query.limit(3).get ==> List(
  ("China", 1277558000, ("Shanghai", 9696300)),
  ("India", 1013662000, ("Mumbai (Bombay)", 10500000)),
  ("United States", 278357000, ("New York", 8008278)),
)

// 3 most populous countries each with their smallest city:
Country.name.population.d1.join(
  City.name.population.a1 // ascending
    .country_(Country.id_).query.limit(1)
).query.limit(3).get ==> List(
  ("China", 1277558000, ("Huangyan", 89288)),
  ("India", 1013662000, ("Vejalpur", 89053)),
  ("United States", 278357000, ("Charleston", 89063))
)
```

Use `offset(n)` to skip the first N rows of a subquery.

```scala
// 3 most populous countries each with their second biggest city:
Country.name.population.d1.join(
  City.name.population.d1.country_(Country.id_).query.limit(1).offset(1)
).query.limit(3).get ==> List(
  ("China", 1277558000, ("Peking", 7472000)),
  ("India", 1013662000, ("Delhi", 7206704)),
  ("United States", 278357000, ("Los Angeles", 3694820))
)
```
<details>
<summary>SQL (H2)</summary>

```sql
SELECT DISTINCT
  Country.name,
  Country.population,
  subquery1.name,
  subquery1.population
FROM Country INNER JOIN (
    SELECT DISTINCT
      name, population, country
    FROM (
      SELECT DISTINCT
        inner_query.*,
        ROW_NUMBER() OVER (
          PARTITION BY inner_query.country 
          ORDER BY inner_query.population DESC NULLS LAST
        ) as rn
      FROM (
        SELECT DISTINCT
          City.name,
          City.population,
          City.country
        FROM City
        WHERE
          City.name       IS NOT NULL AND
          City.population IS NOT NULL
        ORDER BY City.population DESC NULLS LAST
      ) inner_query
    ) filtered_query
    WHERE rn > 1 AND rn <= 2
  ) subquery1 ON Country.id = subquery1.country
WHERE
  Country.name       IS NOT NULL AND
  Country.population IS NOT NULL
ORDER BY Country.population DESC NULLS LAST, subquery1.population DESC
LIMIT 3;
```
</details>
<br>


## Global aggregate comparison

If we don't correlate the subquery with the outer query, we can aggregate the entire sub-table.

We can for instance find employees with a salary above the average salary:

```scala
Employee.name.salary.insert(
  ("Bob", 50000),
  ("Eva", 60000),
  ("Liz", 70000),
).transact

Employee.name.salary.>(Employee.salary_(avg)).query.get ==> List(
  ("Liz", 70000),
)
```
<details>
<summary>SQL (H2)</summary>

```sql
SELECT DISTINCT
  Employee.name,
  Employee.salary,
  subquery1.col
FROM Employee
  CROSS JOIN (
    SELECT DISTINCT
      AVG(Employee.salary) AS col
    FROM Employee
  ) subquery1
WHERE
  Employee.name   IS NOT NULL AND
  Employee.salary > subquery1.col;
```
</details>

#### Available Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `attr(aggregate)` | Equals | `Entity.i(Ref.i(count))` |
| `attr.not(aggregate)` | Not equals | `Entity.i.not(Ref.i(sum))` |
| `attr.<(aggregate)` | Less than | `Entity.i.<(Ref.i(max))` |
| `attr.<=(aggregate)` | Less than or equal | `Entity.i.<=(Ref.i(avg))` |
| `attr.>(aggregate)` | Greater than | `Entity.i.>(Ref.i(min))` |
| `attr.>=(aggregate)` | Greater than or equal | `Entity.i.>=(Ref.i(count))` |




## Global aggregate addition

If you just want to add a global aggregated value to each row you can use `.join` without correlation:
```scala
Employee.name.salary.join(Employee.salary(avg)).query.i.get ==> List(
  ("Bob", 50000, 60000),
  ("Eva", 60000, 60000),
  ("Liz", 70000, 60000),
)
```
<details>
<summary>SQL (H2)</summary>

```sql
SELECT DISTINCT
  Employee.name,
  Employee.salary,
  subquery1.Employee_salary_avg
FROM Employee CROSS JOIN (
    SELECT DISTINCT
      AVG(Employee.salary) AS Employee_salary_avg
    FROM Employee
  ) subquery1
WHERE
  Employee.name   IS NOT NULL AND
  Employee.salary IS NOT NULL;
```
</details>
<br>

## Filtering

You can filter on both the outer and inner tables.
```scala
// Republics with more than 8 unofficial languages:
Country.name.governmentForm_("Republic").join(
  CountryLanguage
    .language(count).>(8).d1
    .isOfficial_(false)
    .country_(Country.id_)
).query.i.get ==> List(
  ("Congo, The Democratic Republic of the", 10),
  ("Kenya", 10),
  ("Mozambique", 10),
  ("Tanzania", 10),
  ("Uganda", 10),
  ("Angola", 9),
  ("Philippines", 9),
)
```
<details>
<summary>SQL (H2)</summary>

```sql
SELECT DISTINCT
  Country.name,
  subquery1.CountryLanguage_language_count
FROM Country INNER JOIN (
    SELECT
      COUNT(CountryLanguage.language) AS CountryLanguage_language_count,
      CountryLanguage.country
    FROM CountryLanguage
    WHERE
      CountryLanguage.isOfficial = false
    GROUP BY CountryLanguage.country
    HAVING COUNT(CountryLanguage.language) > 8
    ORDER BY CountryLanguage_language_count DESC NULLS LAST
  ) subquery1 ON Country.id = subquery1.country
WHERE
  Country.name           IS NOT NULL AND
  Country.governmentForm = 'Republic'
ORDER BY subquery1.CountryLanguage_language_count DESC;
```
</details>

