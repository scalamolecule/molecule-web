---
date: 2015-01-02T22:06:44+01:00
title: "Introduction"
weight: 20
menu:
  main:
    parent: home
---

# Molecule introduction

Facts like 

<pre>John likes pizza 12:35:54</pre>

are stored in Datomic as `Datom`s having these 4 basic elements:

`entity` `attribute` `value` `transaction`(time)

In this example, _likes_ is an `attribute` and it has the `value` _pizza_. It is **asserted** that 
the `entity` _John_ likes pizza at `transaction` time 12:35:54. 


### Immutable data

Everytime a fact is asserted the old value of the attribute is _not deleted_. A Datomic database
is immutable. We can go back in time and see the values of an attribute _at any point in time_. We could for 
instance see all our previous addresses if this was part of our domain model.

Also when we delete data, it's actually not deleted, but "retracted". Retracted data doesn't show
 up when we are querying the current database. But if we look at the database at an earlier point in
  time we can see the data before it got retracted.

### Namespaces and attributes

`attributes` are typically organized in `namespaces` to group related qualities of our domain:
 
![](/img/DatomicElements1.png)

### Entity != row in an sql Table

An `entity` can have _any_ `attribute` from _any_ `namespace` associated to it:

![](/img/DatomicElements2.png)

An entity is therefore not like a row in a table but rather a "cross-cutting" thing that we can
freely associate any attribute value to. Note how "attrB1" in this example is not associated to entity1.


### Molecules

If we imagine attributes as atomic data units, then we can 
imagine molecules as 3-dimensional data structures composed of atoms.

In Molecule we use the builder pattern to model such data structures, attribute 
by attribute until we have a desired data structure. We could model `entity1`
from the example above as

```scala
NamespaceA.attrA1.attrA2.NamespaceB.attrB2
```

### Queries with molecules

If a namespace `Community` has two attributes `name` and `url` we can 
model a data structure of community names and urls as a molecule and then ask
the database to return those to us as a list of tuples with `name`/`url` values:

```scala
val namesAndUrls: List[(String, String)] = m(Community.name.url).get
```

Note how the return types of `name` and `url` are infered. 

Implicit conversions even allow us to condense our query.

```scala
val namesAndUrls = Community.name.url.get
```

### Values and expressions

Values and expressions can be applied to attributes of our molecule so that
 we can express more complex data structures:

```scala
Community.name.`type`("twitter" or "facebook_page")
```

Here we find `name`s of communities of a `type` that has either the value "twitter" 
OR "facebook_page". With your own definitions you can write similar
 complex queries in a simple way using the terms of your own domain.

### Further reading...

Go straight to the [Molecule Seattle tutorial][tutorial] to see a wide range of
 queries that Molecule can express, or check out first how we use Molecule to 
 [setup the database][setup] and [populated it with data][populate].
 
 
[setup]: https://github.com/scalamolecule/wiki/Setup-a-Datomic-database
[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial