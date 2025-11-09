# SQL-like DSL

SQL-like DSL libraries offer a type-safe query syntax that resembles SQL, providing compile-time safety while maintaining SQL-like expressiveness.

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

|                    |          Molecule          |  [ScalikeJDBC]   |   [Typo]   |  [ldbc]  | [Squeryl] | [JOOQ]  |
|--------------------|:--------------------------:|:----------------:|:----------:|:--------:|:---------:|:-------:|
| Scala 3            |             ✅              |        ✅         |     ✅      |    ✅     |           |         |
| Scala 2.13         |                            |        ✅         |     ✅      |          |     ✅     |         |
| Scala 2.12         |                            |        ✅         |            |          |     ✅     |         |
| Scala 2.11         |                            |        ✅         |            |          |     ✅     |         |
| Java               |                            |                  |            |          |           |    ✅    |
| Scala JS           |             ✅              |                  |            |   ✔ 1)   |           |         |
| Scala Native       |                            |                  |            |   ✔ 1)   |           |         |
| Model              |          runtime           |     runtime      |  runtime   | runtime  |  runtime  | compile |
| API                | Sync<br>Async<br>ZIO<br>IO | Sync<br/>pure FP |    Sync    |    IO    |   Sync    |  Sync   |
| DSL                |     Domain navigation      |     SQL-like     | Repository | SQL-like | SQL-like  | SQL 1-1 |
| Raw SQL fallback   |             ✅              |        ✅         |            |    ✅     |     ✅     |    ✅    |
| Code generation    |             ✅              |                  |     ✅      |          |           |    ✅    |
| Repository methods |                            |                  |     ✅      |          |           |         |
| Db compliance SPI  |            ✅ 2)            |                  |            |          |           |         |

1) Compiles to JS/Native, but no examples of use provided.
2) Molecule guarantees that all supported databases behave identically for the end-user. Each supported database pass the same comprehensive compliance SPI test suite of ~2000 tests.


## 2. Supported databases

|            | Molecule | ScalikeJDBC | Typo | ldbc | Squeryl | JOOQ |
|------------|:--------:|:-----------:|:----:|:----:|:-------:|:----:|
| Postgres   |    ✅     |      ✅      |  ✅   |  ✔   |    ✅    |  ✅   |
| MySQL      |    ✅     |      ✅      |      |  ✅   |    ✅    |  ✅   |
| H2         |    ✅     |      ✅      |      |  ✔   |    ✅    |  ✅   |
| SQLite     |    ✅     |      ✅      |      |  ✔   |         |  ✅   |
| MariaDB    |    ✅     |      ✅      |      |  ✔   |         |  ✅   |
| Oracle     |          |      ✅      |      |  ✔   |    ✅    |  ✅   |
| SQL Server |          |      ✅      |      |  ✔   |    ✅    |  ✅   |
| DB2        |          |      ✅      |      |  ✔   |    ✅    |  ✅   |
| HSQLDB     |          |      ✅      |      |  ✔   |         |  ✅   |
| Derby      |          |      ✅      |      |  ✔   |    ✅    |  ✅   |

✅ = Fully supported and tested<br>
✔ = JDBC-compatible; should work<br>
\- = Can be implemented


## 3. Data types

