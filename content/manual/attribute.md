---
date: 2015-01-02T22:06:44+01:00
title: "Attributes"
weight: 30
menu:
  main:
    parent: manual
    identifier: attributes
---

# Molecule Attributes
 
[Build molecules](/manual/attribute/builder) with the builder pattern
```scala
Person.name.age.gender  // require values
Person.name.age_.gender // require but omit values
```

[Infer types](/manual/attribute/types)
```scala
val name   : String      = Person.name.get.one
val age    : Int         = Person.age.get.one
val hobbies: Set[String] = Person.hobbies.get.one // cardinality-many
val gender : String      = Person.gender.get.one  // enum values
```

[Apply values](/manual/attribute/values)
```scala
Person.age(42)               // equal value
Person.age.<(42)             // range values
Person.age.!=(42)            // negated values
Person.name.contains("John") // fulltext search
```

[Apply logical-OR](/manual/attribute/logical-or)
```scala
Person.name("John" or "Jonas")
```

[Parameterize](/manual/attribute/parameterize)
```scala
val person      = Person.name(?).age(?)
val john        = person("John" and 42) // logical-AND
val johnOrJonas = person(("John" and 42) or ("Jonas" and 38))
```