---
prev: /database/transact/raw-transact
---

# Attributes


Molecules are built by chaining Attributes - like atoms - together with the builder pattern. 

We could for instance build a molecule to fetch `name` and `age` of persons in a database:

```scala
Person.name.age
```
And we can continue from there adding more attributes from there. This forms a declarative Data Model of the data that we want to fetch. 

When we're satisfied with the attributes collected - our data model - we call  `query` on the molecule:

```scala
val namesAndAges: Query[(String, Int)] = Person.name.age.query
```
This qives us an immutable `Query` that is parameterized by the types involved. No side effects have been performed yet.



## Calling the database

When we're ready to submit the query to the database, we call `get` on the query and get back the type-safe result. 
This can be a List of either single scalar values or tuples of data:

```scala
val resul1: List[String]                 = Person.name.query.get
val resul2: List[(String, Int)]          = Person.name.age.query.get
val resul3: List[(String, Int, Boolean)] = Person.name.age.member.query.get
```
An implicit connection to the database needs to be in scope for calling `get`. See [Db setup](/database/setup/db-setup) on how to acqire the connection.


## 4 APIs

With a given import of a database and API we can return the data with 4 different APIs by importing the one you want to use:

::: code-tabs#coord
@tab Sync
```scala
import molecule.db.postgres.sync.*
       
val persons: List[(String, Int)] = 
  Person.name.age.query.get
```

@tab Async
```scala
import molecule.db.postgres.async.*

val persons: Future[List[(String, Int)]] = 
  Person.name.age.query.get
```

@tab ZIO
```scala
import molecule.db.postgres.zio.*

val persons: ZIO[Conn, MoleculeError, List[(String, Int)]] = 
  Person.name.age.query.get
```

@tab IO
```scala
import molecule.db.postgres.io.* 

val persons: cats.effect.IO[List[(String, Int)]] = 
  Person.name.age.query.get
```
:::

For brevity, we'll show synchronous results in most examples throughout this documentation.


## 3 modes

An attribute in a molecule can be either _mandatory, tacit or optional_.

Say we have two persons in a database, one with an age and another without,
```scala
Person.name("Bob").age(42).save.transact
Person.name("Liz").save.transact
```
then we can illustrate the different modes:

#### 1. Mandatory `attr`

Using the attribute name as-is ensures that only results containing the attribute value are returned.

Here, both `name` and `age` are mandatory, and only Bob has both:

::: code-tabs
@tab Molecule
```scala
Person.name.age.query.get ==> List(
  ("Bob", 42),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Person.name,
  Person.age
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;
```
:::


The equivalent for SQL queries is adding a `where` clause of `<attr> is not null`.


#### 2. Tacit `attr_`

Add an underscore to the attribute name to make it "tacit" or silent. This guarantees that a value exists for the attribute, but without returning the value.


Here we get the `name`s of persons that have an `age` set without returning the age:

::: code-tabs
@tab Molecule
```scala
Person.name.age_.query.get ==> List(
  "Bob"
)
```
@tab SQL
```sql
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;
```
:::

This way we can switch on and off individual attributes from the result set without affecting the data structures we look for.

The equivalent for SQL queries is again adding a `where` clause of `attr is not null` but this time not having the attribute in the `select` section.



#### 3. Optional `attr_?`

Add `_?` to an attribute to make it optional. This will return entities that both do and don't have the attribute value set.

Here we get the `name`s of persons and an optional `age`:

::: code-tabs
@tab Molecule
```scala
Person.name.age_?.query.get ==> List(
  ("Bob", Some(42)),
  ("Liz", None),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Person.name,
  Person.age
FROM Person
WHERE
  Person.name IS NOT NULL;
```
:::
A null value in an SQL table becomes a `None` in the result returned by the molecule.



## Types & collections

As we saw when defining a [Domain Structure](/database/setup/domain-structure), an attribute can be of basically any scalar type, Set, Seq or Map that we use in Scala! Molecule takes care of mapping each type to the database so that you can think freely in terms of Scala code and focus on your business code rather than in SQL types and implementation details.