|                                                                   | Molecule | ScalikeJDBC | Typo | ldbc | Squeryl | JOOQ |
|-------------------------------------------------------------------|:--------:|:-----------:|:----:|:----:|:-------:|:----:|
| _**Scalars**_                                                     |          |             |      |      |         |      |
| String                                                            |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Int                                                               |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Long                                                              |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Float                                                             |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Double                                                            |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Boolean                                                           |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| BigInt                                                            |    ✅     |      ✅      |  ✅   |  ✔   |    ✅    |  ✅   |
| BigDecimal                                                        |    ✅     |      ✅      |  ✅   |  ✔   |    ✅    |  ✅   |
| Byte                                                              |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Short                                                             |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Char                                                              |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Enum                                                              |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
|                                                                   |          |             |      |      |         |      |
| _**Java types**_                                                  |          |             |      |      |         |      |
| java.util.Date                                                    |    ✅     |      ✅      |  ✅   |  ✔   |    ✅    |  ✅   |
| java.util.UUID                                                    |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| java.net.URI                                                      |    ✅     |             |      |  ✔   |         |  ✅   |
| java.time.Duration                                                |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.Instant                                                 |    ✅     |      ✅      |  ✅   |  ✔   |         |  ✅   |
| java.time.LocalDate                                               |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.LocalTime                                               |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.LocalDateTime                                           |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.OffsetTime                                              |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.OffsetDateTime                                          |    ✅     |      ✅      |  ✅   |  ✅   |         |  ✅   |
| java.time.ZonedDateTime                                           |    ✅     |      ✅      |  ✅   |  ✔   |         |  ✅   |
|                                                                   |          |             |      |      |         |      |
| _[Collections](/database/query/attributes.html#collection-types)_ |          |             |      |      |         |      |
| Set                                                               |    ✅     |             |      |      |         |      |
| Seq                                                               |    ✅     |             |      |      |         |      |
| Map                                                               |    ✅     |             |      |      |         |      |

✅ = Out-of-the-box available Scala type<br>
✔ = Possible with custom mapping/codec<br>


## 4. Schema Definition

SQL-DSL libraries take different approaches to schema definition: some generate code from SQL schemas (Typo, JOOQ), while others define schemas directly in Scala/Java code at runtime (ScalikeJDBC, ldbc, Squeryl). Molecule takes a unique approach by generating code from a Domain Structure. This section shows each approach:

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

@tab ScalikeJDBC
```scala
// Case classes with SQLSyntaxSupport
case class Person(
  id: Long,
  name: String,
  age: Int,
  addressId: Long
)

case class Address(
  id: Long,
  street: String,
)

// Companion objects for table mapping
object Person extends SQLSyntaxSupport[Person] {
  override val tableName = "person"
}

object Address extends SQLSyntaxSupport[Address] {
  override val tableName = "address"
}
```

@tab Typo
User defines SQL schema:
```sql
-- Write PostgreSQL schema
CREATE TABLE person (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(255) NOT NULL,
  age        INT NOT NULL,
  address_id BIGINT REFERENCES address(id)
);

CREATE TABLE address (
  id     BIGSERIAL PRIMARY KEY,
  street VARCHAR(255) NOT NULL
);
```

Typo then generates case classes and repository methods:
```scala
case class PersonRow(
  id: Long,
  name: String,
  age: Int,
  addressId: Long
)

trait PersonRepo {
  def insert(row: PersonRow): ConnectionIO[PersonRow]
  def selectById(id: Long): ConnectionIO[Option[PersonRow]]
  // ... more CRUD methods
}
```

@tab ldbc
```scala
// Case classes with Table derivation
case class Person(
  id: Long,
  name: String,
  age: Int,
  addressId: Long
)derives Table

case class Address(
  id: Long,
  street: String
)derives Table

// Or explicit table definition
class PersonTable extends Table[Person]("person"):
  def id: Column[Long] = column[Long]("id")
  def name: Column[String] = column[String]("name")
  def age: Column[Int] = column[Int]("age")
  def addressId: Column[Long] = column[Long]("address_id")
  override def * : Column[Person] = (id *: name *: age *: addressId).to[Person]
```

@tab Squeryl
```scala
// Case classes and Schema definition
case class Person(
  id: Long,
  name: String,
  age: Int,
  addressId: Long
)

case class Address(
  id: Long,
  street: String
)

// Schema object with table definitions
object MySchema extends Schema {
  val persons   = table[Person]("person")
  val addresses = table[Address]("address")

  // Optional: define relationships
  val personToAddress = oneToManyRelation(persons, addresses)
    .via((p, a) => p.id === a.addressId)
}
```

@tab JOOQ
User defines SQL schema:
```sql
-- Write database schema (any SQL dialect)
CREATE TABLE person (
  id         BIGINT PRIMARY KEY,
  name       VARCHAR(255) NOT NULL,
  age        INT NOT NULL,
  address_id BIGINT,
  FOREIGN KEY (address_id) REFERENCES address(id)
);

CREATE TABLE address (
  id     BIGINT PRIMARY KEY,
  street VARCHAR(255) NOT NULL
);
```

JOOQ then generates Java boilerplate code (simplified):
```java
public class Person extends TableImpl<PersonRecord> {
    public final TableField<PersonRecord, Long> ID = ...;
    public final TableField<PersonRecord, String> NAME = ...;
    public final TableField<PersonRecord, Integer> AGE = ...;
    public final TableField<PersonRecord, Long> ADDRESS_ID = ...;
}

public class PersonRecord extends UpdatableRecordImpl<PersonRecord> {
    public void setId(Long value) { ...}

    public Long getId() { ...}
    // ... getters/setters for all fields
}
```
:::


## 5. Query features

| Supported feature           | Molecule | ScalikeJDBC | Typo | ldbc | Squeryl | JOOQ |
|-----------------------------|:--------:|:-----------:|:----:|:----:|:-------:|:----:|
| Type-safe query             |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Type-safe filtering         |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Type-safe result            |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Nested collections          |    ✅     |             |      |      |         |      |
|                             |          |             |      |      |         |      |
| Joins                       |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Conditional filters (WHERE) |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Sorting                     |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Parameterized queries       |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
|                             |          |             |      |      |         |      |
| Computed expressions        |          |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Group by                    |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Having                      |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Window functions            |          |             |  ✅   |      |         |  ✅   |
| Set operations (union etc)  |          |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
|                             |          |             |      |      |         |      |
| Subqueries                  |          |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Fragments/query composition |          |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
|                             |          |             |      |      |         |      |
| Offset pagination           |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Cursor pagination           |    ✅     |             |      |      |         |      |
|                             |          |             |      |      |         |      |
| fs2 streaming               |    ✅     |             |      |      |         |      |
| ZStream                     |    ✅     |             |      |      |         |      |
|                             |          |             |      |      |         |      |
| Tuples returned             |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Mapping to case class       |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |


## 6. Query Examples

This section shows how the same query looks with Molecule and the various SQL-like DSL libraries:

**Molecule** - Data composition of domain attributes
```scala
val people: List[(String, Int, String)] =
  Person.name.age.>(25).a1.Address.street.query.get
```

**ScalikeJDBC** - SQL-like DSL with explicit syntax objects
```scala
val (p, a) = (Person.syntax("p"), Address.syntax("a"))

val people: List[(Person, Address)] =
  withSQL {
    select(p.result.*, a.result.*)
      .from(Person as p)
      .innerJoin(Address as a).on(p.addressId, a.id)
      .where.gt(p.age, 25)
      .orderBy(p.name)
  }.map { rs =>
    // Manual mapping from ResultSet
    (Person(p)(rs), Address(a)(rs))
  }.list.apply()
```

**Typo** - Generated repository with query methods
```scala
val people: ConnectionIO[List[(PersonRow, AddressRow)]] =
  personRepo.select
    .join(addressRepo.select)
    .on((p, a) => p.addressId === a.id)
    .where((p, a) => p.age > 25)
    .orderBy((p, a) => p.name.asc)
    .toList
// Automatic mapping to generated row types
```

**ldbc** - Functional query builder with type-safe DSL
```scala
val personTable  = TableQuery[Person]
val addressTable = TableQuery[Address]

val people: IO[List[(Person, Address)]] =
  personTable
    .join(addressTable)
    .on(_.addressId === _.id)
    .where(_._1.age > 25)
    .orderBy(_._1.name.asc)
    .query
    .to[List]
    .readOnly(connector)
// Automatic mapping - no manual ResultSet handling
```

**Squeryl** - Scala-integrated query DSL
```scala
val people: List[(Person, Address)] =
  from(MySchema.persons, MySchema.addresses)((p, a) =>
    where(p.age > 25 and p.addressId === a.id)
      select ((p, a))
      orderBy (p.name asc)
  ).toList
// Automatic case class mapping
```

**JOOQ** - Java DSL with 1-1 SQL mapping
```scala
val people: List[Record] =
  dsl.select(PERSON.NAME, PERSON.AGE, ADDRESS.STREET)
    .from(PERSON)
    .join(ADDRESS)
    .on(PERSON.ADDRESS_ID.eq(ADDRESS.ID))
    .where(PERSON.AGE.gt(25))
    .orderBy(PERSON.NAME.asc())
    .fetch()
    .asScala
    .toList
// Requires .get(FIELD) calls or explicit mapping to extract values
```


## 7. Transaction features

|                                             | Molecule | ScalikeJDBC | Typo | ldbc | Squeryl | JOOQ |
|---------------------------------------------|:--------:|:-----------:|:----:|:----:|:-------:|:----:|
| _**Transaction safety**_                    |          |             |      |      |         |      |
| Type-safe structure (entities/attributes)   |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Type-safe literal values                    |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
| Type-safe interpolated values               |    ✅     |      ✅      |  ✅   |  ✅   |    ✅    |  ✅   |
|                                             |          |             |      |      |         |      |
| _**Transaction management**_                |          |             |      |      |         |      |
| Automatic rollback on failure               |    ✅     |      ✅      |  ✅   |      |    ✅    |  ✅   |
| Explicit rollback (programmatic)            |    ✅     |             |  ✅   |      |         |  ✅   |
| Savepoints                                  |    ✅     |             |  ✅   |      |         |  ✅   |
| Unit of work (batched operations as one tx) |    ✅     |      ✅      |  ✅   |      |    ✅    |  ✅   |
| Connection/resource safety within tx        |    ✅     |      ✅      |  ✅   |      |    ✅    |  ✅   |
|                                             |          |             |      |      |         |      |
| _**Multi-entity operations**_               |          |             |      |      |         |      |
| Join/table-spanning mutations <sup>1)</sup> |    ✅     |             |      |      |         |      |
| Nested hierarchical inserts <sup>2)</sup>   |    ✅     |             |      |      |         |      |

