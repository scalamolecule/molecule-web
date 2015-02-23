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
 
[Build molecules](/manual/query/builder) with the builder pattern
```scala
Person.name.age.gender  // require values
Person.name.age_.gender // require but omit values ("tacet values")
```

[Types](/manual/query/types) - all types inferred by IDE
```scala
val name   : String      = Person.name.one
val age    : Int         = Person.age.one
val hobbies: Set[String] = Person.hobbies.one // cardinality-many
val gender : String      = Person.gender.one  // enum values
```

[Expressions](/manual/query/expressions) - filter attribute values with expressions
```scala
Person.age(42)               // equal value
Person.age.!=(42)            // negate values
Person.age.<(42)             // compare values
Person.age.>(42)             // compare values
Person.age.<=(42)            // compare values
Person.age.>=(42)            // compare values
Person.name.contains("John") // fulltext search
```

[Aggregates](/manual/query/aggregates) - aggregate attribute values
```scala
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```

[OR-logic](/manual/query/or-logic) - apply OR logic to collect alternative values
```scala
Person.name("John" or "Jonas")
```

[Parameterize](/manual/query/parameterize) - re-use molecules and let Datomic cache queries and optimize performance
```scala
val person = Person.name(?).age(?)

// Re-use `person`
val Johan  = person("John", 33).one
val Lisa   = person("Lisa", 27).one
```