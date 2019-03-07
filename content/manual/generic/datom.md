---
title: "Datom"
weight: 10
menu:
  main:
    parent: generic
    identifier: datom
up:   /manual/generic
prev: /manual/generic
next: /manual/generic/indexes
down: /manual/debug
---


# Datom API

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Datom.scala)


### Datoms / quintuplets

Attribute values are saved as quintuplets of information in Datomic:

![](/img/generic/datom.png)
<br><br>




The Datom API in Molecule let us retrieve each element generically for any molecule we are working on by 
providing the following "generic attributes" that we can add to our custom molecules:

 - `e` - Entity id (`Long`)
 - `a` - Attribute (`String`)
 - `v` - Value (`Any`)
 - `t` - Transaction point in time (`Long` alternatively `Int`)
 - `op` - Operation: assertion / retraction (`Boolean` true/false)

The Transaction value has two more representations

 - `tx` - Transaction entity id (`Long`)
 - `txInstant` - Transaction wall-clock time (`java.util.Date`)


### Mixing custom and generic attributes

Generic attributes like `e` can be added to retrieve an entity id of a custom molecule:

```scala
// Get entity id of Ben with generic datom attribute `e` on a custom molecule
Person.e.name.get.head === (benEntityId, "Ben")
```

And we can get information about the transaction time of the assertion of some custom attribute value:

```scala
// When was Ben's age updated? Using `txInstant`
Person(benEntityId).age.txInstant.get.head === (42, <April 4, 2019>) // (Date)
```

With a history db we can access the transaction number `t` and
assertion/retraction statusses with `op`

```scala
// 
Person(benEntityId).age.t.op.getHistory.sortBy(r => (r._2, r._3)) === List(
  (41, t1, true),  // age 41 asserted in transaction t1
  (41, t2, false), // age 41 retracted in transaction t2
  (42, t2, true)   // age 42 asserted in transaction t2
)
```


### Fully generic Datom molecules

In molecule, attribute names (the `A` of the Datom) are modelled as our custom DSL attributes as we saw above when we retrieved the
`Person.age` attribute value along with some generic datom data. 

Sometimes we will be interested in more generic data
where we don't know in advance what attributes will be involved. Then we can use the generic Datom
attribute `a` for Attribute name and `v` for value. We could for instance ask what we know about an entity over time
in the database:

```scala
// What do we know about the fred entity?
Person(fred).a.v.t.op.getHistory.sortBy(r => (r._2, r._3)) === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true), 
  (":Person/likes", "pizza", t6, false),
  (":Person/likes", "pasta", t6, true)  
)
```

### Filtering with expressions

By applying values to generic attributes we can filter search results:

```scala
// What was asserted/retracted in transaction tx3 about what Fred likes? 
Person(fred).likes.tx(tx6).op.getHistory.sortBy(r => (r._2, r._3)) === List(
  ("pizza", t6, false), // Fred no longer likes pizza
  ("pasta", t6, true)   // Fred now likes pasta
)
```


### Next

[Indexes...](/manual/generic/indexes)