1) Update or delete across multiple related entities with joins in a single operation:
```scala
// Molecule - update employees and their projects in one operation
Person.name_("Alice").salary(130000).Project.budget(1600000).update

// SQL DSL (ScalikeJDBC example) - requires multiple statements
DB localTx { implicit session =>
  // First, find project IDs for Alice
  val projectIds = withSQL {
    select(sqls"project_id")
      .from(Person as p)
      .where.eq(p.name, "Alice")
  }.map(_.long(1)).list.apply()

  // Update projects
  projectIds.foreach { projectId =>
    withSQL {
      update(Project)
        .set(Project.column.budget -> 1600000)
        .where.eq(Project.column.id, projectId)
    }.update.apply()
  }

  // Update person
  withSQL {
    update(Person)
      .set(Person.column.salary -> 130000)
      .where.eq(Person.column.name, "Alice")
  }.update.apply()
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

// SQL DSL (ScalikeJDBC example) - requires separate inserts with manual foreign key coordination
DB localTx { implicit session =>
  // Insert first invoice
  val inv1Id = withSQL {
    insert.into(Invoice).namedValues(Invoice.column.no -> 1)
  }.updateAndReturnGeneratedKey.apply()

  // Batch insert invoice lines for first invoice
  val lineParams = Seq(
    Seq('qty -> 2, 'product -> "Socks", 'unitPrice -> 15, 'lineTotal -> 30, 'invoiceId -> inv1Id),
    Seq('qty -> 5, 'product -> "Bread", 'unitPrice -> 10, 'lineTotal -> 50, 'invoiceId -> inv1Id)
  )
  sql"insert into invoice_line (qty, product, unit_price, line_total, invoice_id) values ({qty}, {product}, {unitPrice}, {lineTotal}, {invoiceId})"
    .batchByName(lineParams: _*)
    .apply()

  // Insert second invoice and its lines...
}
```


