# Collection-like DSL

Scala SQL libraries offering a Scala collection-like DSL:


|                  |          Molecule          |    [ScalaSQL]     | [Slick] |   [Quill]    |
|------------------|:--------------------------:|:-----------------:|:-------:|:------------:|
| Scala 3          |             ✅              |         ✅         |    ✅    |      ✅       |
| Scala 2.13       |                            |         ✅         |    ✅    |      ✅       |
| Scala 2.12       |                            |                   |    ✅    |      ✅       |
| Scala JS         |             ✅              |                   |         |              |
| Scala Native     |                            |                   |         |              |
| Model            |          runtime           |      runtime      | runtime |   compile    |
| API              | Sync<br>Async<br>ZIO<br>IO |       Sync        |  DBIO   |     ZIO      |
| DSL              |                            | Scala<br>col-like |         |              |
| Raw SQL fallback |             ✅              |         ✅         |    ✅    |      ✅       |
| Code generation  |             ✅              |                   |         | ✅<br>(macro) |

### Supported databases

|            | Molecule | Scala<br>SQL | Slick | Quill |
|------------|:--------:|:------------:|:-----:|:-----:|
| Postgres   |    ✅     |      ✅       |   ✅   |   ✅   |
| MySQL      |    ✅     |      ✅       |   ✅   |   ✅   |
| H2         |    ✅     |      ✅       |   ✅   |   ✅   |
| SQLite     |    ✅     |      ✅       |   ✅   |   ✅   |
| MariaDB    |    ✅     |              |       |       |
| Oracle     |          |              |   ✅   |   ✅   |
| SQL Server |          |              |   ✅   |   ✅   |
| DB2        |          |              |   ✅   |       |
| HSQLDB     |          |              |   ✅   |       |
| Derby      |          |              |   ✅   |       |
| OrientDB   |          |              |       |   ✅   |
| Cassandra  |          |              |       |   ✅   |
| Spark      |          |              |       |   ✅   |
| Clickhouse |          |              |       |       |

✅ = Supported and tested<br>
&nbsp;✔ &nbsp;= JDBC-compliant database support

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


[ScalaSql]: https://github.com/com-lihaoyi/scalasql
[Slick]: https://scala-slick.org
[Quill]: https://github.com/zio/zio-quill