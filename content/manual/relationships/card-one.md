---
date: 2016-07-24T22:06:44+01:00
title: "Cardinality one"
weight: 10
menu:
  main:
    parent: relationships
up:   /manual/relationships
prev: /manual/relationships
next: /manual/relationships/card-many
down: /manual/crud
---

# Card-one relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)

In Molecule we model a cardinality-one relationship in our [schema definition file](/manual/schema/) with the `one[<RefNamespace>]` syntax:

```scala
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val home = one[Addr]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
}
```
The ref attribute `home` is a card-one relationship to namespace `Addr`. When our schema is then translated to 
Molecule boilerplate code our `home` ref attribute is accessible as a value by using its lower case name (`home` instead of `Home`):

```scala
Person.name.home.get === List(("Fred", 102))
```
This can be practical when we want to get a related entity id like `102` in this case.


### Ref namespace

More often though we want to collect the values of the referenced entity attributes. Molecule therefore also creates an Uppercase method `Home` that allow us to
add attributes from the `Home` (`Addr`) namespace:

```scala
Person.name.Home.street.city.get.head === ("Fred", "Baker St. 7", "Boston")
```
Describing the relationship we can simply say that a "Person has an Address". It's important to understand though that the namespace names `Person` and
`Addr` are not like SQL tables. So there is no "instance of a Person" but rather _"an **entity** with some Person attribute values"_ (and maybe other attribute values). 
It's the entity id that tie groups of facts/attribute values together.


### One-to-one or one-to-many

If Fred is living by himself on Baker St. 7 we could talk about a one-to-one relationship between him and his address.

But if several people live on the same address each person entity would reference the same address entity and we would then see 
each persons relation to the address as a one-to-many relationship since other persons also share the address:

```scala
Person.name.home.get === List(
  ("Fred", 102),
  ("Lisa", 102),
  ("Mona", 102)
)
```
Wether a relationship is a one-to-one or one-to-many relationship is therefore determined by the data and not the schema.


### Relationship graph

Relationships can nest arbitrarily deep. We could for instance in the `Addr` namespace have a relationship to a `Country` namespace 
and then get the country `name` too:

```scala
Person.name.Home.street.city.Country.name.get.head === ("Fred", "Baker St. 7", "Boston", "USA")
```
And so on...


### Next

[Card-many relationships...](/manual/relationships/card-many/)
