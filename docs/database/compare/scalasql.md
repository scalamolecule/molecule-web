---
prev: /database/compare/sql-raw
---

# ScalaSql

To compare Molecule with ScalaSql we implement the [ScalaSql Tutorial](https://github.com/com-lihaoyi/scalasql/blob/main/docs/tutorial.md) to see the differences.

## ScalaSql setup

In ScalaSql you need to both define the SQL Schema and a Scala Model that matches the SQL Schema:

#### ScalaSql db schema

```sql
CREATE TABLE IF NOT EXISTS city
(
    id          integer AUTO_INCREMENT PRIMARY KEY,
    name        varchar      NOT NULL,
    countrycode character(3) NOT NULL,
    district    varchar      NOT NULL,
    population  integer      NOT NULL
);

CREATE TABLE IF NOT EXISTS country
(
    code           character(3) PRIMARY KEY,
    name           varchar      NOT NULL,
    continent      varchar      NOT NULL,
    region         varchar      NOT NULL,
    surfacearea    real         NOT NULL,
    indepyear      smallint,
    population     integer      NOT NULL,
    lifeexpectancy real,
    gnp            numeric(10, 2),
    gnpold         numeric(10, 2),
    localname      varchar      NOT NULL,
    governmentform varchar      NOT NULL,
    headofstate    varchar,
    capital        integer,
    code2          character(2) NOT NULL
);

CREATE TABLE IF NOT EXISTS countrylanguage
(
    countrycode character(3) NOT NULL,
    language    varchar      NOT NULL,
    isofficial  boolean      NOT NULL,
    percentage  real         NOT NULL
);
```

#### ScalaSql Schema Model

```scala
case class Country[T[_]](
  code: T[String],
  name: T[String],
  continent: T[String],
  region: T[String],
  surfaceArea: T[Int],
  indepYear: T[Option[Int]],
  population: T[Long],
  lifeExpectancy: T[Option[Double]],
  gnp: T[Option[scala.math.BigDecimal]],
  gnpOld: T[Option[scala.math.BigDecimal]],
  localName: T[String],
  governmentForm: T[String],
  headOfState: T[Option[String]],
  capital: T[Option[Int]],
  code2: T[String]
)

object Country extends Table[Country]()

case class City[T[_]](
  id: T[Int],
  name: T[String],
  countryCode: T[String],
  district: T[String],
  population: T[Long]
)

object City extends Table[City]()

case class CountryLanguage[T[_]](
  countryCode: T[String],
  language: T[String],
  isOfficial: T[Boolean],
  percentage: T[Double]
)

object CountryLanguage extends Table[CountryLanguage]()   
```

## Molecule setup

In Molecule you simply define the structure of your domain with Entities and their Attributes.

- No type parameters
- Single trait for each domain entity (instead of both case class and object)

```scala
object World extends DomainStructure {

  trait Country {
    val code           = oneString
    val name           = oneString
    val continent      = oneString
    val region         = oneString
    val surfaceArea    = oneInt
    val indepYear      = oneInt
    val population     = oneLong
    val lifeExpectancy = oneDouble
    val gnp            = oneBigDecimal
    val gnpOld         = oneBigDecimal
    val localName      = oneString
    val governmentForm = oneString
    val headOfState    = oneString
    val capital        = one[City]
    val code2          = oneString
  }

  trait City {
    val name        = oneString
    val countryCode = oneString
    val district    = oneString
    val population  = oneLong
    val country     = one[Country]
  }

  trait CountryLanguage {
    val countryCode = oneString
    val language    = oneString
    val isOfficial  = oneBoolean
    val percentage  = oneDouble
    val country     = one[Country]
  }
}
```

From this model, Molecule generates

- An SQL schema that we can transact to create the database
- Boilerplate code to write molecule queries and transactions

An important difference is also that with Molecule you don't need to decide upfront wether an attribute can be optional. You can enforce mandatory values though by adding `mandatory` after the type definition. If for instance we wanted to enforce that a code of a Country is mandatory, we could define `val code = oneString.mandatory`. For all definition options, see [Domain Structure](/database/setup/domain-structure#attribute-options).


## Select

ScalaSql maps data to a Model type like `City`, equivalent of `select * from City`:

```scala
// ScalaSql
val query = City.select
db.run(query).take(3) ==> Seq(
  City[Sc](1, "Kabul", "AFG", district = "Kabol", population = 1780000),
  City[Sc](2, "Qandahar", "AFG", district = "Qandahar", population = 237500),
  City[Sc](3, "Herat", "AFG", district = "Herat", population = 186800)
)
```

This means that you will often over-fetch data with ScalaSql if not all attribute values of a Table are needed.

In Molecule you instead choose exactly which attributes you need and what order you want them in:

```scala
// Molecule
City.id.name.countryCode.district.population.query.get.take(3) ==> Seq(
  (1, "Kabul", "AFG", "Kabol", 1780000),
  (2, "Qandahar", "AFG", "Qandahar", 237500),
  (3, "Herat", "AFG", "Herat", 186800)
)
// or
City.countryCode.name.population.query.get.take(3) ==> Seq(
  ("ABW", "Oranjestad", 29034),
  ("AFG", "Herat", 186800),
  ("AFG", "Kabul", 1780000),
)
// etc..
```

## Filter

#### Single filter

```scala
val query = City.select.filter(_.name === "Singapore").single
db.run(query) ==>
  City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733)
```

Molecule applies a filter directly to the attribute.

```scala
City.id.name("Singapore").countryCode.district.population
  .query.get.head ==>
  (3208, "Singapore", "SGP", "", 4017733)
```

#### By ID

```scala
val query = City.select.filter(_.id === 3208).single
db.run(query) ==>
  City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733)
```

```scala
City.id(3208).name.countryCode.district.population
  .query.get.head ==>
  (3208, "Singapore", "SGP", "", 4017733)
```

#### Multiple filters

```scala
val query = City.select.filter(c =>
  c.population > 5000000 && c.countryCode === "CHN"
)
// or 
val query = City.select
  .filter(_.population > 5000000)
  .filter(_.countryCode === "CHN")

db.run(query).take(2) ==> Seq(
  City[Sc](1890, "Shanghai", "CHN", district = "Shanghai", population = 9696300),
  City[Sc](1891, "Peking", "CHN", district = "Peking", population = 7472000)
)
```

In Molecule you neither need references to a Table but instead apply filters directly to the Attributes:

```scala
City.id.name.countryCode("CHN").district.population.>(5000000)
  .query.get.take(2) ==> Seq(
  (1890, "Shanghai", "CHN", "Shanghai", 9696300),
  (1891, "Peking", "CHN", "Peking", 7472000)
)
```

## Lifting

```scala
def find(cityId: Int) = 
  db.run(City.select.filter(_.id === cityId))

find(3208) ==> Seq(City[Sc](3208, "Singapore", "SGP", "", 4017733))
find(3209) ==> Seq(City[Sc](3209, "Bratislava", "SVK", "Bratislava", 448292))
```

```scala
def find(cityId: Int) =
  City.id(cityId).name.countryCode.district.population.query.get

find(3208) ==> Seq((3208, "Singapore", "SGP", "", 4017733))
find(3209) ==> Seq((3209, "Bratislava", "SVK", "Bratislava", 448292))
```

#### OR logic

```scala
val query = City.select
  .filter(c =>
    db.values(Seq("Singapore", "Kuala Lumpur", "Jakarta")).contains(c.name)
  )
  .map(_.countryCode)

db.run(query) ==> Seq("IDN", "MYS", "SGP")
```

```scala
City.name_("Singapore", "Kuala Lumpur", "Jakarta").countryCode
  .query.get ==> Seq("IDN", "MYS", "SGP")
```

## Mapping

#### Tuple2

```scala
val query = Country.select.map(c => (c.name, c.continent))
db.run(query).take(5) ==> Seq(
  ("Afghanistan", "Asia"),
  ("Netherlands", "Europe"),
  ("Netherlands Antilles", "North America"),
  ("Albania", "Europe"),
  ("Algeria", "Africa")
)
```

```scala
Country.name.continent.query.get.take(5) ==> Seq(
  ("Afghanistan", "Asia"),
  ("Albania", "Europe"),
  ("Algeria", "Africa"),
  ("American Samoa", "Oceania"),
  ("Andorra", "Europe"),
)
```

(Without sorting, results differ)

#### Heterogeneous tuple

```scala
val query = City.select
  .filter(_.name === "Singapore")
  .map(c => (c, c.name.toUpperCase, c.population / 1000000))
  .single

db.run(query) ==>
  (
    City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733),
    "SINGAPORE",
    4 // population in millions
  )
```

```scala
City.id.name("Singapore").countryCode.district.population.query.get.map {
  case c@(_, name, _, _, population) =>
    (c, name.toUpperCase, population / 1000000)
}.head ==>
  (
    (3208, "Singapore", "SGP", "", 4017733),
    "SINGAPORE",
    4 // population in millions
  )
```

## Aggregate

#### sum

```scala
val query = City.select.filter(_.countryCode === "CHN").map(_.population).sum
db.run(query) ==> 175953614
```

```scala
City.countryCode_("CHN").population(sum).query.get.head ==> 175953614
```

#### sumBy

```scala
val query = City.select.sumBy(_.population)
db.run(query) ==> 1429559884
```

```scala
City.population(sum).query.get.head ==> 1429559884
```

#### size

```scala
val query = Country.select.filter(_.population > 1000000).size
db.run(query) ==> 154
```

```scala
Country.id(count).population_.>(1000000).query.get.head ==> 154
```

#### aggregates

```scala
val query = Country.select
  .aggregate(cs => (
    cs.minBy(_.population),
    cs.avgBy(_.population),
    cs.maxBy(_.population)
  ))
db.run(query) ==> (0, 25434098, 1277558000)
```

```scala
Country
  .population(min)
  .population(avg)
  .population(max)
  .query.get.head ==> (0, 25434098.11715481, 1277558000)
```

## Sort/Drop/Take

```scala
val query = City.select
  .sortBy(_.population)
  .desc
  .drop(5)
  .take(5)
  .map(c => (c.name, c.population))

db.run(query) ==> Seq(
  ("Karachi", 9269265),
  ("Istanbul", 8787958),
  ("Ciudad de México", 8591309),
  ("Moscow", 8389200),
  ("New York", 8008278)
)
```

```scala
City.name.population.d1 // sort descending
  .query.offset(5).limit(5).get._1 ==> Seq(
  ("Karachi", 9269265),
  ("Istanbul", 8787958),
  ("Ciudad de México", 8591309),
  ("Moscow", 8389200),
  ("New York", 8008278)
)
```

## Casting

```scala
val query = Country.select
  .filter(_.name === "Singapore")
  .map(_.lifeExpectancy.cast[Int])
  .single

db.run(query) ==> 80
```

```scala
Country.name_("Singapore").lifeExpectancy
  .query.get.head.toInt ==> 80
```

## Nullable

```scala
val query = Country.select
  .filter(_.capital.isEmpty)
  .size

db.run(query) ==> 7
```

```scala
Country.capital_().id(count).query.get.head ==> 7
```

## Optional

```scala
val query2 = Country.select
  .filter(_.capital === Option.empty[Int])
  .size

db.run(query2) ==> 7

// this doesn't seem useful
val query = Country.select
  .filter(_.capital `=` Option.empty[Int])
  .size

db.run(query) ==> 0
```

```scala
Country.capital_?(None).id(count).query.get.head ==> (None, 7)
```

## Joins

#### inner

```scala
val query = City.select
  .join(Country)(_.countryCode === _.code)
  .filter { case (city, country) => country.name === "Liechtenstein" }
  .map { case (city, country) => city.name }

db.run(query) ==> Seq("Schaan", "Vaduz")

// or

val query = for {
  city <- City.select
  country <- Country.join(city.countryCode === _.code)
  if country.name === "Liechtenstein"
} yield city.name

db.run(query) ==> Seq("Schaan", "Vaduz")
```

```scala
City.name.Country.name_("Liechtenstein")
  .query.get ==> Seq("Schaan", "Vaduz")
```

#### right

```scala
val query = City.select
  .rightJoin(Country)(_.countryCode === _.code)
  .filter { case (cityOpt, country) => cityOpt.isEmpty(_.id) }
  .map { case (cityOpt, country) => (cityOpt.map(_.name), country.name) }

db.run(query) ==> Seq(
  (None, "Antarctica"),
  (None, "Bouvet Island"),
  (None, "British Indian Ocean Territory"),
  (None, "South Georgia and the South Sandwich Islands"),
  (None, "Heard Island and McDonald Islands"),
  (None, "French Southern territories"),
  (None, "United States Minor Outlying Islands")
)
```

Define an optional entity with an empty relationship value `City.country_()` to get the Countries that don't have a city entity pointing to them: 
```scala
City.?(City.country_()).Country.name
.query.get ==> Seq(
  (None, "Antarctica"),
  (None, "Bouvet Island"),
  (None, "British Indian Ocean Territory"),
  (None, "French Southern territories"),
  (None, "Heard Island and McDonald Islands"),
  (None, "South Georgia and the South Sandwich Islands"),
  (None, "United States Minor Outlying Islands")
)
```

#### left

(No ScalaSql example)

Left join with Molecule where some Countries don't have a Capital:

```scala
Country.name.startsWith("Un").Capital.?(City.name).query.get ==> Seq(
  ("United Arab Emirates", Some("Abu Dhabi")),
  ("United Kingdom", Some("London")),
  ("United States", Some("Washington")),
  ("United States Minor Outlying Islands", None), // no relationship
)
```

## Subqueries

```scala
val query = CountryLanguage.select
  .join(Country.select.sortBy(_.population).desc.take(2))(_.countryCode === _.code)
  .map { case (language, country) => (language.language, country.name) }
  .sortBy(_._1)

db.run(query).take(5) ==> Seq(
  ("Asami", "India"),
  ("Bengali", "India"),
  ("Chinese", "China"),
  ("Dong", "China"),
  ("Gujarati", "India")
)
```

Molecule doesn't support explicit subqueries although some molecules translate to subqueries internally. In this case, two queries can be used to retrieve the same result:

```scala
// Fetch ids of 2 most populated countries
val top2 = Country.id.population.d1.query.limit(2).get.map(_._1)

// Fetch first 5 languages of those countries
CountryLanguage.language.a1.Country.id_(top2).name.query.limit(5).get ==> Seq(
  ("Asami", "India"),
  ("Bengali", "India"),
  ("Chinese", "China"),
  ("Dong", "China"),
  ("Gujarati", "India"),
)
```

... or make a subquery with a raw SQL query:

```scala
rawQuery(
  """SELECT countrylanguage1.language AS res_0, subquery0.name AS res_1
    |FROM (SELECT
    |    country0.code AS code,
    |    country0.name AS name,
    |    country0.population AS population
    |  FROM country country0
    |  ORDER BY population DESC
    |  LIMIT 2) subquery0
    |JOIN countrylanguage countrylanguage1
    |ON (subquery0.code = countrylanguage1.countrycode)
    |ORDER BY res_0
    |LIMIT 5
    |""".stripMargin
) ==> Seq(
  Seq("Asami", "India"),
  Seq("Bengali", "India"),
  Seq("Chinese", "China"),
  Seq("Dong", "China"),
  Seq("Gujarati", "India"),
)
```

## Union/Except/Intersect

```scala
val largestCountries =
  Country.select.sortBy(_.name).sortBy(_.population).desc.take(2).map(_.name)

val smallestCountries =
  Country.select.sortBy(_.name).sortBy(_.population).asc.take(2).map(_.name)

val query = smallestCountries.union(largestCountries)
db.run(query) ==> Seq("Antarctica", "Bouvet Island", "China", "India")
```

Molecule doesn't support union/except/intercept semantics. Instead, two queries can be used:

```scala
// (need to sort by Country name too since several countries have 0 inhabitants)
Country.name.a2.population.a1.query.limit(2).get.map(_._1) ==>
  Seq("Antarctica", "Bouvet Island")

Country.name.population.d1.query.limit(2).get.map(_._1) ==>
  Seq("China", "India")
```

... or a raw query:

```scala
rawQuery(
  """SELECT subquery0.res AS res
    |FROM (SELECT country0.name AS res
    |  FROM country country0
    |  ORDER BY country0.population ASC, res
    |  LIMIT 2) subquery0
    |UNION
    |SELECT subquery0.res AS res
    |FROM (SELECT country0.name AS res
    |  FROM country country0
    |  ORDER BY country0.population DESC, res
    |  LIMIT 2) subquery0
    |LIMIT 5
    |""".stripMargin
).flatten ==> Seq("Antarctica", "Bouvet Island", "China", "India")
```

## Window functions

Molecule doesn't support window functions. A raw query can be used instead as shown above.

## Realistic queries

It could be argued that the complexity of the ScalaSql query constructions for the examples in this section in the tutorial start to be as complex as writing the SQL directly. So you might as well simply write a raw SQL query from the beginning without having to learn the advanced query constructs of ScalaSql. For instance it seems that you don't gain much from understanding/using this:

```scala
City.select
  .map(c => (c, db.rank().over.partitionBy(c.countryCode).sortBy(c.population).desc))
  .subquery
  .filter { case (city, r) => r <= 3 }
  .map { case (city, r) => (city.name, city.population, city.countryCode, r) }
  .join(Country)(_._3 === _.code)
  .sortBy(_._5.population)
  .desc
  .map { case (name, population, countryCode, r, country) =>
    (name, population, countryCode, r)
  }
```

compared to this:

```sql
SELECT subquery0.res_0_name        AS res_0,
       subquery0.res_0_population  AS res_1,
       subquery0.res_0_countrycode AS res_2,
       subquery0.res_1             AS res_3
FROM (SELECT city0.name        AS res_0_name,
             city0.countrycode AS res_0_countrycode,
             city0.population  AS res_0_population,
             RANK()               OVER (PARTITION BY city0.countrycode ORDER BY city0.population DESC) AS res_1
      FROM city city0) subquery0
         JOIN country country1 ON (subquery0.res_0_countrycode = country1.code)
WHERE (subquery0.res_1 <= 1)
ORDER BY country1.population DESC
```

## Inserts

#### Single row

```scala
val query = City.insert.columns( // ID provided by database AUTO_INCREMENT
  _.name := "Sentosa",
  _.countryCode := "SGP",
  _.district := "South",
  _.population := 1337
)
db.run(query)
db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq(
  City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733),
  City[Sc](4080, "Sentosa", "SGP", district = "South", population = 1337)
)
```

Insert a single row by calling `save` on a populated molecule:

```scala
City
  .name("Sentosa")
  .countryCode("SGP")
  .district("South")
  .population(1337).save.transact

City.name.countryCode_("SGP").district.population.d1.query.get ==> Seq(
  ("Singapore", "", 4017733),
  ("Sentosa", "South", 1337),
)
```

#### Batch

```scala
val query = City.insert.batched(_.name, _.countryCode, _.district, _.population)(
  ("Sentosa", "SGP", "South", 1337), // ID provided by database AUTO_INCREMENT
  ("Loyang", "SGP", "East", 31337),
  ("Jurong", "SGP", "West", 313373)
)

db.run(query)

db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq(
  City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733),
  City[Sc](4080, "Sentosa", "SGP", district = "South", population = 1337),
  City[Sc](4081, "Loyang", "SGP", district = "East", population = 31337),
  City[Sc](4082, "Jurong", "SGP", district = "West", population = 313373)
)
```

Insert multiple rows to an `insert` molecule:

```scala
City.name.countryCode.district.population.insert(
  ("Sentosa", "SGP", "South", 1337), // ID provided by database AUTO_INCREMENT
  ("Loyang", "SGP", "East", 31337),
  ("Jurong", "SGP", "West", 313373)
).transact

City.id.name.countryCode_("SGP").district.population.query.get ==> Seq(
  (3208, "Singapore", "", 4017733),
  (4080, "Sentosa", "South", 1337),
  (4081, "Loyang", "East", 31337),
  (4082, "Jurong", "West", 313373),
)
```

#### Arbitrary data

```scala
val query = City.insert.select(
  c => (c.name, c.countryCode, c.district, c.population),
  City.select
    .filter(_.name === "Singapore")
    .map(c => (Expr("New-") + c.name, c.countryCode, c.district, Expr(0L)))
)

db.run(query)

db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq(
  City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733),
  City[Sc](4080, "New-Singapore", "SGP", district = "", population = 0)
)
```

With Molecule we would insert a list of tuples from a query result with the desired modifications:

```scala
City.name.countryCode.district.population.insert(
  City.name("Singapore").countryCode.query.get.map {
    case (name, countryCode) => ("New-" + name, countryCode, "", 0L)
  }
).transact

City.id.name.countryCode_("SGP").district.population.query.get ==> Seq(
  (3208, "Singapore", "", 4017733),
  (4080, "New-Singapore", "", 0)
)
```

## Updates

#### static values

```scala
val query = City
  .update(_.countryCode === "SGP")
  .set(_.population := 0, _.district := "UNKNOWN")

db.run(query)

db.run(City.select.filter(_.countryCode === "SGP").single) ==>
  City[Sc](3208, "Singapore", "SGP", district = "UNKNOWN", population = 0)
```

Filter entities to be updated with tacit attributes and apply new data to mandatory attributes:

```scala
City.countryCode_("SGP").district("UNKNOWN").population(0).update.transact

City.name.countryCode_("SGP").district.population.query.get ==> Seq(
  ("Singapore", "UNKNOWN", 0)
)
```

#### computed values

```scala
val query = City
  .update(_.countryCode === "SGP")
  .set(c => c.population := (c.population + 1000000))

db.run(query)

db.run(City.select.filter(_.countryCode === "SGP").single) ==>
  City[Sc](3208, "Singapore", "SGP", district = "", population = 5017733)
```

```scala
City.countryCode_("SGP").population.+(1000000).update.transact

City.name.countryCode_("SGP").population.query.get ==> Seq(
  ("Singapore", 5017733)
)
```

#### multiple values

```scala
val query = City.update(_ => true).set(_.population := 0)
db.renderSql(query) ==> "UPDATE city SET population = ?"

db.run(query)

db.run(City.select.filter(_.countryCode === "LIE")) ==> Seq(
  City[Sc](2445, "Schaan", "LIE", district = "Schaan", population = 0),
  City[Sc](2446, "Vaduz", "LIE", district = "Vaduz", population = 0)
)
```

Filter all cities having a population value and then set it to 0 for all! Beware

```scala
City.population_.population(0).update.transact

City.name.countryCode_("LIE").population.query.get ==> Seq(
  ("Schaan", 0),
  ("Vaduz", 0),
)
```

## Deletes

```scala
val query = City.delete(_.countryCode === "SGP")
db.renderSql(query) ==> "DELETE FROM city WHERE (city.countrycode = ?)"
db.run(query)

db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq()
```

Filter entities to be updated with tacit attributes and apply new data to mandatory attributes:

```scala
City.name(count).countryCode_("SGP").query.get.head ==> 1
City.countryCode_("SGP").delete.transact
City.name(count).countryCode_("SGP").query.get.head ==> 0
```

## Transactions

```scala
try {
  dbClient.transaction { implicit db =>
    db.run(City.delete(_.countryCode === "SGP"))
    db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq()
    throw new Exception()
  }
} catch {
  case e: Exception => /*do nothing*/
}

dbClient.transaction { implicit db =>
  db.run(City.select.filter(_.countryCode === "SGP").single) ==>
    City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733)
}
```

Transactions in Molecule are usually called on a specific action (save/insert/update/delet molecule) like

```scala
City.countryCode_("SGP").delete.transact
```

This commits immediately. If you want to transact and also check things with a query, you can use a `unitOfWork` that can be rolled back if an exception is thrown:

```scala
try {
  unitOfWork {
    City.countryCode_("SGP").delete.transact
    City.countryCode("SGP").query.get ==> Seq()
    throw new Exception()
  }
} catch {
  case _: Exception => ()
}
City.countryCode("SGP").query.get ==> Seq("SGP")
```

`unitOfWork` doesn't have a handle to manually rollback. For this, use a savepoint (see below).


#### Savepoints

In ScalaSql, a `savepoint` block executes inside a `transaction` block. It has a handle that you can use to roll back the actions inside the savepoint body:

```scala
dbClient.transaction { implicit db =>
  db.savepoint { implicit sp =>
    db.run(City.delete(_.countryCode === "SGP"))
    db.run(City.select.filter(_.countryCode === "SGP")) ==> Seq()
    sp.rollback()
  }

  db.run(City.select.filter(_.countryCode === "SGP").single) ==>
    City[Sc](3208, "Singapore", "SGP", district = "", population = 4017733)
}
```

You can do the same in Molecule, except that the savepoint block is inside a `unitOfWork` block:

```scala
City.countryCode("SGP").query.get ==> Seq("SGP")
unitOfWork {
  savepoint { sp =>
    City.countryCode_("SGP").delete.transact
    City.countryCode("SGP").query.get ==> Seq()
    sp.rollback()
  }
}
City.countryCode("SGP").query.get ==> Seq("SGP")
```

A thrown exception also rolls back the savepoint actions:

```scala
City.countryCode("SGP").query.get ==> Seq("SGP")
try {
  unitOfWork {
    savepoint { _ =>
      City.countryCode_("SGP").delete.transact
      City.countryCode("SGP").query.get ==> Seq()
      throw new Exception()
    }
  }
} catch {
  case _: Exception => ()
}
City.countryCode("SGP").query.get ==> Seq("SGP")
```

--- 

Molecule doesn't implement custom expressions or Type Mappings.