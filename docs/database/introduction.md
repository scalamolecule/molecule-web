
# Introduction

<br>

![Molecule logo](/static/img/logo/Molecule-logo-600.png)


Molecule is a Scala 3 library for querying and mutating SQL databases using a type-safe DSL — not one for SQL, but one generated from your own domain structure.

Most libraries offer a DSL to express _their_ concepts — like SQL or a SQL-DSL. Molecule inverts that: it builds a DSL directly from your domain structure, letting you model and query data in your own domain terms.

After defining your domain structure, Molecule generates boilerplate code for your custom DSL. You can then declare *what* data you want using this DSL, and Molecule handles *how* to retrieve or modify it.


## Compose your Data

Compose a "molecule" data model with the words of your domain in plain Scala instead of writing brittle query text strings or navigating intermediary DSLs:

::: code-tabs#coord
@tab Molecule
```scala
Person.name.age.Address.street
```
@tab SQL
```scala
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

Molecule translates your molecules to queries for the chosen database and you get back matching typed data with one of four APIs:

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


Likewise, Data can be transacted with molecules:

::: code-tabs#coord
@tab Save
```scala
Person.name("Ben").age(22).Address.street("Main st.").save.transact
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


## Domain Structure

The [Domain Structure](/database/setup/domain-structure) for the example above is written in vanilla Scala:

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
Running `sbt moleculeGen` then generates the necessary DSL code to let you write molecules.


## Limitation

Molecule is not a facade to database APIs or JDBC.

Molecule only focuses on generating queries and transactions from "molecule" data models in your code and execute them against a database, as easily and intuitively as possible.

Other more administrative operations on databases like creating indexes, manual rollbacks of transactions etc. are outside the scope of Molecule. It might be helpful to use other SQL library APIs to handle those operations if not more low-level abstractions like JDBC.


