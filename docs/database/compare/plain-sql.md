# Plain SQL

Plain-SQL libraries allow you to write SQL directly, typically through string interpolators or lightweight wrappers, with minimal abstraction.

1. [Quick Comparison](#_1-quick-comparison) - CRUD operations side-by-side
2. [Environment](#_2-environment) - Scala versions, APIs, code generation
3. [Supported databases](#_3-supported-databases) - Database compatibility
4. [Data types](#_4-data-types) - Supported Scala types
5. [Query](#_5-query) - Query features and examples
6. [Result parsing](#_6-result-parsing) - Parsing results to tuples and case classes
7. [Transaction](#_7-transaction) - Transaction features
8. [Transaction setup](#_8-transaction-setup) - How to use transactions
9. [When to choose what?](#_9-when-to-choose-what) - Decision guide


## 1. Quick Comparison

Here's a side-by-side comparison of basic operations with Molecule and Doobie:

::: code-tabs#quick
@tab Query
```scala
// Molecule - domain model query
Person.name.age.Address.street.query.get

// Plain SQL
sql"""SELECT p.name, p.age, a.street
      FROM person p
      JOIN address a ON p.address_id = a.id"""
  .query[(String, Int, String)]
  .to[List]
```

@tab Insert
```scala
// Molecule - type-safe insert
Person.name("Alice").age(30).save

// Plain SQL
sql"INSERT INTO person(name, age) VALUES ('Alice', 30)".update.run
```

@tab Update
```scala
// Molecule - update by id
Person(personId).age(31).update

// Plain SQL
sql"UPDATE person SET age = 31 WHERE id = $personId".update.run
```

@tab Delete
```scala
// Molecule - delete by id
Person(personId).delete

// Plain SQL
sql"DELETE FROM person WHERE id = $personId".update.run
```
:::


## 2. Environment


|                            |          Molecule          | [Doobie] | [Magnum] |    [Skunk]    |   [Anorm]    |
|----------------------------|:--------------------------:|:--------:|:--------:|:-------------:|:------------:|
| Scala 3                    |             ✅              |    ✅     |    ✅     |       ✅       |      ✅       |
| Scala 2.13                 |                            |    ✅     |          |       ✅       |      ✅       |
| Scala 2.12                 |                            |    ✅     |          |       ✅       |      ✅       |
| Scala JS                   |             ✅              |          |          |  ✅ (Node.js)  |              |
| Scala Native               |                            |          |          |       ✅       |              |
| Model                      |          runtime           | runtime  | runtime  |    runtime    |   compile    |
| API                        | Sync<br>Async<br>ZIO<br>IO |   F[_]   |   Sync   |     F[_]      |     Sync     |
| Underlying api             |            JDBC            |   JDBC   |   JDBC   | Postgres wire |     JDBC     |
| Code generation            |             ✅              |          |          |               | ✅<br>(macro) |
| Repository pattern support |                            |          |    ✅     |               |              |
| Db compliance SPI          |            ✅ 1)            |          |          |               |              |

1) Molecule guarantees that all supported databases behave identically for the end-user. Each supported database pass the same comprehensive compliance SPI test suite of ~2000 tests.


## 3. Supported databases

|            | Molecule | Doobie | Magnum | Skunk | Anorm |
|------------|:--------:|:------:|:------:|:-----:|:-----:|
| Postgres   |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| MySQL      |    ✅     |   ✅    |   ✅    |   ❌   |   ✅   |
| H2         |    ✅     |   ✅    |   ✅    |   ❌   |   ✅   |
| SQLite     |    ✅     |   ✔    |   ✔    |   ❌   |   ✅   |
| MariaDB    |    ✅     |   ✔    |   ✔    |   ❌   |   ✔   |
| Oracle     |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| SQL Server |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| DB2        |    -     |   ✔    |   ✔    |   ❌   |   ✔   |
| HSQLDB     |    -     |   ✔    |   ✅    |   ❌   |   ✔   |
| Derby      |    -     |   ✔    |   ✔    |   ❌   |   ✔   |
| OrientDB   |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Cassandra  |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Spark      |    -     |   ✔    |   ✔    |   ❌   |   ✅   |
| Clickhouse |    -     |   ✔    |   ✅    |   ❌   |   ✔   |

✅ = Fully supported and tested<br>
✔ = JDBC-compatible; should work<br>
\- = Can be implemented<br>
❌ = Not supported


## 4. Data types

|                                                                   | Molecule | Doobie | Magnum | Skunk | Anorm |
|-------------------------------------------------------------------|:--------:|:------:|:------:|:-----:|:-----:|
| _**Scalars**_                                                     |          |        |        |       |       |
| String                                                            |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Int                                                               |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Long                                                              |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Float                                                             |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Double                                                            |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Boolean                                                           |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| BigInt                                                            |    ✅     |        |        |   ✔   |       |
| BigDecimal                                                        |    ✅     |   ✅    |   ✅    |   ✔   |       |
| Byte                                                              |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Short                                                             |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Char                                                              |    ✅     |        |        |   ✅   |       |
| Enum                                                              |    ✅     |   ✅    |        |   ✅   |       |
|                                                                   |          |        |        |       |       |
| _**Java types**_                                                  |          |        |        |       |       |
| java.util.Date                                                    |    ✅     |   ✅    |   ✅    |   ✔   |   ✅   |
| java.util.UUID                                                    |    ✅     |   ✅    |   ✅    |   ✅   |       |
| java.net.URI                                                      |    ✅     |        |        |   ✔   |       |
| java.time.Duration                                                |    ✅     |        |        |   ✅   |       |
| java.time.Instant                                                 |    ✅     |   ✅    |   ✅    |   ✔   |       |
| java.time.LocalDate                                               |    ✅     |   ✅    |   ✅    |   ✅   |       |
| java.time.LocalTime                                               |    ✅     |   ✅    |   ✅    |   ✅   |       |
| java.time.LocalDateTime                                           |    ✅     |   ✅    |   ✅    |   ✅   |       |
| java.time.OffsetTime                                              |    ✅     |   ✅    |        |   ✅   |       |
| java.time.OffsetDateTime                                          |    ✅     |   ✅    |        |   ✅   |       |
| java.time.ZonedDateTime                                           |    ✅     |        |        |   ✔   |       |
|                                                                   |          |        |        |       |       |
| _[Collections](/database/query/attributes.html#collection-types)_ |          |        |        |       |       |
| Set                                                               |    ✅     |        |        |       |       |
| Seq                                                               |    ✅     |        |        |       |       |
| Map                                                               |    ✅     |        |        |       |       |

✅ = Out-of-the-box available Scala type<br>
✔ = Possible with custom Codec<br>


## 5. Query

| Supported feature           | Molecule | Doobie | Magnum | Skunk | Anorm |
|-----------------------------|:--------:|:------:|:------:|:-----:|:-----:|
| Type-safe query             |    ✅     |        |        |       |       |
| Type-safe filtering         |    ✅     |        |        |       |       |
| Type-safe result            |    ✅     |   ✅    |   ✅    |   ✅   |       |
| Nested collections          |    ✅     |        |        |       |       |
|                             |          |        |        |       |       |
| Joins                       |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Conditional filters (WHERE) |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Sorting                     |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Parameterized queries       |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
|                             |          |        |        |       |       |
| Computed expressions        |          |   ✅    |   ✅    |   ✅   |   ✅   |
| Group by                    |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Having                      |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Window functions            |          |   ✅    |   ✅    |   ✅   |   ✅   |
| Set operations (union etc)  |          |   ✅    |   ✅    |   ✅   |   ✅   |
|                             |          |        |        |       |       |
| Subqueries                  |          |   ✅    |   ✅    |   ✅   |   ✅   |
| Fragments/query composition |          |   ✅    |        |   ✅   |       |
|                             |          |        |        |       |       |
| Offset pagination           |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Cursor pagination           |    ✅     |        |        |       |       |
|                             |          |        |        |       |       |
| fs2 streaming               |    ✅     |   ✅    |        |   ✅   |       |
| ZStream                     |    ✅     |        |        |       |       |
| Subscription (Pub/Sub)      |    ✅     |        |        |   ✅   |       |
|                             |          |        |        |       |       |
| Tuples returned             |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
| Mapping to case class       |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |

#### Example query

"Departments with more than 2 employees assigned to projects with a budget exceeding 1M"

Molecule:
```scala
Department.name.Employees.id_(countDistinct).>(2).d1.Projects.budget_.>(1000000)
```

SQL:
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

## 6. Result parsing

Different libraries require different amounts of boilerplate for parsing query results into Scala types.

### Parsing to tuples

**Molecule** - Automatic mapping with no parser definitions:
```scala
Person.name.age.query.get
// Returns List[(String, Int)] automatically
```

**Doobie** - Type annotation with automatic parsing:
```scala
sql"SELECT name, age FROM person"
  .query[(String, Int)]  // Type specified, parser inferred
  .to[List]
```

**Magnum** - Type annotation with automatic parsing:
```scala
sql"SELECT name, age FROM person"
  .query[(String, Int)]  // Type specified, parser inferred
  .run()
```

**Skunk** - Explicit codec definition required:
```scala
// Define decoder with codecs
val decoder = (text ~ int4).map { case (n, a) => (n, a) }

sql"SELECT name, age FROM person"
  .query(decoder)
  .stream(...)
```

**Anorm** - Manual parser definition required:
```scala
// Define row parser upfront
private val parser: RowParser[(String, Int)] =
  (SqlParser.str("name") ~ SqlParser.int("age")).map {
    case n ~ a => (n, a)
  }

SQL"SELECT name, age FROM person"
  .as(parser.*)
```

### Parsing to case classes

```scala
case class Person(name: String, age: Int)
```

**Molecule** - Automatic mapping with no parser definitions:
```scala
Person.name.age.query.get.map(Person.tupled)
// or use a custom transformer for direct case class mapping
```

**Doobie** - Type annotation with automatic parsing:
```scala
sql"SELECT name, age FROM person"
  .query[Person]  // Case class mapping automatic
  .to[List]
```

**Magnum** - Type annotation with automatic parsing:
```scala
sql"SELECT name, age FROM person"
  .query[Person]  // Case class mapping automatic
  .run()
```

**Skunk** - Explicit codec definition:
```scala
val decoder: Decoder[Person] = (text ~ int4).map {
  case (n, a) => Person(n, a)
}

sql"SELECT name, age FROM person"
  .query(decoder)
  .stream(...)
```

**Anorm** - Manual parser definition:
```scala
private val parser: RowParser[Person] =
  (SqlParser.str("name") ~ SqlParser.int("age")).map {
    case n ~ a => Person(n, a)
  }

SQL"SELECT name, age FROM person"
  .as(parser.*)
```


## 7. Transaction

|                                             | Molecule | Doobie | Magnum | Skunk | Anorm |
|---------------------------------------------|:--------:|:------:|:------:|:-----:|:-----:|
| _**Transaction safety <sup>1)</sup>**_      |          |        |        |       |       |
| Type-safe structure (entities/attributes)   |    ✅     |        |        |       |       |
| Type-safe literal values                    |    ✅     |        |        |       |       |
| Type-safe interpolated values               |    ✅     |   ✅    |   ✅    |   ✅   |   ✅   |
|                                             |          |        |        |       |       |
| _**Transaction management**_                |          |        |        |       |       |
| Automatic rollback on failure               |    ✅     |   ✅    |   ✅    |   ✅   |       |
| Explicit rollback (programmatic)            |    ✅     |   ✅    |        |   ✅   |       |
| Savepoints                                  |    ✅     |   ✅    |        |   ✅   |       |
| Unit of work (batched operations as one tx) |    ✅     |   ✅    |   ✅    |       |   ✅   |
| Connection/resource safety within tx        |    ✅     |   ✅    |   ✅    |   ✅   |       |
|                                             |          |        |        |       |       |
| _**Multi-entity operations**_               |          |        |        |       |       |
| Join/table-spanning mutations <sup>2)</sup> |    ✅     |        |        |       |       |
| Nested hierarchical inserts <sup>3)</sup>   |    ✅     |        |        |       |       |

1) **Transaction safety** - Molecule provides three levels of compile-time type safety:

```scala
val name = "Alice"
val age = 30

// Molecule - all three levels of type safety
// ✓ structure, literals, and interpolated values all type-safe
Person.name(name).age(age).save
// ✗ compile error: invalidAttr doesn't exist
Person.invalidAttr(name).save
// ✗ compile error: String doesn't match Int type
Person.name(name).age("30").save

// Plain SQL - only interpolated values are type-safe
// ✓ interpolated values type-safe
sql"INSERT INTO person(name, age) VALUES ($name, $age)".update.run
// ✓ compiles, runtime error (invalid column)
sql"INSERT INTO person(name, invalid_col) VALUES ($name, $age)".update.run
// ✓ compiles, runtime error (wrong type)
sql"INSERT INTO person(name, age) VALUES ($name, '30')".update.run
```

2) Update or delete across multiple related entities with joins in a single operation:
```scala
// Molecule - update employees and their projects in one operation
Person.name_("Alice").salary(130000).Project.budget(1600000).update

// Plain SQL - requires multiple statements
sql"""UPDATE project SET budget = 1600000
      WHERE id IN (SELECT project_id FROM employee WHERE name = 'Alice')"""
  .update.run
sql"UPDATE employee SET salary = 130000 WHERE name = 'Alice'"
  .update.run
```

3) Insert parent and child entities together in one operation:
```scala
// Molecule - insert multiple invoices with nested invoice lines in one call
Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).insert(
  // Invoice 1
  (1, List(
    // Invoice lines for invoice 1
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),

  // Invoice 2
  (2, List(
    (1, "Knife", 40, 50),
    (4, "Bread", 10, 40),
  ))
).transact

// Plain SQL - requires separate inserts with manual foreign key coordination
for {
  // Insert invoice 1
  inv1Id <- sql"INSERT INTO invoice(no) VALUES (1) RETURNING id"
              .query[Int].unique
  _      <- sql"""INSERT INTO invoice_line(
                    qty, product, unit_price, line_total, invoice_id)
                  VALUES
                    (2, 'Socks', 15, 30, $inv1Id),
                    (5, 'Bread', 10, 50, $inv1Id)"""
              .update.run

  // Insert invoice 2
  inv2Id <- sql"INSERT INTO invoice(no) VALUES (2) RETURNING id"
              .query[Int].unique
  _      <- sql"""INSERT INTO invoice_line(
                    qty, product, unit_price, line_total, invoice_id)
                  VALUES
                    (1, 'Knife', 40, 50, $inv2Id),
                    (4, 'Bread', 10, 40, $inv2Id)"""
              .update.run
} yield ()
```


## 8. Transaction setup

How to execute multiple operations in a transaction:

**Molecule** - Call `.transact` on the operation:
```scala
given Conn = ... // Molecule connection

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

// Or, when no coordination is needed:
transact(
  Entity.int(1).save,         // List(1)
  Entity.int.insert(2, 3),    // List(1, 2, 3)
  Entity(1).delete,           // List(2, 3)
  Entity(3).int.*(10).update, // List(2, 30)
)
```

**Doobie** - Build a `ConnectionIO` and call `.transact(xa)`:
```scala
val xa: Transactor[IO] = ... // Doobie transactor

// Single operation
sql"INSERT INTO person(name, age) VALUES ('Alice', 30)"
  .update.run.transact(xa)

// Multiple operations in a transaction
val program: ConnectionIO[Unit] = for {
  p1 <- sql"""INSERT INTO project(name, budget)
              VALUES ('Project X', 100000)"""
          .update.withUniqueGeneratedKeys[Int]("id")
  _  <- sql"""INSERT INTO employee(name, salary, project_id)
              VALUES ('Alice', 80000, $p1), ('Bob', 90000, $p1)"""
          .update.run
} yield ()

program.transact(xa)
```

**Magnum** - Use `transactor().transact` block:
```scala
val transactor: Transactor = ... // Magnum transactor

// Single operation
transactor.transact {
  sql"INSERT INTO person(name, age) VALUES ('Alice', 30)"
    .update.run()
}

// Multiple operations in a transaction
transactor.transact { // Provides implicit DbTx
  val p1 = sql"""INSERT INTO project(name, budget)
                 VALUES ('Project X', 100000)
                 RETURNING id"""
             .query[Int].run().head
  sql"""INSERT INTO employee(name, salary, project_id)
        VALUES ('Alice', 80000, $p1), ('Bob', 90000, $p1)"""
    .update.run()
}
```

**Skunk** - Use `session.transaction` block:
```scala
val session: Session[IO] = ... // Skunk session

// Single operation
val insertPerson: Command[String ~ Int] =
  sql"INSERT INTO person(name, age) VALUES ($text, $int4)".command

session.transaction.use { _ =>
  session.prepare(insertPerson).flatMap(_.execute("Alice" ~ 30))
}

// Multiple operations in a transaction - define commands
val insertProject: Query[String ~ Int, Int] =
  sql"INSERT INTO project(name, budget) VALUES ($text, $int4) RETURNING id"
    .query(int4)

val insertEmployees: Command[List[(String, Int, Int)]] = {
  val enc = (text ~ int4 ~ int4).values.list(2)
  sql"INSERT INTO employee(name, salary, project_id) VALUES $enc".command
}

session.transaction.use { _ =>
  for {
    p1 <- session.prepare(insertProject).flatMap(_.unique("Project X" ~ 100000))
    _  <- session.prepare(insertEmployees).flatMap(
            _.execute(List(("Alice", 80000, p1), ("Bob", 90000, p1)))
          )
  } yield ()
}
```

**Anorm** - Operations within a connection block are transactional:
```scala
val dataSource: DataSource = ... // JDBC DataSource

// Single operation
Manager { use =>
  implicit val c: Connection = use(dataSource.getConnection)
  SQL"INSERT INTO person(name, age) VALUES ('Alice', 30)".execute()
}

// Multiple operations in a transaction
Manager { use =>
  implicit val c: Connection = use(dataSource.getConnection)

  val p1 = SQL"""INSERT INTO project(name, budget)
                 VALUES ('Project X', 100000)"""
             .executeInsert().map(_.toInt).get

  SQL"""INSERT INTO employee(name, salary, project_id)
        VALUES ('Alice', 80000, $p1), ('Bob', 90000, $p1)"""
    .execute()
}
```


## 9. When to choose what?

### Choose Molecule if you value:

- **Type safety throughout** - Compile-time validation of your entire data model (entities, attributes, relationships, and values)
- **Domain modeling** - Work with Scala types and your business domain rather than SQL syntax
- **Productivity** - Write less boilerplate code for common operations
- **Multi-entity operations** - Update/insert across related entities in single operations
- **Database portability** - Switch databases without rewriting queries
- **Team accessibility** - No SQL expertise required for most operations

### Choose a plain-SQL library if you need:

- **Full SQL control** - Direct access to all SQL features and optimizations
- **Advanced SQL** - Window functions, CTEs, complex subqueries, computed expressions
- **Performance tuning** - Fine-grained query optimization and database-specific features
- **Existing SQL skills** - Leverage team'\''s SQL expertise
- **Gradual adoption** - Easy integration into existing SQL-based projects
- **SQL tooling** - Use standard SQL development tools and query analyzers

### Both approaches support:

- Type-safe parameter interpolation
- Transaction management with rollback
- Connection pooling and resource safety
- Streaming large result sets
- Mapping results to case classes


[Doobie]: https://typelevel.org/doobie/index.html
[Magnum]: https://github.com/AugustNagro/magnum
[Skunk]: https://typelevel.org/skunk/ 
[Anorm]: https://playframework.github.io/anorm/