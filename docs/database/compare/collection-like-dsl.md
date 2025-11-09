# Collection-like DSL

Collection-like DSL libraries offer a type-safe query syntax that resembles Scala collections, providing intuitive query composition with compile-time safety.

1. [Environment](#_1-environment) - Scala versions, APIs, code generation
2. [Supported databases](#_2-supported-databases) - Database compatibility
3. [Data types](#_3-data-types) - Supported Scala types
4. [Schema Definition](#_4-schema-definition) - How to define tables and models
5. [Query features](#_5-query-features) - Query features overview
6. [Query examples](#_6-query-examples) - Query examples
7. [Transaction features](#_7-transaction-features) - Transaction features
8. [Transaction examples](#_8-transaction-examples) - Examples of single/multi-entity transactions
9. [When to choose what?](#_9-when-to-choose-what) - Decision guide


## 1. Environment

|                  |          Molecule          |    [ScalaSQL]     | [Slick] | [ProtoQuill] |
|------------------|:--------------------------:|:-----------------:|:-------:|:------------:|
| Scala 3          |             ✅              |         ✅         |    ✅    |      ✅       |
| Scala 2.13       |                            |         ✅         |    ✅    |              |
| Scala 2.12       |                            |                   |    ✅    |              |
| Scala JS         |             ✅              |                   |         |              |
| Scala Native     |                            |                   |         |              |
| Model            |          runtime           |      runtime      | runtime |   compile    |
| API              | Sync<br>Async<br>ZIO<br>IO |       Sync        |  DBIO   |     ZIO      |
| DSL              |                            | Scala<br>col-like |         |              |
| Raw SQL fallback |             ✅              |         ✅         |    ✅    |      ✅       |
| Code generation  |             ✅              |                   |         | ✅<br>(macro) |


## 2. Supported databases

|            | Molecule | Scala<br>SQL | Slick | Proto<br>Quill |
|------------|:--------:|:------------:|:-----:|:--------------:|
| Postgres   |    ✅     |      ✅       |   ✅   |       ✅        |
| MySQL      |    ✅     |      ✅       |   ✅   |       ✔        |
| H2         |    ✅     |      ✅       |   ✅   |       ✅        |
| SQLite     |    ✅     |      ✅       |   ✅   |       ✔        |
| MariaDB    |    ✅     |              |       |                |
| Oracle     |          |              |   ✅   |       ✔        |
| SQL Server |          |              |   ✅   |       ✔        |
| DB2        |          |              |   ✅   |                |
| HSQLDB     |          |              |   ✅   |                |
| Derby      |          |              |   ✅   |                |
| OrientDB   |          |              |       |                |
| Cassandra  |          |              |       |       ✅        |
| Spark      |          |              |       |                |
| Clickhouse |          |              |       |                |

✅ = Supported and tested<br>
&nbsp;✔ &nbsp;= JDBC-compliant database support


## 3. Data types

|                                                                   | Molecule | ScalaSQL | Slick | ProtoQuill |
|-------------------------------------------------------------------|:--------:|:--------:|:-----:|:----------:|
| _**Scalars**_                                                     |          |          |       |            |
| String                                                            |    ✅     |    ✅     |   ✅   |     ✅      |
| Int                                                               |    ✅     |    ✅     |   ✅   |     ✅      |
| Long                                                              |    ✅     |    ✅     |   ✅   |     ✅      |
| Float                                                             |    ✅     |    ✅     |   ✅   |     ✅      |
| Double                                                            |    ✅     |          |   ✅   |     ✅      |
| Boolean                                                           |    ✅     |          |   ✅   |     ✔      |
| BigInt                                                            |    ✅     |          |       |     ✔      |
| BigDecimal                                                        |    ✅     |          |   ✅   |     ✅      |
| Byte                                                              |    ✅     |          |   ✅   |     ✅      |
| Short                                                             |    ✅     |          |   ✅   |     ✅      |
| Char                                                              |    ✅     |          |       |     ✔      |
| Enum                                                              |    ✅     |          |       |     ✔      |
|                                                                   |          |          |       |            |
| _**Java types**_                                                  |          |          |       |            |
| java.util.Date                                                    |    ✅     |    ✅     |       |     ✅      |
| java.util.UUID                                                    |    ✅     |          |   ✅   |     ✔      |
| java.net.URI                                                      |    ✅     |          |       |     ✔      |
| java.time.Duration                                                |    ✅     |          |   ✅   |     ✔      |
| java.time.Instant                                                 |    ✅     |    ✅     |   ✅   |     ✅      |
| java.time.LocalDate                                               |    ✅     |          |   ✅   |     ✅      |
| java.time.LocalTime                                               |    ✅     |          |   ✅   |     ✅      |
| java.time.LocalDateTime                                           |    ✅     |          |   ✅   |     ✅      |
| java.time.OffsetTime                                              |    ✅     |          |   ✅   |     ✅      |
| java.time.OffsetDateTime                                          |    ✅     |          |   ✅   |     ✅      |
| java.time.ZonedDateTime                                           |    ✅     |          |   ✅   |     ✅      |
|                                                                   |          |          |       |            |
| _[Collections](/database/query/attributes.html#collection-types)_ |          |          |       |            |
| Set                                                               |    ✅     |          |       |            |
| Seq                                                               |    ✅     |          |       |            |
| Map                                                               |    ✅     |          |       |            |

✅ = Out-of-the-box available Scala type<br>
✔ = Possible with custom mapping/codec<br>


## 4. Schema Definition

Collection-like DSL libraries define schemas directly in Scala code at runtime using case classes and table mappings. Molecule takes a unique approach by generating code from a Domain Structure. This section shows each approach:

::: tabs#schema

@tab Molecule

User defines DomainStructure with one trait for each table
```scala
object People extends DomainStructure {
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

Molecule then generates type-safe DSL (simplified):
```scala
object Person extends Person_0 {
  // Attributes for building queries
  lazy val name   : Person_1[String]  = //...
  lazy val age    : Person_1[Int]     = //...
  lazy val address: Person_1[Address] = //...

  // Tacit versions (for filtering without returning)
  lazy val name_ : Person_0 = //...
  lazy val age_  : Person_0 = //...
  // ... and many more query construction methods
}
```

@tab ScalaSQL

```scala
// Case classes with Table companion object
case class Person[T[_]](
  id: T[Int],
  name: T[String],
  age: T[Int],
  addressId: T[Int]
)

case class Address[T[_]](
  id: T[Int],
  street: T[String]
)

// Companion objects extending Table
object Person extends Table[Person]
object Address extends Table[Address]

// Alternative: SimpleTable for Scala 3.7.0+
case class Person(
  id: Int,
  name: String,
  age: Int,
  addressId: Int
)
object Person extends SimpleTable[Person]
```

@tab Slick

```scala
// Case classes for mapping
case class Person(
  id: Option[Int],
  name: String,
  age: Int,
  addressId: Int
)

case class Address(
  id: Option[Int],
  street: String
)

// Table definitions
class People(tag: Tag) extends Table[Person](tag, "person") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def age = column[Int]("age")
  def addressId = column[Int]("address_id")
  def * = (id.?, name, age, addressId).mapTo[Person]

  // Optional: foreign key
  def address = foreignKey("ADDRESS_FK", addressId, addresses)(_.id)
}

class Addresses(tag: Tag) extends Table[Address](tag, "address") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def street = column[String]("street")
  def * = (id.?, street).mapTo[Address]
}

// TableQuery instances
val people    = TableQuery[People]
val addresses = TableQuery[Addresses]
```

@tab ProtoQuill

```scala
// Create database context
val ctx = new PostgresJdbcContext(SnakeCase, "ctx")
import ctx._

// Simple case classes - no additional ceremony
case class Person(
  id: Int,
  name: String,
  age: Int,
  addressId: Int
)

case class Address(
  id: Int,
  street: String
)

// ProtoQuill uses Scala 3 inline macros for compile-time queries
// Table names are derived from case class names (can be customized)
inline def people = quote {
  query[Person]
}

inline def addresses = quote {
  query[Address]
}
```

:::


## 5. Query features

| Supported feature           | Molecule | ScalaSQL | Slick | ProtoQuill |
|-----------------------------|:--------:|:--------:|:-----:|:----------:|
| Type-safe query             |    ✅     |    ✅     |   ✅   |     ✅      |
| Type-safe filtering         |    ✅     |    ✅     |   ✅   |     ✅      |
| Type-safe result            |    ✅     |    ✅     |   ✅   |     ✅      |
| Nested collections          |    ✅     |          |       |            |
|                             |          |          |       |            |
| Joins                       |    ✅     |    ✅     |   ✅   |     ✅      |
| Conditional filters (WHERE) |    ✅     |    ✅     |   ✅   |     ✅      |
| Sorting                     |    ✅     |    ✅     |   ✅   |     ✅      |
| Parameterized queries       |    ✅     |    ✅     |   ✅   |     ✅      |
|                             |          |          |       |            |
| Computed expressions        |          |    ✅     |   ✅   |     ✅      |
| Group by                    |    ✅     |    ✅     |   ✅   |     ✅      |
| Having                      |    ✅     |    ✅     |   ✅   |     ✅      |
| Window functions            |          |          |   ✅   |     ✅      |
| Set operations (union etc)  |          |    ✅     |   ✅   |     ✅      |
|                             |          |          |       |            |
| Subqueries                  |          |    ✅     |   ✅   |     ✅      |
| Fragments/query composition |          |    ✅     |   ✅   |     ✅      |
|                             |          |          |       |            |
| Offset pagination           |    ✅     |    ✅     |   ✅   |     ✅      |
| Cursor pagination           |    ✅     |          |       |            |
|                             |          |          |       |            |
| fs2 streaming               |    ✅     |          |       |            |
| ZStream                     |    ✅     |          |       |     ✅      |
| DbApi.stream                |          |    ✅     |       |            |
|                             |          |          |       |            |
| Tuples returned             |    ✅     |    ✅     |   ✅   |     ✅      |
| Mapping to case class       |    ✅     |    ✅     |   ✅   |     ✅      |


## 6. Query examples

This section shows how the same query looks with Molecule and the various collection-like DSL libraries:

**Molecule** - Data composition of domain attributes
```scala
val people: List[(String, Int, String)] =
  Person.name.age.>(25).a1.Address.street.query.get
```

**ScalaSQL** - Collection-like DSL with type-safe operations
```scala
val people: List[(String, Int, String)] =
  db.run(
    Person.select
      .join(Address)(_.addressId === _.id)
      .filter { case (p, a) => p.age > 25 }
      .sortBy(_._1.name)
      .map { case (p, a) => (p.name, p.age, a.street) }
  )
```

**Slick** - Functional Relational Mapping with DBIO
```scala
val people: Future[Seq[(String, Int, String)]] =
  db.run(
    (for {
      (p, a) <- people join addresses on (_.addressId === _.id)
      if p.age > 25
    } yield (p.name, p.age, a.street))
      .sortBy(_._1)
      .result
  )
```

**ProtoQuill** - Compile-time query generation with quotations
```scala
val people: List[(String, Int, String)] = ctx.run {
  quote {
    for {
      p <- query[Person] if p.age > 25
      a <- query[Address] if a.id == p.addressId
    } yield (p.name, p.age, a.street)
  }.sortBy(_._1)
}
```


## 7. Transaction features

|                                             | Molecule | ScalaSQL | Slick | ProtoQuill |
|---------------------------------------------|:--------:|:--------:|:-----:|:----------:|
| _**Transaction safety**_                    |          |          |       |            |
| Type-safe structure (entities/attributes)   |    ✅     |    ✅     |   ✅   |     ✅      |
| Type-safe literal values                    |    ✅     |    ✅     |   ✅   |     ✅      |
| Type-safe interpolated values               |    ✅     |    ✅     |   ✅   |     ✅      |
|                                             |          |          |       |            |
| _**Transaction management**_                |          |          |       |            |
| Automatic rollback on failure               |    ✅     |    ✅     |   ✅   |     ✅      |
| Explicit rollback (programmatic)            |    ✅     |    ✅     |   ✅   |            |
| Savepoints                                  |    ✅     |    ✅     |       |            |
| Unit of work (batched operations as one tx) |    ✅     |    ✅     |   ✅   |     ✅      |
| Connection/resource safety within tx        |    ✅     |    ✅     |   ✅   |     ✅      |
|                                             |          |          |       |            |
| _**Multi-entity operations**_               |          |          |       |            |
| Join/table-spanning mutations <sup>1)</sup> |    ✅     |          |       |            |
| Nested hierarchical inserts <sup>2)</sup>   |    ✅     |          |       |            |

1) Update or delete across multiple related entities with joins in a single operation:
```scala
// Molecule - update employees and their projects in one operation
Person.name_("Alice").salary(130000).Project.budget(1600000).update

// Collection-like DSL (ScalaSQL example) - requires multiple statements
db.transaction { conn =>
  // First, find project IDs for Alice
  val projectIds = Person.select
    .filter(_.name === "Alice")
    .map(_.projectId)
    .run(conn)

  // Update projects
  projectIds.foreach { projectId =>
    Project.update(_.id === projectId)
      .set(_.budget := 1600000)
      .run(conn)
  }

  // Update person
  Person.update(_.name === "Alice")
    .set(_.salary := 130000)
    .run(conn)
}
```

2) Insert parent and child entities together in one operation:
```scala
// Molecule - insert multiple invoices with nested invoice lines in one call
Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).insert(
  (1, List(
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),
  (2, List(
    (1, "Knife", 40, 50),
    (4, "Bread", 10, 40),
  ))
).transact

// Collection-like DSL (ScalaSQL example)
// Requires separate inserts with manual foreign key coordination
db.transaction { conn =>
  // Insert first invoice
  val inv1Id = Invoice.insert
    .columns(_.no := 1)
    .returning(_.id)
    .run(conn)

  // Batch insert invoice lines for first invoice
  InvoiceLine.insert.batched(
    _.invoiceId, _.qty, _.product, _.unitPrice, _.lineTotal
  )(
    (inv1Id, 2, "Socks", 15, 30),
    (inv1Id, 5, "Bread", 10, 50)
  ).run(conn)

  // Insert second invoice and its lines...
}
```


## 8. Transaction examples

How to execute multiple operations in a transaction:

**Molecule** - Call `.transact` on the operation:
```scala
given Conn =
... // Molecule connection

// Single operation
Person.name("Alice").age(30).save.transact

// Multiple operations within unitOfWork
unitOfWork {
  val p1 = Project.name("Project X").budget(100000).save.transact.id
  Employee.name.salary.project.insert(
    ("Alice", 80000, p1),
    ("Bob", 90000, p1),
  ).transact
}
```

**ScalaSQL** - Use `dbClient.transaction` block:
```scala
val dbClient: DbClient =
... // ScalaSQL database client

// Single operation
dbClient.transaction { db =>
  db.run(
    Person.insert.columns(
      _.name := "Alice",
      _.age := 30
    )
  )
}

// Multiple operations in a transaction
dbClient.transaction { db =>
  val p1 = db.run(
    Project.insert.columns(
      _.name := "Project X",
      _.budget := 100000
    ).returning(_.id)
  )

  // Batch insert multiple employees
  db.run(
    Employee.insert.batched(_.name, _.salary, _.projectId)(
      ("Alice", 80000, p1),
      ("Bob", 90000, p1)
    )
  )
}
```

**Slick** - Use `.transactionally` combinator:
```scala
val db: Database =
... // Slick database

// Single operation
db.run(
  people.map(p => (p.name, p.age))
    .insert(("Alice", 30))
    .transactionally
)

// Multiple operations in a transaction
val transaction = (for {
  p1 <- projects.returning(projects.map(_.id))
    .insert(("Project X", 100000))

  // Batch insert multiple employees
  _ <- employees.map(e => (e.name, e.salary, e.projectId))
    .insertAll(
      ("Alice", 80000, p1),
      ("Bob", 90000, p1)
    )
} yield ()).transactionally

db.run(transaction)
```

**ProtoQuill** - Use ZIO transaction with `ctx.transaction`:
```scala
val ctx = new PostgresJdbcContext(SnakeCase, "ctx")
import ctx._

// Single operation
ctx.run(
  quote {
    query[Person].insertValue(lift(Person("Alice", 30)))
  }
)

// Multiple operations in a transaction
ctx.transaction {
  val p1 = ctx.run(
    quote {
      query[Project]
        .insertValue(lift(Project("Project X", 100000)))
        .returningGenerated(_.id)
    }
  )

  // Batch insert multiple employees
  ctx.run(
    quote {
      liftQuery(List(
        Employee("Alice", 80000, p1),
        Employee("Bob", 90000, p1)
      )).foreach(e => query[Employee].insertValue(e))
    }
  )
}
```


## 9. When to choose what?

### Choose Molecule if you value:

- **Type safety throughout** - Compile-time validation of your entire data model (entities, attributes, relationships, and values)
- **Domain modeling** - Work with Scala types and your business domain rather than database tables
- **Productivity** - Write less boilerplate code for common operations
- **Multi-entity operations** - Update/insert across related entities in single operations
- **Database portability** - Switch databases without rewriting queries
- **Nested collections** - Query and return hierarchical data structures naturally
- **Team accessibility** - No SQL expertise required for most operations

### Choose a Collection-like DSL library if you need:

- **Familiar Scala syntax** - Query databases using familiar collection operations (map, filter, flatMap)
- **Composable queries** - Build queries incrementally using standard Scala combinators
- **Type-safe joins** - Explicit join operations with compile-time safety
- **Functional programming** - Pure functional approach to database operations
- **Fine-grained control** - Direct mapping between Scala expressions and SQL queries

### Specific library recommendations:

- **ScalaSQL** - Best for teams wanting a simple, intuitive collection-like API with excellent documentation and savepoint support. Great for Scala 3 projects needing straightforward database access without heavy abstractions.

- **Slick** - Best for teams needing a mature, production-proven library with extensive database support, strong community, and comprehensive documentation. Ideal for projects requiring functional reactive streams and complex query composition.

- **ProtoQuill** - Best for teams using ZIO and wanting compile-time query generation with quotations. Excellent for type-safe queries that are validated at compile time, though with less flexibility for dynamic queries. Good fit for functional Scala 3 applications in the ZIO ecosystem.

### Both approaches support:

- Type-safe query construction
- Type-safe parameter interpolation
- Transaction management with automatic rollback
- Connection pooling and resource safety
- Mapping results to case classes and tuples


[ScalaSql]: https://github.com/com-lihaoyi/scalasql

[Slick]: https://scala-slick.org

[ProtoQuill]: https://github.com/zio/zio-protoquill