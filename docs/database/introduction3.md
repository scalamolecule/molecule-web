
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

[Complex example](/database/relationships/complex-example.html#full-complex-query)

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


## Key Features

**[Authorization](/database/authorization/overview)** — Control data access at the attribute level. Define roles and permissions in your domain structure, and Molecule enforces them automatically in all queries and transactions. No need to scatter security logic throughout your business code.

**[Validation](/database/validation/basics)** — Validate data at insertion and update time with built-in validators or custom validation functions. Molecule ensures data integrity before it reaches the database.

**[Filtering and Aggregations](/database/query/filter)** — Filter data with intuitive operators and aggregate with functions like `count`, `sum`, `avg`, `min`, and `max`. Compose complex queries without leaving your domain language.

**[Sorting and Pagination](/database/query/sort)** — Sort results by any attribute in ascending or descending order. Paginate with offset-based or cursor-based pagination for efficient data loading.

**[Optional and Nested Data](/database/query/optional-nested)** — Query optional attributes and traverse nested relationships naturally. Molecule handles the complexity of outer joins and nested data structures.

**[Subscriptions](/database/subscriptions/basics)** — Subscribe to data changes and receive real-time updates when your queries match new or modified data. Keep your application state synchronized with the database.


## What Molecule Does (and Doesn't Do)

Molecule generates type-safe queries and transactions from your domain model and executes them against SQL databases.

Molecule is **not** a complete database facade. Administrative operations like creating indexes, manual transaction rollbacks, or connection pooling are outside its scope. For those tasks, use other SQL libraries or JDBC directly alongside Molecule.


