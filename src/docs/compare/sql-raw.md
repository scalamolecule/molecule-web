# SQL raw

Comparing Molecule with popular SQL libraries using SQL interpolation  

|              |          Molecule          | Doobie  | Skunk    |      Anorm       | Magnum  |        ldbc        | JDBC<br>(Java) |
|--------------|:--------------------------:|:-------:|:---------:|:----------------:|:-------:|:------------------:|:--------------:|
| Scala 3      |             ✅              |    ✅    | ✅        |        ✅         |    ✅    |         ✅          |       -        |
| Scala 2.13   |             ✅              |    ✅    | ✅        |        ✅         |         |                    |       -        |
| Scala 2.12   |             ✅              |    ✅    | ✅        |        ✅         |         |                    |       -        |
| Scala JS     |             ✅              |         |          |                  |         |         ✅?         |       -        |
| Scala Native |                            |         |          |                  |         |         ✅?         |       -        |
| Model        |     codegen<br>runtime     | runtime | runtime  | macro<br>compile | runtime | runtime<br>pure FP |    runtime     |
| API          | Sync<br>Async<br>ZIO<br>IO |  F[_]   | F[_]     |       Sync       |  Sync   |         IO         |      Sync      |
|              |                            |         |          |                  |         |                    |                |
| Postgres     |             ✅              |    ✅    | ✅        |        ✅         |    ✅    |         ✅          |       ✅        |
| MySQL        |             ✅              |    ✅    | ✅        |        ✅         |    ✅    |         ✅          |       ✅        |
| H2           |             ✅              |    ✅    | ✅        |        ✅         |    ✅    |         ✅          |       ✅        |
| SQLite       |             ✅              |    ✅    | ✅        |        ✅         |         |                    |       ✅        |
| MariaDB      |             ✅              |         |          |                  |         |                    |       ✅        |
| Oracle       |                            |    ✅    |          |        ✅         |         |         ✅          |       ✅        |
| SQL Server   |                            |    ✅    |          |        ✅         |         |         ✅          |       ✅        |
| DB2          |                            |    ✅    |          |                  |         |         ✅          |       ✅        |
| HSQLDB       |                            |    ✅    |          |                  |    ✅    |                    |       ✅        |
| Derby        |                            |    ✅    |          |                  |         |         ✅          |       ✅        |
| OrientDB     |                            |         |          |        ✅         |         |                    |                |
| Cassandra    |                            |         |          |        ✅         |         |                    |                |
| Spark        |                            |         |          |        ✅         |         |                    |                |
| Clickhouse   |                            |         |          |                  |     ✅    |                    |                |




Features supported...