## 8. Transaction examples

How to execute multiple operations in a transaction:

**Molecule** - Call `.transact` on the operation:
```scala
given Conn = //... Molecule connection

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

**ScalikeJDBC** - Use `DB localTx` or `DB withinTx` block:
```scala
implicit val session: DBSession = //... ScalikeJDBC session

// Single operation
DB localTx { implicit session =>
  withSQL {
    insert.into(Person).namedValues(
      Person.column.name -> "Alice",
      Person.column.age -> 30
    )
  }.update.apply()
}

// Multiple operations in a transaction
DB localTx { implicit session =>
  val p1 = withSQL {
    insert.into(Project).namedValues(
      Project.column.name -> "Project X",
      Project.column.budget -> 100000
    )
  }.updateAndReturnGeneratedKey.apply()

  // Batch insert multiple employees
  val batchParams = Seq(
    Seq('name -> "Alice", 'salary -> 80000, 'projectId -> p1),
    Seq('name -> "Bob", 'salary -> 90000, 'projectId -> p1)
  )
  sql"""insert into employee (name, salary, project_id) 
       |  values ({name}, {salary}, {projectId})""".stripMargin
    .batchByName(batchParams: _*)
    .apply()
}
```

**Typo** - Use Doobie's `transact` on ConnectionIO operations:
```scala
implicit val transactor: Transactor[IO] = //... Doobie transactor

// Single operation
personRepo.insert(
  PersonRow(0L, "Alice", 30, None)
).transact(transactor).unsafeRunSync()

// Multiple operations in a transaction
val transaction: ConnectionIO[Unit] = for {
  p1 <- projectRepo.insert(
    ProjectRow(0L, "Project X", 100000)
  )
  // Batch insert multiple employees
  employees = List(
    EmployeeRow(0L, "Alice", 80000, p1.id),
    EmployeeRow(0L, "Bob", 90000, p1.id)
  )
  _ <- employeeRepo.insertStreaming(employees)
} yield ()

transaction.transact(transactor).unsafeRunSync()
```

**ldbc** - Use IO monad with connection:
```scala
implicit val connector: Connector[IO] = //... ldbc connector

// Single operation
sql"INSERT INTO person (name, age) VALUES (${"Alice"}, ${30})"
  .update
  .readOnly(connector)
  .unsafeRunSync()

// Multiple operations in a transaction
val transaction: IO[Unit] = for {
  p1 <- sql"INSERT INTO project (name, budget) VALUES (${"Project X"}, ${100000})"
    .returning[Long]
    .readOnly(connector)
  // Batch insert multiple employees
  employees = List(("Alice", 80000), ("Bob", 90000))
  _ <- employees.traverse { case (name, salary) =>
    sql"INSERT INTO employee (name, salary, project_id) VALUES ($name, $salary, $p1)"
      .update
      .readOnly(connector)
  }
} yield ()

transaction.unsafeRunSync()
```

**Squeryl** - Use `transaction` or `inTransaction` block:
```scala
import org.squeryl.PrimitiveTypeMode._

// Single operation
transaction {
  MySchema.persons.insert(Person(0L, "Alice", 30, None))
}

// Multiple operations in a transaction
transaction {
  val p1 = MySchema.projects.insert(Project(0L, "Project X", 100000))

  // Batch insert multiple employees
  MySchema.employees.insert(Seq(
    Employee(0L, "Alice", 80000, p1.id),
    Employee(0L, "Bob", 90000, p1.id)
  ))
}
```

**JOOQ** - Use `transaction` lambda or manual transaction control:
```scala
val dsl: DSLContext = //... JOOQ DSL context

// Single operation
dsl.transaction((configuration: Configuration) => {
  configuration.dsl()
    .insertInto(PERSON, PERSON.NAME, PERSON.AGE)
    .values("Alice", 30)
    .execute()
})

// Multiple operations in a transaction
dsl.transaction((configuration: Configuration) => {
  val ctx = configuration.dsl()

  val p1 = ctx.insertInto(PROJECT, PROJECT.NAME, PROJECT.BUDGET)
    .values("Project X", 100000)
    .returningResult(PROJECT.ID)
    .fetchOne()
    .getValue(PROJECT.ID)

  // Batch insert multiple employees
  ctx.batch(
    ctx.insertInto(
        EMPLOYEE, 
        EMPLOYEE.NAME, 
        EMPLOYEE.SALARY, 
        EMPLOYEE.PROJECT_ID
      )
      .values(
        null.asInstanceOf[String], 
        null.asInstanceOf[Integer], 
        null.asInstanceOf[Long]
      )
  ).bind("Alice", 80000, p1)
   .bind("Bob", 90000, p1)
   .execute()
})
```


## 9. When to choose what?

### Choose Molecule if you value:

- **Type safety throughout** - Compile-time validation of your entire data model (entities, attributes, relationships, and values)
- **Domain modeling** - Work with Scala types and your business domain rather than database tables
- **Productivity** - Write less boilerplate code for common operations
- **Multi-entity operations** - Update/insert across related entities in single operations
- **Database portability** - Switch databases without rewriting queries
- **Team accessibility** - No SQL expertise required for most operations

### Choose a SQL-DSL library if you need:

- **SQL-like syntax** - Familiar SQL-style query structure for developers with SQL background
- **Advanced SQL** - Window functions, CTEs, complex subqueries, computed expressions
- **Performance tuning** - Fine-grained query optimization and database-specific features
- **Gradual adoption** - Easy integration into existing SQL-based projects
- **Mature ecosystem** - Large community and extensive documentation (especially for ScalikeJDBC)

### Specific library recommendations:

- **ScalikeJDBC** - Best for teams wanting SQL-like DSL with simple sync API and excellent documentation
- **Typo** - Best for teams wanting generated repository code from PostgreSQL schema
- **ldbc** - Best for teams using Scala 3 and wanting pure functional approach with MySQL (pre-1.0)
- **Squeryl** - Best for teams wanting concise Scala-integrated DSL with ORM features and relationship management
- **JOOQ** - Best for teams needing comprehensive SQL feature coverage, Java/Scala interop, and maximum database compatibility

### Both approaches support:

- Type-safe parameter interpolation
- Transaction management with rollback
- Connection pooling and resource safety
- Mapping results to case classes


[ScalikeJDBC]: https://scalikejdbc.org

[ldbc]: https://takapi327.github.io/ldbc/

[Typo]: https://github.com/oyvindberg/typo

[Squeryl]: https://www.squeryl.org

[JOOQ]: https://www.jooq.org