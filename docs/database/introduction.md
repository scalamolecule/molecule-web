
# Introduction

<br>

![Molecule logo](/static/img/logo/Molecule-logo-600.png)


Molecule is a Scala 3 library that lets you query and mutate SQL databases using type-inferred code written with the words of your domain.

Here's a query across two tables:

```scala
Person.name.age.Address.street
```


No SQL strings, no join syntax, no mapping boilerplate. Just your domain concepts composed together. The compiler ensures you only write valid queries, and you get back typed data.

You express intent; Molecule translates it into optimized SQL queries and handles the execution.



## How It Works

Define your domain structure in plain Scala:

```scala
import molecule.DomainStructure

object MyDomainStructure extends DomainStructure {
  trait Person {
    val name    = oneString
    val age     = oneInt
    val address = manyToOne[Address]
  }
  trait Address {
    val street = oneString
  }
}
```

Run `sbt moleculeGen` and the sbt-molecule plugin will generate a custom Scala DSL for your domain. No macros, no runtime reflection.

Now you can compose molecules to model the data that you want to query or mutate. Molecule translates them into optimized SQL. No need to write SQL strings or join syntax:

::: code-tabs#coord
@tab Molecule
```scala
Person.name.age.Address.street
```
@tab SQL
```sql
SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;
```
@tab ScalaSql
```scala
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```
@tab Slick
```scala
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```
:::


<details>
<summary>Complex example</summary>

Departments and number of developers working on projects with more than 2 employees and a budget over one million ([see more](/database/relationships/complex-example.html))
```scala
// Molecule
Department.name
  .Employees.id(countDistinct).>(2).d1
  .Projects.budget_.>(1000000)
  .query.i.get ==> List(("Development", 4))
```

```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee   ON Department.id = Employee.department
  INNER JOIN Assignment ON Employee.id = Assignment.employee
  INNER JOIN Project    ON Assignment.project = Project.id
WHERE
  Department.name IS NOT NULL AND
  Project.budget  > 1000000
GROUP BY Department.name
HAVING COUNT(DISTINCT Employee.id) > 2
ORDER BY Employee_id_count DESC;
```

</details>


## Query and Transact

Execute queries with one of four APIs—sync, async, ZIO, or Cats Effect:

::: code-tabs#coord
@tab Sync
```scala
import molecule.db.postgres.sync.*
       
val persons: List[(String, Int, String)] =
  Person.name.age.Address.street.query.get
```

@tab Async
```scala
import molecule.db.postgres.async.*

val persons: Future[List[(String, Int, String)]] =
  Person.name.age.Address.street.query.get
```

@tab ZIO
```scala
import molecule.db.postgres.zio.*

val persons: ZIO[Conn, MoleculeError, List[(String, Int, String)]] =
  Person.name.age.Address.street.query.get
```

@tab IO
```scala
import molecule.db.postgres.io.*

val persons: cats.effect.IO[List[(String, Int, String)]] =
  Person.name.age.Address.street.query.get
```
:::


Transact data just as easily:

::: code-tabs#coord
@tab Save
```scala
Person.name("Ben").age(22).Address.street("DoobieSetup2 st.").save.transact
```

@tab Insert
```scala
Person.name.age.Address.street.insert(List(
  ("Lisa", 20, "Broadway"),
  ("John", 24, "5th Avenue")
)).transact
```

@tab Update
```scala
Person(lisaId).age(21).update.transact
```

@tab Delete
```scala
Person(benId).delete.transact
```
:::


### Declarative, not Imperative

Instead of thinking in SQL terms (tables, joins, indexes), Molecule encourages staying in the mental model of the domain: what domain data are you trying to model?

This separation between *declarative intent* and *imperative implementation* is a core principle of Molecule. You express intent; Molecule translates it to optimized SQL queries and mutations.

## Supported Databases

Molecule supports:

- PostgreSQL
- SQLite
- MySQL
- MariaDB
- H2
- (more can easily be added...)

All Molecule queries behave identically across these databases. Each backend passes the same SPI compliance test suite with +2000 tests.


## Why Molecule?

- **Type-safe from end to end** — no string queries, no surprises
- **Write in your own domain language** — not SQL
- **Built-in authorization** — move the security boundary to the data layer, not scattered across endpoints
- **Declarative, not imperative** — express intent, not mechanics
- **Composable and immutable** — functional by design
- **Cross-platform** — JVM and Scala.js with built-in RPC and serialization
- **Same behavior across backends** — Postgres, MySQL, MariaDB, SQLite, H2

## Key Features

**[Authorization](/database/authorization/overview)** — Built-in RBAC (Role-Based Access Control) at the attribute level. Define roles and permissions in your domain structure, and Molecule enforces them automatically in all queries and transactions with highly optimized bit masking for minimal runtime overhead. No need to scatter security logic across endpoints and business code.

**[Validation](/database/setup/validation)** — Validate data at insertion and update time with built-in validators or custom validation functions. Molecule ensures data integrity before it reaches the database.

**[Filtering](/database/query/filters)** and **[Aggregations](/database/query/aggregation)** — Filter data with intuitive operators and aggregate with functions like `count`, `sum`, `avg`, `min`, and `max`. Compose complex queries without leaving your domain language.

**[Sorting](/database/query/sorting)** and **[Pagination](/database/query/pagination)** — Sort results by any attribute in ascending or descending order. Paginate with offset-based or cursor-based pagination for efficient data loading.

**[Optional](/database/relationships/many-to-one#optional-relationship)** and **[Nested Data](/database/relationships/complex-example#nested-queries)** — Query optional attributes/relationships and traverse nested relationships naturally. Unlike other SQL libraries that return flat data, Molecule can return hierarchical nested data structures (up to 7 levels deep), eliminating the need to manually group and format results in your application code.

**[Transaction Management](/database/transaction/tx-management)** — Full transaction support with `unitOfWork` for composing multiple operations, `savepoint` for nested transactions, and `rollback` for fine-grained control. Execute complex transactional workflows with confidence.

**[Subscriptions](/database/query/subscription)** — Subscribe to data changes and receive real-time updates when your queries match new or modified data. Keep your application state synchronized with the database.


## What Molecule is Not

Molecule is not a complete database facade or SQL replacement. It focuses on type-safe queries and transactions from your domain model. Outside its scope:

- Administrative operations (creating indexes, connection pooling)
- Advanced SQL features (subqueries, window functions, CTEs)
- Database migrations and schema management
- Direct JDBC operations

For administrative tasks, use other SQL libraries or JDBC directly alongside Molecule. For advanced SQL features, Molecule provides fallback [rawQuery](/database/query/raw-query) and [rawTransact](/database/transaction/raw-transact) methods that let you execute raw SQL when needed—you just lose type safety for those specific queries.


