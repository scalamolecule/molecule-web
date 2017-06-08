---
date: 2015-01-02T22:06:44+01:00
title: "Parameterized"
weight: 60
menu:
  main:
    parent: attributes
up: /docs/attributes
prev: /docs/attributes/aggregates
next: /docs/entities
down: /docs/entities
---

# Parameterized Input-molecules

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Input.scala)

Molecules can be parameterized by applying the input placeholder `?` as a value to an attribute. The molecule then expects input for that
attribute at runtime.

By assigning parameterized "Input-molecules" to variables we can re-use those variables to query for 
similar data structures where only some data part varies:

```scala
// 1 input parameter
val person = m(Person.name(?))

val john = person("John").get.head
val lisa = person("Lisa").get.head
```

Of course more complex molecules would benefit even more from this approach.

### Datomic cache and optimization
Datomic will cache and optimize the queries from such Input-molecules. This gives us an additional 
reason to use them.


### Parameterized expressions

```scala
val personName  = m(Person.name(?))
val johnOrLisas = personName("John" or "Lisa").get // OR
```

### Multiple parameters
Molecules can have up to 3 `?` placeholder parameters. Since we can apply expressions and logic to 
them it seems likely that this will satisfy the majority of all parameterized queires.

```scala
val person      = m(Person.name(?).age(?))
val john        = person("John" and 42).get.head // AND
val johnOrJonas = person(("John" and 42) or ("Jonas" and 38)).get // AND/OR
```

### Mix parameterized and static expressions

```scala
val americansYoungerThan = m(Person.name.age.<(?).Country.name("USA"))
val americanKids         = americansYoungerThan(13).get
val americanBabies       = americansYoungerThan(1).get
```

For more examples, please see the 
[Seattle examples](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTests.scala)



### Next

[Entities...](/docs/entities)