---
date: 2015-01-02T22:06:44+01:00
title: "Types"
weight: 20
menu:
  main:
    parent: query
---

# Attribute types

Molecule Attributes can have the following types:

```
Cardinality one     Cardinality many
---------------     ----------------
String              Set[String]
Int                 Set[Int]   
Long                Set[Long]    
Float               Set[Float]     
Double              Set[Double]      
Boolean             Set[Boolean]      
Date                Set[Date]    
UUID                Set[UUID]    
URI                 Set[URI]   
enum: String        enums: Set[String]              
```
Attribute are defined in your [schema](/manual/schema/definition/).
 

### Types inferred

Types are inferred so that you can determine the result signature of a molecule

```scala
val persons: List[(String, Int)] = Person.name.age.get
```


### Cardinality one/many

Attributes defined as cardinality-many will return sets of values. A Person could for instance have a `hobbies` cardinality-many Attribute defined and thus return sets of hobby values:

```scala
val engaged: List[(String, Set[String])] = Person.name.hobbies.get
```

### Enums
Enum values are predefined values that an Attribute can have. They are defined in your schema. Cardinality one enums can have one enum value and cardinality many can have a set of unique enum values for each entity.
