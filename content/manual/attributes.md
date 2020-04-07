---
title: "Attributes"
weight: 40
menu:
  main:
    parent: manual
    identifier: attributes
up: /manual/schema
prev: /manual/schema/transaction
next: /manual/attributes/basics
down: /manual/entities
---

# Attributes

Molecules are built by chaining attributes together with the builder pattern. Here are some groups of different attribute types and their use with links to their manual pages:

<br>

[Attribute basics](/manual/attributes/basics), return types, arity, cardinality ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala))
```
val persons: List[(String, Int)] = Person.name.age.get
```
<br>

[Mandatory/Tacit/Optional](/manual/attributes/modes) attributes ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala))
```
Person.name.age.get  // all required values              ("mandatory value")
Person.name.age_.get // age is required but not returned ("tacit value")
Person.name.age$.get // optional age returned            ("optional value")
```
<br>

[Map attributes](/manual/attributes/mapped) - mapped attribute values
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap))
```
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

[Expressions](/manual/attributes/expressions) - filter attribute values with expressions
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression))
```
Person.age(42)                  // equality
Person.name.contains("John")    // fulltext search
Person.age.!=(42)               // negation
Person.age.<(42)                // comparison
Person.age(nil)                 // nil (null)
Person.name("John" or "Jonas")  // OR-logic
```
<br>

[Aggregates](/manual/attributes/aggregates) - aggregate attribute values
([tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))
```
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```
<br>

[Parameterize](/manual/attributes/parameterized) - re-use molecules and let Datomic cache queries and optimize performance
(tests: 
[1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1),
[2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2),
[3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3))
```
val person = m(Person.name(?).age(?))

// Re-use `person` input molecule
val Johan  = person("John", 33).get.head
val Lisa   = person("Lisa", 27).get.head
```
