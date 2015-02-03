---
date: 2015-01-02T22:06:44+01:00
title: "Values"
weight: 30
menu:
  main:
    parent: attributes
---

# Applying Attribute values

We can apply values to Attributes in order to filter the data structures we are looking for. We could for instance look for names of female persons:

```scala
Person.name.gender.apply("female")
```
or simply

```scala
Person.name.gender("female")
```

### Omit applied value
Instead of returning the gender value "female" for all entities returned we could add an underscore to only return the names of female persones:

```scala
val females: List[String] = Person.name.gender_("female")
```

### Inferred types
Only applied values matching the Attribute type can compile. And we can even infer the type in our IDE so that we will get warned if we try to apply a non-matchig type:

```scala
// types match
Person.name("John").age(42)

// won't compile
// IDE will bark: 42(Int) is not of type String
Person.name(42)
```

