# Slick

Compare [Slick Tutorial](https://scala-slick.org/doc/stable/sql-to-slick.html) code examples with molecules in the following sections or see [setups](#slick-setup).

_The molecule examples use the simple synchronous api for brevity. [Asynchronous/ZIO/cats.effect.IO](/database/query/attributes#4-apis) apis are also available._


## Slick Setup

In Slick you need to define 3 things for each table in the database:

- A tuple type for the column values of the Table
- A class for the Table columns
- A `val` for a Table query

```scala
object Tables {
  type Person = (Int, String, Int, Int)
  class People(tag: Tag) extends Table[Person](tag, "PERSON") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def age = column[Int]("AGE")
    def addressId = column[Int]("ADDRESS_ID")
    def * = (id, name, age, addressId)
    def address = foreignKey("ADDRESS", addressId, addresses)(_.id)
  }
  lazy val people = TableQuery[People]

  type Address = (Int, String, String)
  class Addresses(tag: Tag) extends Table[Address](tag, "ADDRESS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def * = (id, street, city)
  }
  lazy val addresses = TableQuery[Addresses]
}

import Tables.*
```

## Molecule Setup

In Molecule you define things more in terms of your domain structure with only the Entities and their Attributes.


- Single trait for each domain entity (instead of 3 different definitions)
- No redundant keywords like `column[..](..)`. In contrast, the repeating "one\<type\>" in the Molecule structure defines cardinality-one semantics and is therefore not redundant.
- No redundant repetition of each column name ("street" .. "STREET")

```scala
object People extends DomainStructure {

  trait Person {
    val name    = oneString
    val age     = oneInt
    val address = one[Address]
  }

  trait Address {
    val street = oneString
    val city   = oneString
  }
}
```
From this model, Molecule generates

- An SQL schema that we can transact to create the database
- Boilerplate code to write molecule queries and transactions

An important difference is also that with Molecule you don't need to decide upfront whether an attribute can be optional. You can enforce mandatory values though by adding `mandatory` after the type definition. If for instance we wanted to enforce that a code of a Country is mandatory, we could define `val code = oneString.mandatory`. For all definition options, see [Domain Structure](/database/setup/domain-structure#attribute-options).



## Select

The Slick equivalent of `SELECT *` is the result of the plain TableQuery:

```scala
// Slick
db.run(people.result)
```

This means that you will often over-fetch data with Slick if not all attribute values of a Table are needed.

In Molecule you instead choose exactly which attributes you need and what order you want them in:

```scala
// Molecule
Person.id.name.age.query.get
```

#### Select certain columns

```scala
val query = people.map(p =>
  (p.age, p.name ++ " (" ++ p.id.asColumnOf[String] ++ ")")
)
db.run(query.result)
```

With Molecule we would concatenate `name` and `id` from the returned result set:

```scala
Person.id.name.age.query.get.map {
  case (id, name, age) => (age, s"$name ($id)")
}
```

## filter

```scala
val query = people.filter(p => p.age >= 18 && p.name === "C. Vogt").result
db.run(query.result)
```

Filter by values applied to attributes in a molecule:
```scala
Person.age.>=(18).name("C. Vogt").query.get
```

## sortBy

```scala
val query = people.sortBy(p => (p.age.asc, p.name)).result
db.run(query.result)
```

Add `a1` for ascending or `d1` for descending to an attribute in Molecule:

```scala
Person.age.a1.name.query.get
```

Use a secondary sort order with `a2`/`d2` etc

## Aggregations

```scala
val query = people.map(_.age).max.result
db.run(query.result)
```

Aggregate functions like `max` are applied as a keyword to a molecule attribute:

```scala
Person.age(max).query.get

// or get a range of top values
Person.age(max(2)).query.get
```

Aggregate calculations in Molecule include `min`, `max`, `count`, `countDistinct`, `sum`, `avg`, `median`, `variance`, `stddev` and `sample`.

## groupBy

```scala
val query = people.groupBy(p => p.addressId)
  .map { case (addressId, group) => (addressId, group.map(_.age).avg) }
  .list
db.run(query.result)
```

Molecule automatically groups by attributes not having an aggregate expression when the molecule has an aggregation.

In this case the query will group by `address` and calculate the average `age` for persons living there.

```scala
Person.address.age(avg).query.get
```

## groupBy + filter

```scala
val query = people.groupBy(p => p.addressId)
  .map { case (addressId, group) => (addressId, group.map(_.age).avg) }
  .filter { case (addressId, avgAge) => avgAge > 50 }
  .map(_._1)
  .result
db.run(query.result)
```

Filter an aggregation value on the result in Molecule:
```scala
Person.address.age(avg).query.get.filter(_._2 > 50)
```

## Joins

#### Implicit join

```scala
val query = people.flatMap(p =>
  addresses.filter(a => p.addressId === a.id)
    .map(a => (p.name, a.city))
).result
db.run(query.result)

// or equivalent for-expression:
val query = (for (p <- people;
      a <- addresses if p.addressId === a.id
      ) yield (p.name, a.city)
  ).result
db.run(query.result)
```

```scala
Person.name.Address.city.query.get
```

#### Explicit join

```scala
val query = (people join addresses on (_.addressId === _.id))
  .map { case (p, a) => (p.name, a.city) }.result
db.run(query.result)
```

```scala
Person.name.Address.city.query.get
```

#### left/right/outer join

```scala
val query = (addresses joinLeft people on (_.id === _.addressId))
  .map { case (a, p) => (p.map(_.name), a.city) }.result
db.run(query.result)
```

Left join in Molecule uses the syntax

    <RelationshipName>.?(<RelatedNamespace>.<attributes..>)

and returns `Option[(<attribute values..)]`

```scala
Person.name.Address.?(Address.city).query.get
```

## Subquery

```scala
val address_ids = addresses.filter(_.city === "New York City").map(_.id)
val query       = people.filter(_.id in address_ids).result // <- run as one query
db.run(query.result)
```
Molecule doesn't directly have subqueries but the Slick example could in this case be expressed in Molecule as: 
```scala
Person.age.name.Address.city_("New York City").query.get
```

## Insert

```scala
val query = people.map(p => (p.name, p.age, p.addressId))
  .insert(("M Odersky", 12345, 1))
db.run(query.result)
```

```scala
Person.name.age.address.insert(
  ("M Odersky", 12345, 1L),
).transact
```
Or we can populate a molecule and call `save` on it
```scala
Person.name("M Odersky").age(12345).address(1).save.transact
```

## Update

```scala
val query = people.filter(_.name === "M Odersky")
  .map(p => (p.name, p.age))
  .update(("M. Odersky", 54321))
db.run(query.result)
```

```scala
Person.name_("M Odersky").name("M. Odersky").age(54321).update.transact
```

## Delete

```scala
val query = people.filter(p => p.name === "M. Odersky").delete
db.run(query.result)
```

```scala
Person.name_("M. Odersky").delete.transact
```

## case

```scala
val query = people.map(p =>
  Case
    If (p.addressId === 1) Then "A"
    If (p.addressId === 2) Then "B"
).list
db.run(query.result)
```
In Molecule we'd map on the result
```scala
Person.address(1, 2).query.get.map {
  case 1 => "A"
  case 2 => "B"
}
```
