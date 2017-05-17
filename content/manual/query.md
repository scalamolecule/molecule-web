---
date: 2015-01-02T22:06:44+01:00
title: "Query"
weight: 60
menu:
  main:
    parent: manual
    identifier: query
---

# Molecule Queries
 
[Building molecules](/manual/query/builder) with the builder pattern 
([tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/attr/Attribute.scala))
```scala
Person.name.age.gender  // require values
Person.name.age_.gender // require but omit values ("tacet values")
```

[Maped values](/manual/query/mapped) - mapped attribute values 
([tests](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule/attrMap))
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

[Expressions](/manual/query/expressions) - filter attribute values with expressions 
([tests](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule/expression))
```scala
Person.age(42)                  // equal value
Person.age.!=(42)               // negate values
Person.age.<(42)                // compare values
Person.age.>(42)                // compare values
Person.age.<=(42)               // compare values
Person.age.>=(42)               // compare values
Person.name.contains("John")    // fulltext search
Person.name("John" or "Jonas")  // OR-logic
```

[Aggregates](/manual/query/aggregates) - aggregate attribute values 
([tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))
```scala
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```

[Parameterize](/manual/query/parameterize) - re-use molecules and let Datomic cache queries and optimize performance 
([tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/attr/Input.scala))
```scala
val person = Person.name(?).age(?)

// Re-use `person`
val Johan  = person("John", 33).get.head
val Lisa   = person("Lisa", 27).get.head
```
[Relationships](/manual/query/relationships) - Connect namespaces with
([tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/ref))

```scala
Person.name.City.name.Country.name
```