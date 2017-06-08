---
date: 2015-01-02T22:06:44+01:00
title: "Attributes"
weight: 30
menu:
  main:
    parent: docs
    identifier: attributes
up: /docs/schema
prev: /docs/schema/transaction
next: /docs/attributes/basics
down: /docs/entities
---

# Attributes
 
Examples from the sub pages:

<br>

[Attribute basics](/docs/attributes/basics), return types, arity, cardinality ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala))
```scala
val persons: Iterable[(String, Int)] = Person.name.age.get
```
<br>

[Mandatory/Tacet/Optional](/docs/attributes/modes) attributes ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala))
```scala
Person.name.age.get  // all required values              ("mandatory value")
Person.name.age_.get // age is required but not returned ("tacet value")
Person.name.age$.get // optional age returned            ("optional value")
```
<br>

[Map attributes](/docs/attributes/mapped) - mapped attribute values 
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap))
```scala
Person.id.name.get.head === (
  1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```
<br>

[Expressions](/docs/attributes/expressions) - filter attribute values with expressions 
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression))
```scala
Person.age(42)                  // equality
Person.name.contains("John")    // fulltext search
Person.age.!=(42)               // negation
Person.age.<(42)                // comparison
Person.age(nil)                 // nil (null)
Person.name("John" or "Jonas")  // OR-logic
```
<br>

[Aggregates](/docs/attributes/aggregates) - aggregate attribute values 
([tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))
```scala
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```
<br>

[Parameterize](/docs/attributes/parameterized) - re-use molecules and let Datomic cache queries and optimize performance 
([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Input.scala))
```scala
val person = Person.name(?).age(?)

// Re-use `person`
val Johan  = person("John", 33).get.head
val Lisa   = person("Lisa", 27).get.head
```
