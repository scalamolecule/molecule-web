---
title: "Molecule and Datomic"
weight: 30
menu:
  main:
    parent: dev
    identifier: dev-molecule-datomic 
---

# Molecule and Datomic

Molecule targets the [Datomic](https://www.datomic.com) database. So it can be good idea to know how Datomic works in order to understand Molecule better.


### Facts/Datoms with time built in

Instead of mapping objects to tables or documents, the core unit of data in Datomic is an atomic piece of information: a _Datom_. A Datom describes a _fact_, for instance that "John likes pizza". A timestamp adds information about _when_ John stated that he likes pizza. A fifth piece of information "added" tells if the fact is asserted (true - John likes) or retracted (false - John no longer likes). So, a Datom consists of 5 pieces of information:
```
 John     likes    pizza      12:35:54       true
   |        |        |           |            |
Entity  Attribute  Value  Transaction/time  Added
```
With Molecule we could model asserting the fact like this:

```
val txTime = Person(johnId).likes("pizza").update.txInstant
```
_likes_ is an `attribute` with `value` _pizza_. It is **asserted** that the `entity` _johnId_ likes pizza at `transaction` time 12:35:54. A timestamp is automatically set with all transactions. But if we need "domain time" we could add such attribute to the transaction as well, since this is simply a saved data structure in Datomic as our domain data (more on [Transaction meta data](/code/transactions))

As you saw, Molecule simply models Datomic datoms by chaining together _attributes_ to form "_molecules_" in unlimited combinations suiting your domain. You can then call different operations on a molecule as we saw above in order to interact with the underlying Datomic database.

### Immutable data

Everytime a fact is asserted the old value of the attribute is _not deleted_. Data is only appended to a Datomic database, and an update of an attribute value internally creates a retraction of the old value and an assertion of the new value. In this way, we can [go back in time](/code/time) and see the values of an attribute _at any point in time_. We could for instance see all our previous addresses if this was part of our domain model.

Also when we delete data, it's actually not deleted, but "retracted". Retracted data doesn't show up when we are querying the current database. But if we look at the database at an earlier point in time we can see the data before it got retracted.

### Namespaces and attributes

In Molecule, `attributes` are organized in `namespaces` to group related qualities of our domain:

![](/img/page/intro/DatomicElements1.png)

### Entity != row in an sql Table

An `entity` can have _any_ `attribute` from _any_ `namespace` associated to it:

![](/img/page/intro/DatomicElements2.png)

An entity is therefore not like a row in a table but rather a "cross-cutting" thing that we can freely associate any attribute value to. Note how "attrB1" in this example is not associated to entity1.



## Query optimization

Molecule transparently optimize all queries sent to Datomic.

Most selective Clauses are automatically grouped first in the :where section of the Datomic query as per the recommendation in [Datomic Best Practices](https://docs.datomic.com/on-prem/best-practices.html#most-selective-clauses-first).

This brings dramatic performance gains of in some cases beyond 100x compared to un-optimized queries. The optimization happens automatically in the background so that you can focus entirely on your domain without concern for the optimal order of attributes in your molecules.

