---
date: 2015-01-02T22:06:44+01:00
title: "Building"
weight: 10
menu:
  main:
    parent: attribute
---

# Building molecules of Attributes







### Namespaced Attributes

A molecule starts with a Namespace and builds on with attributes and/or other Namespaces/Attributes to form a desired data structure to work with. We could for instance have some attributes that relates to how we model a Person and organize those attributes in a `Person` namespace:

```scala
trait Person {
  val name = oneString
  val age  = oneInt
}
```

We can then use the Attributes `name` and `age` to build a molecule that will query for names and ages of person in the database:


```scala
val persons = Person.name.age.get
```

