# SQL or DSL

SQL database libraries in the Scala ecosystem are basically SQL- or DSL-based. So, the primary choice of library comes down to whether you want to write SQL or use some higher-level DSL abstraction that the library then translates to SQL for you.



## SQL

Popular libraries that focus on letting you write SQL queries and transactions:

- [Doobie](https://typelevel.org/doobie/index.html)
- [Magnum](https://github.com/AugustNagro/magnum)
- [Anorm](https://playframework.github.io/anorm/)
- [Skunk](https://typelevel.org/skunk/)
- [ldbc](https://takapi327.github.io/ldbc/)
- [JDBC (Java)](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)


|              |          Molecule          | Doobie  | Magnum  |  Skunk  |      Anorm       |        ldbc        | JDBC<br>(Java) |
|--------------|:--------------------------:|:-------:|:-------:|:-------:|:----------------:|:------------------:|:--------------:|
| Scala 3      |             ✅              |    ✅    |    ✅    |    ✅    |        ✅         |         ✅          |       -        |
| Scala 2.13   |                            |    ✅    |         |    ✅    |        ✅         |                    |       -        |
| Scala 2.12   |                            |    ✅    |         |    ✅    |        ✅         |                    |       -        |
| Scala JS     |             ✅              |         |         |         |                  |         ✅?         |       -        |
| Scala Native |                            |         |         |         |                  |         ✅?         |       -        |
| Model        |     codegen<br>runtime     | runtime | runtime | runtime | macro<br>compile | runtime<br>pure FP |    runtime     |
| API          | Sync<br>Async<br>ZIO<br>IO |  F[_]   |  Sync   |  F[_]   |       Sync       |         IO         |      Sync      |
|              |                            |         |         |         |                  |                    |                |
| Postgres     |             ✅              |    ✅    |    ✅    |    ✅    |        ✅         |         ✅          |       ✅        |
| MySQL        |             ✅              |    ✅    |    ✅    |    ✅    |        ✅         |         ✅          |       ✅        |
| H2           |             ✅              |    ✅    |    ✅    |    ✅    |        ✅         |         ✅          |       ✅        |
| SQLite       |             ✅              |    ✅    |         |    ✅    |        ✅         |                    |       ✅        |
| MariaDB      |             ✅              |         |         |         |                  |                    |       ✅        |
| Oracle       |                            |    ✅    |         |         |        ✅         |         ✅          |       ✅        |
| SQL Server   |                            |    ✅    |         |         |        ✅         |         ✅          |       ✅        |
| DB2          |                            |    ✅    |         |         |                  |         ✅          |       ✅        |
| HSQLDB       |                            |    ✅    |    ✅    |         |                  |                    |       ✅        |
| Derby        |                            |    ✅    |         |         |                  |         ✅          |       ✅        |
| OrientDB     |                            |         |         |         |        ✅         |                    |                |
| Cassandra    |                            |         |         |         |        ✅         |                    |                |
| Spark        |                            |         |         |         |        ✅         |                    |                |
| Clickhouse   |                            |         |    ✅    |         |                  |                    |                |


### Query

Molecule generates boilerplate that lets you compose attributes to form a query:

```scala
// Molecule
Person.name.age.Address.street
```
This will transparently be transformed to the following SQL that you would otherwise have to write with one of the SQL libraries:

```scala
// SQL
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
Of course, Table prefixes could often be omitted but for more complex queries they will often be needed anyway to distinguish fields.

### Transact

::: code-tabs#coord
@tab Save
```scala
// Molecule
Person.name("Ben").age(22).Address.street("Main st.").save

// SQL - 2 operations
INSERT INTO Address (
  street
) VALUES ("Main st.")

INSERT INTO Person (
  name,
  age,
  home
) VALUES ("Ben", 22, [addressId])
```

@tab Update
```scala
// Molecule
Person(benId).age(23).update

// SQL
UPDATE Person
  SET
age = ?
WHERE
Person.id IN(benId) AND
  Person.age IS NOT NULL
```

@tab Delete
```scala
// Molecule
Person(benId).delete

// SQL
DELETE FROM Person WHERE id IN (benId)
```
:::


## DSL

The DSL-based libraries offer a DSL that either resembles Scala collection-like functionality (map, flatMap etc) or SQL-like operations (join, select etc)

- [Slick](https://scala-slick.org) - [compare](/database/compare/slick)
- [Zio-Quill](https://github.com/zio/zio-quill)
- [ScalaSql](https://github.com/com-lihaoyi/scalasql) - [compare](/database/compare/scalasql)
- [ScalikeJDBC](https://scalikejdbc.org)
- [Typo](https://github.com/oyvindberg/typo)
- [Squeryl (Java)](https://www.squeryl.org)
- [JOOQ (Java)](https://www.jooq.org)



|              |          Molecule          |  Slick  |      Quill       | Scala<br>SQL | Scalike<br>JDBC | Squeryl<br>(Java) | JOOQ<br>(Java) |
|--------------|:--------------------------:|:-------:|:----------------:|:------------:|:---------------:|:-----------------:|:--------------:|
| Scala 3      |             ✅              |    ✅    |        ✅         |      ✅       |        ✅        |         -         |       -        |
| Scala 2.13   |                            |    ✅    |        ✅         |      ✅       |        ✅        |         -         |       -        |
| Scala 2.12   |                            |    ✅    |        ✅         |              |        ✅        |         -         |       -        |
| Scala JS     |             ✅              |         |                  |              |                 |         -         |       -        |
| Scala Native |                            |         |                  |              |                 |         -         |       -        |
| Model        |     codegen<br>runtime     | runtime | macro<br>compile |   runtime    |     runtime     |      runtime      |    runtime     |
| API          | Sync<br>Async<br>ZIO<br>IO |  DBIO   |       ZIO        |     Sync     |      Sync       |       Sync        |      Sync      |
|              |                            |         |                  |              |                 |                   |                |
| Postgres     |             ✅              |    ✅    |        ✅         |      ✅       |        ✅        |         ✅         |       ✅        |
| MySQL        |             ✅              |    ✅    |        ✅         |      ✅       |        ✅        |         ✅         |       ✅        |
| H2           |             ✅              |    ✅    |        ✅         |      ✅       |        ✅        |         ✅         |       ✅        |
| SQLite       |             ✅              |    ✅    |        ✅         |      ✅       |                 |                   |       ✅        |
| MariaDB      |             ✅              |         |                  |              |                 |                   |       ✅        |
| Oracle       |                            |    ✅    |        ✅         |              |                 |         ✅         |       ✅        |
| SQL Server   |                            |    ✅    |        ✅         |              |                 |         ✅         |       ✅        |
| DB2          |                            |    ✅    |                  |              |                 |         ✅         |       ✅        |
| HSQLDB       |                            |    ✅    |                  |              |        ✅        |                   |       ✅        |
| Derby        |                            |    ✅    |                  |              |                 |         ✅         |       ✅        |
| OrientDB     |                            |         |        ✅         |              |                 |                   |                |
| Cassandra    |                            |         |        ✅         |              |                 |                   |                |
| Spark        |                            |         |        ✅         |              |                 |                   |                |
| Clickhouse   |                            |         |                  |              |                 |                   |                |


All DSL libraries also offer SQL interpolation to fallback on raw sql.


### Query

::: code-tabs#coord
@tab ScalaSql

```scala
// Molecule
Person.name.age.Address.street.query.get

// ScalaSql
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Slick

```scala
// Molecule
Person.name.age.Address.street.query.get

// Slick
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```
:::

### Transact

::: code-tabs#coord
@tab ScalaSql

```scala
// Molecule
City.name.countryCode.district.population.insert(
  ("Sentosa", "SGP", "South", 1337)
).transact

// ScalaSql
val query = City.insert.columns(
  _.name := "Sentosa",
  _.countryCode := "SGP",
  _.district := "South",
  _.population := 1337
)
db.run(query)
```

@tab Slick

```scala
// Molecule
City.name.countryCode.district.population.insert(
  ("Sentosa", "SGP", "South", 1337)
).transact

// Slick
val query = cities.map(p => (p.name, p.countryCode, p.district, p.population))
  .insert(("Sentosa", "SGP", "South", 1337))
db.run(query.result)
```
:::

