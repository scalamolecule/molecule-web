# DSL-based

The DSL-based libraries offer a DSL that either resembles Scala collection-like functionality (map, flatMap etc) or SQL-like operations (join, select etc)

- [Slick](https://scala-slick.org) - [compare](/database/compare/slick)
- [Zio-Quill](https://github.com/zio/zio-quill) - [compare](/database/compare/protoquill)
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

