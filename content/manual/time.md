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
4 different ways:


### 1. Transaction entity id 

A transaction entity id is the 4th value of Datomic quintuplets that tells us in what transaction
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


### 2. Transaction value

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


### 3. Transaction report

Each transaction returns a `TxReport` with information about the transaction and we can use the report itself as a point in time:


```scala
val txReport1 = Person.name("Fred").likes("pizza").save
val fred = txReport1.eid // Getting created entity id from tx report

val txReport2 = Person(fred).likes("sushi").update

Person(fred).likes.get === List("sushi")
Person(fred).likes.getAsOf(txReport1) === List("pizza")
```



### 4. `java.util.Date`

Lastly we can also supply a human time/date of type `java.util.Date` 

```scala
val criticalDate = new Date("2017-04-26")
Person(e5).likes.getAsOf(criticalDate) === List("pizza")
```



## Data getters


### `get` or `getAsync` - the current view

Normally we get the current state of the database with the `get` method on a molecule.

```scala
// Sync
val personsCurrently: List[(String, Int)] = Person.name.age.get

// Async - wraps data in a Future
val personsCurrentlyAsync: Future[List[(String, Int)]] = Person.name.age.getAsync
```
But we might be interested in how the data looked at another point in time:


### [☞ `getAsOf(t)`](/manual/time/asof-since/) or [`getAsyncAsOf(t)`](/manual/time/asof-since/)

When we call `getAsOf(t)` on a molecule we get the data as it looked at some point in time `t`. 

We could for instance want to know what Persons existed in the database the 5th of November:

```scala
val personsAsOfNov5 = Person.name.age.getAsOf(nov5date) 
val personsAsOfNov5Async = Person.name.age.getAsyncAsOf(nov5date) 
```


### [☞ `getSince(t)`](/manual/time/asof-since/) or [`getSince(t)`](/manual/time/asof-since/)

Likewise we might want to know what Persons have been added _after_ or _since_ 5th of November. When
we call `getSince(nov5date)` we will get a snapshot of the current
database filtered with only the data added/retracted after November 5:

```scala
val personsAddedSinceNov5 = Person.name.age.getSince(nov5date)
val personsAddedSinceNov5Async = Person.name.age.getAsyncSince(nov5date)
```


### [☞ `getHistory`](/manual/time/history/) or [`getAsyncHistory`](/manual/time/history/)

The `getHistory` can for instance tell us how a Persons age attribute value has changed over time

```scala
val currentAndPreviousAgesOfFred = Person(fredId).age.getHistory
val currentAndPreviousAgesOfFredAsync = Person(fredId).age.getAsyncHistory
```
Note that this is not a snapshot in time but a series of all assertions and retractions over time that matches the query!


### [☞ `getWith(txTestData)`](/manual/time/with/) or [`getAsyncWith(txTestData)`](/manual/time/with/)

By supplying some test transaction data to `getWith(txTestData)` we can get a "branch" of the current database with
the test transaction data applied. This is a very powerful way of testing future-like "what-if" scenarios. 

Transactional test data to be tested can be obtained by calling one of the following methods on some test-molecules:

- `<molecule>.getSaveTx`  
- `<molecule>.getInsertTx`  
- `<molecule>.getUpdateTx`  
- `<entityId>.getRetractTx`  

```scala
// Apply one or more tx test data molecules
val personsWithNewData = Person.name.age.getWith(<txTestData>*) 
val personsWithNewDataAsync = Person.name.age.getAsyncWith(<txTestData>*) 
```

The "test db" that such query works on is simply garbage collected when it goes out of scope. We therefore don't need
to do any tear-down as we would normally need to when testing with a mutable database.


## Limit returned data

The amount of data returned with 

- get
- getAsOf
- getSince
- getWith

can be limitted by adding a max row parameter:
```scala
val some30personsCurrently = Person.name.age.get(30)

val some20personsAsOfNov5Async = Person.name.age.getAsyncAsOf(nov5date, 20) 

val some10personsAddedSinceNov5 = Person.name.age.getSince(nov5date, 10)

// The `with` methods have the limit parameter as their first argument since the last argument is a vararg
val some25personsWithNewData = Person.name.age.getWith(25, <txTestData>) 
```

`getHistory(n: Int)` is not implemented since the whole data set normally needs to be sorted
to give chronological meaningful information.

#### Why not an `offSet` method for pagination?

Since Datomic has no sorting option in queries (like `ORDER BY` in sql for instance), 
we sort data in application code. This sorting could be arbitrary complex and Molecule therefore
has no "standard" sorting API implemented. 

Pagination is and example that needs sorting, and we do that in our application code on the
server as it would be done on a sql database server too except that we apply our logic on the raw data
ourselves. The limit option is therefore mainly implemented to be able to work on a smaller data set. 


### Next

[AsOf / Since...](/manual/time/asof-since)