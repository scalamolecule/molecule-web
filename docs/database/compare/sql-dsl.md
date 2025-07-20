# SQL DSL

Comparing Molecule with popular SQL libraries using a DSL

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

