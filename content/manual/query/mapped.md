---
date: 2015-01-02T22:06:44+01:00
title: "Mapped attributes"
weight: 20
menu:
  main:
    parent: query
---

# Mapped Attributes

(See [mapped tests](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule/attrMap))

Mapped values can be saved with mapped attributes in Molecule. It's a special Molecule construct that makes
it easy to save for instance multi-lingual data without having to create language-variations of each attribute.
But they can also be used for any other key-value indexed data. 

Say you want to save famous Persons names in multiple languages. Then you could use a mapString:

```scala
// In definition file
val name = mapString
 
// Insert mapped data
Person.id.name.insert(
  1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)

// Retrieve mapped data
Person.id.name.one === (1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```

Molecule concatenates the key and value of each pair to one of several values of an underlying cardinality-many attribute. When
data is then retrieved Molecule splits the concatenated string into a typed pair. This all happens automatically and let's us focus 
 on their use in our code.


There's a broad range of ways we can query mapped attributes and you can see a lot of examples of their use in 
the [`attrMap` test cases](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule/attrMap)...