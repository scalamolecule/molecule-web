# SQL-based

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
