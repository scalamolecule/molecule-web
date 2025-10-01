# SQL-like DSL

Scala SQL libraries offering a SQL-like DSL:

|                    |          Molecule          |  [ScalikeJDBC]   | [ldbc]  | [Typo]  | [Squeryl]<br>(Java) | [JOOQ]<br>(Java) |
|--------------------|:--------------------------:|:----------------:|:-------:|:-------:|:-------------------:|:----------------:|
| Scala 3            |             ✅              |        ✅         |    ✅    |    ✅    |          -          |        -         |
| Scala 2.13         |                            |        ✅         |         |    ✅    |          -          |        -         |
| Scala 2.12         |                            |        ✅         |         |         |          -          |        -         |
| Scala JS           |             ✅              |                  |  ✅ 2)   |         |          -          |        -         |
| Scala Native       |                            |                  |  ✅ 2)   |         |          -          |        -         |
| Model              |          runtime           |     runtime      | runtime | runtime |       runtime       |     runtime      |
| API                | Sync<br>Async<br>ZIO<br>IO | Sync<br/>pure FP |   IO    |         |        Sync         |       Sync       |
| DSL                |                            |                  |         |         |                     |     SQL 1-1      |
| Raw SQL fallback   |             ✅              |        ✅         |    ✅    |         |          ✅          |        ✅         |
| Code generation    |             ✅              |                  |         |    ✅    |                     |                  |
| Repository methods |                            |                  |         |    ✅    |                     |                  |
| Db compliance SPI  |            ✅ 1)            |                  |         |         |                     |                  |

1) Molecule guarantees that all supported databases behave identically for the end-user. Each supported database pass the same comprehensive compliance SPI test suite of ~2000 tests.
2) Compiles to JS/Native, but no examples of use provided.

### Supported databases

|            | Molecule | Scalike<br>JDBC | ldbc | Typo | Squeryl<br>(Java) | JOOQ<br>(Java) |
|------------|:--------:|:---------------:|:----:|:----:|:-----------------:|:--------------:|
| Postgres   |    ✅     |        ✅        |  ✔   |  ✅   |         ✅         |       ✅        |
| MySQL      |    ✅     |        ✅        |  ✅   |      |         ✅         |       ✅        |
| H2         |    ✅     |        ✅        |  ✔   |      |         ✅         |       ✅        |
| SQLite     |    ✅     |                 |  ✔   |      |                   |       ✅        |
| MariaDB    |    ✅     |                 |  ✔   |      |                   |       ✅        |
| Oracle     |          |                 |  ✔   |      |         ✅         |       ✅        |
| SQL Server |          |                 |  ✔   |      |         ✅         |       ✅        |
| DB2        |          |                 |  ✔   |      |         ✅         |       ✅        |
| HSQLDB     |          |        ✅        |  ✔   |      |                   |       ✅        |
| Derby      |          |                 |  ✔   |      |         ✅         |       ✅        |
| OrientDB   |          |                 |  ✔   |      |                   |                |
| Cassandra  |          |                 |  ✔   |      |                   |                |
| Spark      |          |                 |  ✔   |      |                   |                |
| Clickhouse |          |                 |  ✔   |      |                   |                |

✅ = Supported and tested<br>
&nbsp;✔ &nbsp;= JDBC-compliant database support

### Features

|          | Molecule | Scalike<br>JDBC | ldbc | Squeryl<br>(Java) | JOOQ<br>(Java) |
|----------|:--------:|:---------------:|:----:|:-----------------:|:--------------:|
| Postgres |    ✅     |        ✅        |  ✔   |         ✅         |       ✅        |
| MySQL    |    ✅     |        ✅        |  ✅   |         ✅         |       ✅        |
| H2       |    ✅     |        ✅        |  ✔   |         ✅         |       ✅        |


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

[ScalikeJDBC]: https://scalikejdbc.org
[ldbc]: https://takapi327.github.io/ldbc/
[Typo]: https://github.com/oyvindberg/typo
[Squeryl]: https://www.squeryl.org
[JOOQ]: https://www.jooq.org