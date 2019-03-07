---
title: "Relationships"
weight: 60
menu:
  main:
    parent: manual
    identifier: relationships
up:   /manual/entities
prev: /manual/entities
next: /manual/relationships/card-one
down: /manual/crud
---

# Relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)


To understand how Molecule treats relationships it is valuable to get an idea of how they work in Datomic.


### Ref attributes connect entities

A relationship in Datomic is simply when a ref attribute of entity A has an entity B id value. Then there is a relationship from A to B!

In the following example, entity `101` has a ref attribute `:Person/home` with a value `102`. That makes the relationship between 
entity `101` and entity `102`, or that Fred has an Address:

![](/img/relationships/ref.jpg)

We can illustrate the same data as two entities (groups of facts with a shared entity id) with the link between them:

![](/img/relationships/entityref.jpg)

[Card-one relationships](/manual/relationships/card-one/)...


### Card-many ref attributes

Since datomic has cardinality many attributes, ref attributes can also be of cardinality many.

We could for instance have a classic Order/LineItems card-many example where the `:Order/items` card-many ref attribute has two
LineItem entity id values `102` and `103`:

![](/img/relationships/entityrefs.jpg)


[Card-many relationships](/manual/relationships/card-many/)...