::: code-tabs#types
@tab Primitive
```scala
String
Int
Long
Float
Double
Boolean
BigInt
BigDecimal
java.util.Date
java.time.Duration
java.time.Instant
java.time.LocalDate
java.time.LocalTime
java.time.LocalDateTime
java.time.OffsetTime
java.time.OffsetDateTime
java.time.ZonedDateTime
java.util.UUID
java.net.URI
Byte
Short
Char
```

@tab Set
```scala
Set[String]
Set[Int]
Set[Long]
Set[Float]
Set[Double]
Set[Boolean]
Set[BigInt]
Set[BigDecimal]
Set[java.util.Date]
Set[java.time.Duration]
Set[java.time.Instant]
Set[java.time.LocalDate]
Set[java.time.LocalTime]
Set[java.time.LocalDateTime]
Set[java.time.OffsetTime]
Set[java.time.OffsetDateTime]
Set[java.time.ZonedDateTime]
Set[java.util.UUID]
Set[java.net.URI]
Set[Byte]
Set[Short]
Set[Char]
```

@tab Seq
```scala
Seq[String]
Seq[Int]
Seq[Long]
Seq[Float]
Seq[Double]
Seq[Boolean]
Seq[BigInt]
Seq[BigDecimal]
Seq[java.util.Date]
Seq[java.time.Duration]
Seq[java.time.Instant]
Seq[java.time.LocalDate]
Seq[java.time.LocalTime]
Seq[java.time.LocalDateTime]
Seq[java.time.OffsetTime]
Seq[java.time.OffsetDateTime]
Seq[java.time.ZonedDateTime]
Seq[java.util.UUID]
Seq[java.net.URI]
Array[Byte] // special case for byte arrays
Seq[Short]
Seq[Char]
```

@tab Map
```scala
Map[String, String]
Map[String, Int]
Map[String, Long]
Map[String, Float]
Map[String, Double]
Map[String, Boolean]
Map[String, BigInt]
Map[String, BigDecimal]
Map[String, java.util.Date]
Map[String, java.time.Duration]
Map[String, java.time.Instant]
Map[String, java.time.LocalDate]
Map[String, java.time.LocalTime]
Map[String, java.time.LocalDateTime]
Map[String, java.time.OffsetTime]
Map[String, java.time.OffsetDateTime]
Map[String, java.time.ZonedDateTime]
Map[String, java.util.UUID]
Map[String, java.net.URI]
Map[String, Byte]
Map[String, Short]
Map[String, Char]
```
:::


Molecule transparently maps each type to and from the database.


### Collection types

Using collection types is useful for smaller collections of values.

```scala
// Set of nicknames (non-ordered unique values)
Person.nicknames.query.get.head ==> Set("J", "Jonny")

// Seq of scores (ordered non-unique values)
Person.scores.query.get.head ==> Seq(7, 8, 7, 9)

// Map of names in different languages
Person.langNames.query.get.head ==> Map(
  "en" -> "Shostakovich",
  "de" -> "Schostakowitsch",
  "fr" -> "Chostakovitch",
)
```

The `Map` type can often be useful to store language-keyed data.

Collection data is always returned as the default collection type, in the case of `Seq`, the default subtype `List` is returned:

```scala
Person.scores.query.get.head ==> List(1, 2, 3)

// We can still match the super type
Person.scores.query.get.head ==> Seq(1, 2, 3)
```

Molecule saves Map data as JSON in the database. Set and Seq data is saved as Arrays in the database where available, otherwise as JSON.


## Distinct rows

Molecule only returns distinct rows. This is unlike SQL databases that can return duplicate rows.

Sometimes we want rows with the same values though. We can then use the special `id` attribute that corresponds to an auto-incremented and unique id column that Molecule expects in SQL tables. If we had two persons named "Liz" we would get:

```scala
// with id
Person.id.name.query.get ==> List(
  (1, "Bob"),
  (2, "Liz"),
  (3, "Liz"),
)
// without id, two rows with "Liz" coalesce to one row
Person.name.query.get ==> List(
  "Bob",
  "Liz"
)
```