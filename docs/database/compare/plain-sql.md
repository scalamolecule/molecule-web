# Plain SQL

Plain SQL libraries allow you to write SQL directly, typically through string interpolators or lightweight wrappers, with minimal abstraction.

- [Doobie](https://typelevel.org/doobie/index.html) - Functional JDBC wrapper with typeclasses and F[_] support
- [Magnum](https://github.com/AugustNagro/magnum) - Minimalist raw-SQL library with repository helpers
- [Skunk](https://typelevel.org/skunk/) - Pure functional Postgres library using cats-effect
- [Anorm](https://playframework.github.io/anorm/) - Lightweight SQL wrapper integrated with Play Framework

## Comparison

We'll compare Molecule with the plain-SQL libraries in the following areas:

1. [Environment](#_1-environment) - Scala versions, JS/Native, APIs, runtime/compiletime etc
2. [Supported databases](#_2-supported-databases) - tested/optional/not supported databases
3. [Data types](#_3-types) - attribute types and defaults
4. [Setup](#_4-setup) - schema, model mappings
5. [Query](#_5-query) - syntax, features, result types
6. [Transaction](#_6-transaction) - mutation syntax and tx handling
7. [Integration](#_7-integration)  with other libraries


## 1. Environment

|                            |          Molecule          | Doobie  | Magnum  |  Skunk  |    Anorm     |
|----------------------------|:--------------------------:|:-------:|:-------:|:-------:|:------------:|
| Scala 3                    |             ✅              |    ✅    |    ✅    |    ✅    |      ✅       |
| Scala 2.13                 |             -              |    ✅    |    -    |    ✅    |      ✅       |
| Scala 2.12                 |             -              |    ✅    |    -    |    ✅    |      ✅       |
| Scala JS                   |             ✅              |    -    |    -    |    -    |      -       |
| Scala Native               |             -              |    -    |    -    |    -    |      -       |
| Model                      |          runtime           | runtime | runtime | runtime |   compile    |
| API                        | Sync<br>Async<br>ZIO<br>IO |  F[_]   |  Sync   |  F[_]   |     Sync     |
| Code generation            |             ✅              |    -    |    -    |    -    | ✅<br>(macro) |
| Repository pattern support |                            |    -    |    ✅    |    -    |      -       |
| Db compliance SPI          |            ✅ 1)            |    -    |    -    |    -    |      -       |

1) Molecule guarantees that all supported databases behave identically for the end-user. Each supported database pass the same comprehensive compliance SPI test suite of 2000+ tests.


## 2. Supported databases

|            | Molecule | Doobie | Magnum | Skunk | Anorm |
|------------|:--------:|:------:|:------:|:-----:|:-----:|
| Postgres   |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| MySQL      |    ✅     |   ✅    |   ✅    |   ❌   |   ✅   |
| H2         |    ✅     |   ✅    |   ✅    |   ❌   |   ✅   |
| SQLite     |    ✅     |   ✅    |   ✔    |   ❌   |   ✅   |
| MariaDB    |    ✅     |   ✔    |   ✔    |   ❌   |   ✔   |
| Oracle     |    -     |   ✅    |   ✔    |   ❌   |   ✅   |
| SQL Server |    -     |   ✅    |   ✔    |   ❌   |   ✅   |
| DB2        |    -     |   ✅    |   ✔    |   ❌   |   ✔   |
| HSQLDB     |    -     |   ✅    |   ✅    |   ❌   |   ✔   |
| Derby      |    -     |   ✅    |   ✔    |   ❌   |   ✔   |
| OrientDB   |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Cassandra  |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Spark      |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Clickhouse |    -     |   ✔    |   ✅    |   ❌   |   ✔   |

✅ = Fully supported and tested<br>
✔ = JDBC-compatible; should work<br>
\- = Can be implemented<br>
❌ = Not supported


## 3. Data types

|                          | Molecule | Doobie | Magnum | Skunk | Anorm |
|--------------------------|:--------:|:------:|:------:|:-----:|:-----:|
| _**Primitives**_         |          |        |        |       |       |
| String                   |    ✅     |        |        |       |       |
| Int                      |    ✅     |        |        |       |       |
| Long                     |    ✅     |        |        |       |       |
| Float                    |    ✅     |        |        |       |       |
| Double                   |    ✅     |        |        |       |       |
| Boolean                  |    ✅     |        |        |       |       |
| BigInt                   |    ✅     |        |        |       |       |
| BigDecimal               |    ✅     |        |        |       |       |
| Byte                     |    ✅     |        |        |       |       |
| Short                    |    ✅     |        |        |       |       |
| Char                     |    ✅     |        |        |       |       |
|                          |          |        |        |       |       |
| Enum                     |    ✅     |        |        |       |       |
|                          |          |        |        |       |       |
| _**Java types**_         |          |        |        |       |       |
| java.util.Date           |    ✅     |        |        |       |       |
| java.util.UUID           |    ✅     |        |        |       |       |
| java.net.URI             |    ✅     |        |        |       |       |
| java.time.Duration       |    ✅     |        |        |       |       |
| java.time.Instant        |    ✅     |        |        |       |       |
| java.time.LocalDate      |    ✅     |        |        |       |       |
| java.time.LocalTime      |    ✅     |        |        |       |       |
| java.time.LocalDateTime  |    ✅     |        |        |       |       |
| java.time.OffsetTime     |    ✅     |        |        |       |       |
| java.time.OffsetDateTime |    ✅     |        |        |       |       |
| java.time.ZonedDateTime  |    ✅     |        |        |       |       |
|                          |          |        |        |       |       |
| _**Collections**_        |          |        |        |       |       |
| Seq                      |    ✅     |        |        |       |       |
| Set                      |    ✅     |        |        |       |       |
| Map                      |    ✅     |        |        |       |       |
|                          |          |        |        |       |       |
|                          |          |        |        |       |       |
| _**Collections**_        |          |        |        |       |       |
| Geo-spatial?             |          |        |        |       |       |
| Composite?               |          |        |        |       |       |
| More?                    |          |        |        |       |       |


## 4. Setup

- "What is needed to set up to start using each library?"
- Setups for all libs should have the same tables/attributes so that we can compare

::: tabs#coord

@tab Doobie
```scala
// Doobie code..
```

@tab Magnum
```scala
// Magnum code..
```

@tab Skunk
```scala
// Skunk code..
```

@tab Anorm
```scala
// Anorm code..
```

Anorm explanation ...

:::


More comparisons...?

|                                  | Molecule | Doobie | Magnum | Skunk | Anorm |
|----------------------------------|:--------:|:------:|:------:|:-----:|:-----:|
| SQL schema generated             |    ✅     |        |        |       |       |
| Custom attribute types           |          |        |        |       |       |
| Views                            |          |        |        |       |       |
| Migration handling (a la Flyway) |          |        |        |       |       |


## 5. Query

|                        |    Molecule    | Doobie | Magnum | Skunk | Anorm |
|------------------------|:--------------:|:------:|:------:|:-----:|:-----:|
| Type-safe query        |       ✅        |        |        |       |       |
| Type-safe filtering    |       ✅        |        |        |       |       |
| Type-safe result       |       ✅        |        |        |       |       |
| Nested result          |       ✅        |        |        |       |       |
| Sorting                |       ✅        |        |        |       |       |
| Offset pagination      |       ✅        |   ✅    |   ✅    |   ✅   |   ✅   |
| Cursor pagination      |       ✅        |        |        |       |       |
| Attribute filter       |       ✅        |        |        |       |       |
| Attribute calculations |                |        |        |       |       |
| Having                 |       ✅        |        |        |       |       |
| union, unionAll        |                |        |        |       |       |
| except, intersect      |                |        |        |       |       |
| window functions       |                |        |        |       |       |
| window functions       |                |        |        |       |       |
| Tuples returned        |       ✅        |        |        |       |       |
| Mapping to case class  |       ✅        |        |        |       |       |
| Variable binding       |       ✅        |        |        |       |       |
| Subscription           |       ✅        |        |        |       |       |
| Streaming              | ZStream<br>fs2 |  fs2   |        |       |       |
| Subqueries             |                |   ✅    |        |       |       |


### Example query

"Departments With More Than 2 Employees Assigned to Projects With a Budget Exceeding 1M"

Molecule:
```scala
Department.name.Employees.id_(countDistinct).>(2).d1.Projects.budget_.>(1000000)
```

SQL:
```scala
sql"""SELECT d.name AS department
      FROM Department d
      JOIN Employee e             ON e.department = d.id
      JOIN EmployeeProject ep     ON e.id = ep.employee
      JOIN Project p              ON ep.project = p.id
      WHERE p.budget > 1000000
      GROUP BY d.id, d.name
      HAVING COUNT(DISTINCT e.id) > 2
      ORDER BY COUNT(DISTINCT e.id) DESC"""
```

Of course, Table prefixes could often be omitted but for more complex queries they will often be needed anyway to distinguish fields.


## 6. Transaction

|                       | Molecule | Doobie | Magnum | Skunk | Anorm |
|-----------------------|:--------:|:------:|:------:|:-----:|:-----:|
| Type-safe transaction |    ✅     |        |        |       |       |
| Save points           |    ✅     |        |        |       |       |
| Unit of work          |    ✅     |        |        |       |       |
| Rollback              |    ✅     |        |        |       |       |
| Join mutations        |    ✅     |        |        |       |       |
| Nested mutations      |    ✅     |        |        |       |       |

Comparing Molecule [save](/database/transact/save), [update](/database/transact/update) and [delete](/database/transact/delete) with mutations in plain SQL libraries:

(same idea as for query...)

::: code-tabs#coord
@tab Save

```scala
// Molecule
Person.name("Ben").age(22).Address.street("Main st.").save

// SQL - 2 operations
INSERT INTO Address(
  street
) VALUES ("Main st.")

INSERT INTO Person(
  name,
  age,
  home
) VALUES("Ben", 22, [addressId])
```

@tab Update

```scala
// Molecule [read more](/database/transact/save)
Person(benId).age(23).update

// SQL
UPDATE Person
  SET age = ?
WHERE
Person.id IN (benId) AND
  Person.age IS NOT NULL;
```

@tab Delete

```scala
// Molecule
Person(benId).delete

// SQL
DELETE FROM Person WHERE id IN (benId)
```
:::


## 7. Integration

bla bla

|       | Molecule | Doobie | Magnum | Skunk | Anorm |
|-------|:--------:|:------:|:------:|:-----:|:-----:|
| ZIO   |    ✅     |   ✅    |        |   ✅   |       |
| Cats  |    ✅     |   ✅    |        |       |       |
| Play  |          |        |        |       |   ✅   |
| Akka  |          |        |        |       |       |
| Pekko |          |        |        |       |       |


Okay, let me be a bit more specific.
I want to examine how to name many-to-many join tables that have no additional attributes.
I'm considering to define such a domain structure like I do already in Molecule.
We'll examine the more complex case of many-to-many relations with additional attributes later.
For now, let's focus on the simple case and how to

Okay, let me be a bit more specific.
Say that I define two many-to-many relationships:

```scala
object WebCompany extends DomainStructure {

  trait Employee {
    val name      = oneString
    val frontends = manyToOne[Project].designers
    val backends  = manyToOne[Project]
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
    val designers = manyToOne[Employee]
    val engineers = manyToOne[Employee].backends
  }


  trait Person {
    val friends = manyToOne[Person]
  }
}
```
Notice how I tie the Employee.fontends ref to the reverse designers ref in the related Project trait to
denote that these should share the same join table.  
And likewise for the Project.engineers ref to Employee.backends ref - now just making the link
from the Project side.

What would be meaningfull ways to distinctively name the two join tables?


As I do it now, I name them like this:

Employee_frontends_Project // using ref attribute name "frontends" from Employee trait
Project_engineers_Employee // using ref attribute name "engineers" from Project trait

This takes the distinguishing ref attrubute name from the "defining" side of the relationship.
