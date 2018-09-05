---
date: 2015-01-02T22:06:44+01:00
title: "Time"
weight: 90
menu:
  main:
    parent: manual
    identifier: time
up:   /manual/transactions
prev: /manual/transactions/tx-meta-data
next: /manual/time/asof-since
---

# Time

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time)

Datomic has powerful ways of accessing the immutable data that accumulates over time in the database.

Molecule maps those to 5 data getters that we can illustrate with a time line of transactions.


![](/img/time/all.png)


The 5 ways of getting data have the following semantics:

- `get` - snapshot of tx 1-7 accumulated. This is the current view of the database ("as of now") that we normally use.
- [☞ `getAsOf(t4)`](/manual/time/asof-since/) - snapshot of tx 1-4 accumulated. How the db looked after tx 4 was transacted.
- [☞ `getSince(t4)`](/manual/time/asof-since/) - snapshot of tx 5-7 accumulated. What has happened since tx 4 until now.
- [☞ `getHistory`](/manual/time/history/) - all transactions! See all that has happened over time.
- [☞ `getWith(tx8data)`](/manual/time/with/) - snapshot of tx 1-8 accumulated given some tx 8 data. "What if"-look into the future.



## Point in time {#pointintime}

The two methods `getAsOf(t)` and `getSince(t)` takes a _point in time_ in the database that can be supplied in
3 different ways:


### 1. `tx` Transaction entity id 

A transaction entity id is the 4th value of the Datomic quintuplets that tells us in what transaction
 this Datom/fact was asserted/retracted.
 
![](/img/time/1.png)

In Molecule code we can get this information by adding the generic `tx` attribute after an attribute:

```scala
Person(e5).likes.tx.get.head === ("pizza", tx4)
```
Here we get some transaction entity id `tx4` (a `Long` number) for the transaction where it 
was asserted that Person entity `fredId` likes pizza. 

Such transaction entity id can then be used as a point in time `t` for `getAsOf(t)` or `getSince(t)` in
other queries.


### 2. `t` Transaction value

An alternative to the transaction entity id is a "transaction value" that is an auto-incremented number 
that Datomic generates automatically in the background for each transaction taking place. 
This can be useful if we for instance want to examine "the previous" transaction.

As when getting the transaction entity id with `tx` we can get the transaction value by appending
the generic Molecule attribute `t` after some attribute:

```scala
val someT = Person(e5).likes_.t.get.head
```
Then we could ask "was there another value in the previous transaction?"

```scala
val previousT = someT - 1
Person(e5).likes.getAsOf(previousT) === Nil // There were no `likes` value before...
```

Transaction values can be converted to transaction entity ids and vice versa if needed with the Datomic Peer methods `toTx` and `toT`

```
// t -> tx
datomic.Peer.toTx(t1) === tx1

// tx -> t
datomic.Peer.toT(tx2) === t2
```


### 3. `java.util.Date`

Lastly we can also supply a human time/date of type `java.util.Date` 

```scala
val criticalDate = new Date("2017-04-26")
Person(e5).likes.getAsOf(criticalDate) === List("pizza")
```



## Data getters

### `get` current view

Normally we get the current state of the database with the `get` method on a molecule.

```scala
Person.name.age.get === ... // Persons as of now
```
But we might be interested in how the data looked at another point in time:


### [☞ `getAsOf(t)`](/manual/time/asof-since/)

When we call `getAsOf(t)` on a molecule we get the data as it looked at some point in time `t`. 


We could for instance want to know what Persons existed in the database the 5th of November:

```scala
Person.name.age.getAsOf(nov5date) === ... // Persons as of November 5 (inclusive) 
```


### [☞ `getSince(t)`](/manual/time/asof-since/)

Likewise we might want to know what Persons have been added _after_ or _since_ 5th of November. When
we call `getSince(nov5date)` we will get a snapshot of the current
database filtered with only the data added/retracted after November 5:

```scala
Person.name.age.getSince(nov5date) === ... // Persons added after November 5
```


### [☞ `getHistory`](/manual/time/history/)

The `getHistory` can for instance tell us how a Persons age attribute value has changed over time

```scala
Person(fredId).age.getHistory === ... // Current and previous ages of Fred
```
Note that this is not a snapshot in time but a series of all assertions and retractions over time that matches the query!


### [☞ `getWith(testTxData)`](/manual/time/with/)

By supplying some test transaction data to `getWith(testTxData)` we can get a "branch" of the current database with
the test transaction data applied. This is a very powerful way of testing future-like "what-if" scenarios

```scala
Person.name.age.getWith(<testTxData>) === ... // Persons including some new data 
```

The "test db" that such query works on is simply garbage collected when it goes out of scope. We therefore don't need
to any tear-down as we would normally need to testing with a mutable database.



### Next

[AsOf / Since...](/manual/time/asof-since)