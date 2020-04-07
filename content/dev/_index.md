---
date: 2014-05-14T02:13:50Z
title: "Developer"
weight: 10
menu:
  main:
    parent: dev
---

# Developer

The basic philosophy of Molecule is to think from the User's perspective towards a technical solution. 

The priority is always to make it as easy as possible for the end User to make Datomic queries, no matter 
how challenging on the Molecule backend.

When we asked

_"how can I most intuitively and with most minimal code query for persons?"_

we first tried something like this
```
import Person._
m(firstName ~ lastName ~ age) // all fields of a `Person` trait...
```
But it quickly became un-intuitive with relations, expressions etc.

### Builder pattern

Instead we settled on using the builder pattern with `.`s

```
Person.firstName.lastName.age
```
We can't think of a more minimal Scala representation of "finding persons".


### Generated boilerplate code

The builder pattern has shown a surprising strong capacity to express a wide range of query constructs. But it also
 requires an extensive amount of boilerplace code to work.

We therefore generate all boilerplate code automatically when we compile our project with `sbt compile`.


### Scala macro transformations
Our generated boilerplate code allow us to build molecules attribute by attribute:

```
val personsMolecule = m(Person.name.age)
```

The `m`olecule method transforms our source code _at compile time_ through a series of states:

1. Source code
2. Model AST
3. Query AST
4. Datomic query string

The end result is simply a Datomic query string:

```
"[:find ?b ?c :where [?a :Person/name ?b] [?a :Person/age ?c]]"
```

Since the query is created at compile time it's all ready to fetch our data _at runtime_ with no performance impact:

```
val data = personsMolecule.get
```

Given implicit conversions we could even unify the two steps:
```
val persons = Person.name.age.get
```
The query would still be created at compile time and fetching data at runtime.


### Closed eco-system

Since we create our molecules from our self-generated boilerplate code our macros have full knowledge about the 
possible constructs we can expect. We are therefore in full control of the entire "eco-system" from molecule
 to Datomic query. Non-valid molecules simply won't compile. And we can infer all type information from our molecules.

[Read more about the macro transformations...](/dev/transformation)