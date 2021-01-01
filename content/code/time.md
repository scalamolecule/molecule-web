---
title: "Time"
weight: 90
menu:
  main:
    parent: code
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



### Point in time {#pointintime}

The two methods `getAsOf(t)` and `getSince(t)` takes a _point in time_ in the database that can be supplied in 4 different ways:


### 1. Transaction entity id 

A transaction entity id is the 4th value of Datomic quintuplets that tells us in what transaction this Datom/fact was asserted/retracted.
 
![](/img/time/1.png)

In Molecule code we can get this information by adding the generic `tx` attribute after an attribute:

```
Person(e5).likes.tx.get.head === ("pizza", tx4)
```
Here we get some transaction entity id `tx4` (a `Long` number) for the transaction where it was asserted that Person entity `fredId` likes pizza. 

Such transaction entity id can then be used as a point in time `t` for `getAsOf(t)` or `getSince(t)` in other queries.


### 2. Transaction value

An alternative to the transaction entity id is a "transaction value" that is an auto-incremented number that Datomic generates automatically in the background for each transaction taking place. This can be useful if we for instance want to examine "the previous" transaction.

As when getting the transaction entity id with `tx` we can get the transaction value by appending the generic Molecule attribute `t` after some attribute:

```
val someT = Person(e5).likes_.t.get.head
```
Then we could ask "was there another value in the previous transaction?"

```
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


```
val txReport1 = Person.name("Fred").likes("pizza").save
val fred = txReport1.eid // Getting created entity id from tx report

val txReport2 = Person(fred).likes("sushi").update

Person(fred).likes.get === List("sushi")
Person(fred).likes.getAsOf(txReport1) === List("pizza")
```



### 4. `java.util.Date`

Lastly we can also supply a human time/date of type `java.util.Date` 

```
val criticalDate = new Date("2017-04-26")
Person(e5).likes.getAsOf(criticalDate) === List("pizza")
```



### Data getters


### `get` or `getAsync` - the current view

Normally we get the current state of the database with the `get` method on a molecule.

```
// Sync
val personsCurrently: List[(String, Int)] = Person.name.age.get

// Async - wraps data in a Future
val personsCurrentlyAsync: Future[List[(String, Int)]] = Person.name.age.getAsync
```
But we might be interested in how the data looked at another point in time:


### [☞ `getAsOf(t)`](/manual/time/asof-since/) or [`getAsyncAsOf(t)`](/manual/time/asof-since/)

When we call `getAsOf(t)` on a molecule we get the data as it looked at some point in time `t`. 

We could for instance want to know what Persons existed in the database the 5th of November:

```
val personsAsOfNov5 = Person.name.age.getAsOf(nov5date) 
val personsAsOfNov5Async = Person.name.age.getAsyncAsOf(nov5date) 
```


### [☞ `getSince(t)`](/manual/time/asof-since/) or [`getSince(t)`](/manual/time/asof-since/)

Likewise we might want to know what Persons have been added _after_ or _since_ 5th of November. When we call `getSince(nov5date)` we will get a snapshot of the current database filtered with only the data added/retracted after November 5:

```
val personsAddedSinceNov5 = Person.name.age.getSince(nov5date)
val personsAddedSinceNov5Async = Person.name.age.getAsyncSince(nov5date)
```


### [☞ `getHistory`](/manual/time/history/) or [`getAsyncHistory`](/manual/time/history/)

The `getHistory` can for instance tell us how a Persons age attribute value has changed over time

```
val currentAndPreviousAgesOfFred = Person(fredId).age.getHistory
val currentAndPreviousAgesOfFredAsync = Person(fredId).age.getAsyncHistory
```
Note that this is not a snapshot in time but a series of all assertions and retractions over time that matches the query!


### [☞ `getWith(txTestData)`](/manual/time/with/) or [`getAsyncWith(txTestData)`](/manual/time/with/)

By supplying some test transaction data to `getWith(txTestData)` we filter the current database by applying the test transaction data. This is a very powerful way of testing future-like "what-if" scenarios. 

Transactional test data to be tested can be obtained by calling one of the following methods on some test-molecules:

- `<molecule>.getSaveTx`  
- `<molecule>.getInsertTx`  
- `<molecule>.getUpdateTx`  
- `<entityId>.getRetractTx`  

```
// Apply one or more tx test data molecules
val personsWithNewData = Person.name.age.getWith(<txTestData>*) 
val personsWithNewDataAsync = Person.name.age.getAsyncWith(<txTestData>*) 
```

The "test db" that such query works on is simply garbage collected when it goes out of scope. We therefore don't need to do any tear-down as we would normally need to when testing with a mutable database.


### Limit returned data

The amount of data returned with 

- get
- getAsOf
- getSince
- getWith

can be limitted by adding a max row parameter:
```
val some30personsCurrently = Person.name.age.get(30)

val some20personsAsOfNov5Async = Person.name.age.getAsyncAsOf(nov5date, 20) 

val some10personsAddedSinceNov5 = Person.name.age.getSince(nov5date, 10)

// The `with` methods have the limit parameter as their first argument since the last argument is a vararg
val some25personsWithNewData = Person.name.age.getWith(25, <txTestData>) 
```

`getHistory(n: Int)` is not implemented since the whole data set normally needs to be sorted to give chronological meaningful information.

#### Why not an `offSet` method for pagination?

Since Datomic has no sorting option in queries (like `ORDER BY` in sql for instance), we sort data in application code. This sorting could be arbitrary complex and Molecule therefore has no "standard" sorting API implemented. 

Pagination is and example that needs sorting, and we do that in our application code on the server as it would be done on a sql database server too except that we apply our logic on the raw data ourselves. The limit option is therefore mainly implemented to be able to work on a smaller data set. 

## AsOf / Since

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time)

`getAsOf(t)` and `getSince` are complementary functions that either get us a snapshop of the database at some point in time or a current snapshot filtered with only changes after a point in time. Like before/after scenarios.


##" AsOf

[AsOf test...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetAsOf.scala)

Calling `getAsOf(t)` on a molecule gives us the data as of a certain point in time like `t4`:

![](/img/time/as-of.png)

<br>

As we saw in [point in time](/manual/time#pointintime), a `t` can be either a transaction entity id like `txE4`, a transaction number `t4`, the resulting transaction report `tx4` from some transactional operation or a `java.util.Date` like `date4`. So we could get to the same data in 4 different ways:

```
Person.name.age.getAsOf(txE4) === ... // Persons as of transaction entity id `txE4` (inclusive)
 
Person.name.age.getAsOf(t4) === ... // Persons as of transaction value `t4` (inclusive) 

Person.name.age.getAsOf(tx4) === ... // Persons as of transaction report `tx4` (inclusive) 

Person.name.age.getAsOf(date4) === ... // Persons as of some Date `date4` (inclusive) 
```

Note that `t` is "inclusive" meaning that it is how the database looked right _after_ transaction `txE4`/`t4`/`tx4`/`date4`.

### AsOf APIs

Data AsOf some point in time `t` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `t` can be any of:

- Transaction entity id (`Long`)
- Transaction number (`Long`)
- Transaction report (`molecule.facade.TxReport`)
- Date (`java.util.Date`)

Combine the needed return type with some representation of `t` and optionally a row limit by calling one of the corresponding `AsOf` implementations. All return type/parameter combinations have a synchronous and asynchronous implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-3 column ">
        <ul>
            <li><code>getAsOf(t)</code> (List)</li>
            <li><code>getArrayAsOf(t)</code></li>
            <li><code>getIterableAsOf(t)</code></li>
            <li><code>getJsonAsOf(t)</code></li>
            <li><code>getRawAsOf(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsOf(t, limit)</code> (List)</li>
            <li><code>getArrayAsOf(t, limit)</code></li>
            <li><code>getJsonAsOf(t, limit)</code></li>
            <li><code>getRawAsOf(t, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncAsOf(t)</code> (List)</li>
            <li><code>getAsyncArrayAsOf(t)</code></li>
            <li><code>getAsyncIterableAsOf(t)</code></li>
            <li><code>getAsyncJsonAsOf(t)</code></li>
            <li><code>getAsyncRawAsOf(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncAsOf(t, limit)</code> (List)</li>
            <li><code>getAsyncArrayAsOf(t, limit)</code></li>
            <li><code>getAsyncJsonAsOf(t, limit)</code></li>
            <li><code>getAsyncRawAsOf(t, limit)</code></li>
        </ul>
    </div>
</div>

`getIterableAsOf(t, limit)` and `getAsyncIterableAsOf(t, limit)` are not implemented since the data is lazily evaluated with calls to `next` on the `Iterator`.


### Since
[Since tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetSince.scala)

As a complementary function to `getAsOf(t)` we have `getSince(t)` that gives us a snapshot of the current database filtered with only changes added _after/since_ `t`:

![](/img/time/since.png)

<br>

Contrary to the getAsOf(t) method, the `t` is _not_ included in `getSince(t)`.

`t` can be either a transaction entity id like `txE4`, a transaction number `t4`, the resulting transaction report `tx4` from some transactional operation or a `java.util.Date` like `date4`. So we could get to the same data in 4 different ways:

```
Person.name.age.getSince(txE4) === ... // Persons added since/after transaction entity id `txE4` (exclusive)
 
Person.name.age.getSince(t4) === ... // Persons added since/after transaction value `t4` (exclusive) 

Person.name.age.getSince(tx4) === ... // Persons added since/after transaction report `tx4` (exclusive)

Person.name.age.getSince(date4) === ... // Persons added since/after some Date `date4` (exclusive) 
```




### Since APIs

Data Since some point in time `t` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `t` can be any of:

- Transaction entity id (`Long`)
- Transaction number (`Long`)
- Transaction report (`molecule.facade.TxReport`)
- Date (`java.util.Date`)

Combine the needed return type with some representation of `t` and optionally a row limit by calling one of the corresponding `Since` implementations. All return type/parameter combinations have a synchronous and asynchronous implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-3 column ">
        <ul>
            <li><code>getSince(t)</code> (List)</li>
            <li><code>getArraySince(t)</code></li>
            <li><code>getIterableSince(t)</code></li>
            <li><code>getJsonSince(t)</code></li>
            <li><code>getRawSince(t)</code></li>
        </ul>
        <ul>
            <li><code>getSince(t, limit)</code> (List)</li>
            <li><code>getArraySince(t, limit)</code></li>
            <li><code>getJsonSince(t, limit)</code></li>
            <li><code>getRawSince(t, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncSince(t)</code> (List)</li>
            <li><code>getAsyncArraySince(t)</code></li>
            <li><code>getAsyncIterableSince(t)</code></li>
            <li><code>getAsyncJsonSince(t)</code></li>
            <li><code>getAsyncRawSince(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncSince(t, limit)</code> (List)</li>
            <li><code>getAsyncArraySince(t, limit)</code></li>
            <li><code>getAsyncJsonSince(t, limit)</code></li>
            <li><code>getAsyncRawSince(t, limit)</code></li>
        </ul>
    </div>
</div>


`getIterableSince(t, limit)` and `getAsyncIterableSince(t, limit)` are not implemented since the data is lazily evaluated with calls to `next` on the `Iterator`.

The asynchronous implementations simply wraps the synchronous result in a Future as any other database server would normally do internally. The difference is that the Peer (the "database server") runs in the same process as our application code which makes it natural to do the Future-wrapping in Molecule as part of running our application.


## History

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/GetHistory.scala)


The history perspective gives us all the assertions and retractions that has happened over time.

![](/img/time/history.png)

### Assertions and retractions

Normally we get a snapshot of the database at a certain point in time. But when we call the `getHistory` method on a molecule we get all the assertions and retractions that has happened over time for the attributes of the molecule.

As an example we can imagine Fred being added in tx3 and then updated in tx6.

```
// tx 3 (save)
val result3 = Person.name("Fred").likes("pizza").save
val tx3 = result3.tx
val fred = result3.eid

// tx 6 (update)
val result6 = Person(fred).likes("pasta").update
val tx6 = result6.tx
```

The two transactions (save + update) produces the following 4 facts in the database:

![](/img/time/4.png)

### Generic attributes

#### `tx`, `op`

The 4th column in the facts schema above shows the transaction entity id that is saved with each fact. We get this value by appending the "generic attribute" `tx` after an attribute.

The 5th column shows the operation performed. `true` for added/asserted and `false` for retracted. We get this value by adding the generic attribute `op` after an attribute.

Let's see the transaction values and operations over time for the attribute `likes`:

```
Person(fred).likes.tx.op.getHistory.toSeq.sortBy(r => (r._2, r._3)) === List(
  ("pizza", tx3, true), // 2nd fact
  ("pizza", tx6, false),// 3rd fact
  ("pasta", tx6, true)  // 4th fact
)
```
Since output order is not guaranteed by Datomic we sort by transaction and then operation to get a chronological view of the historical data (Datomic of course keeps internal order). For brevity we omit the sorting in the following examples.


#### `t`, `txInstant`

Instead of getting the transaction entity id with `tx` we could also get the transaction value (an auto-incremented internal number for each transaction) with the generic attribute `t`:

```
Person(fred).likes.t.op.getHistory === List(
  ("pizza", t3, true), 
  ("pizza", t6, false),
  ("pasta", t6, true)  
)
```
.. or the time/date of the transaction with `txInstant`:

```
Person(fred).likes.txInstant.op.getHistory === List(
  ("pizza", date3, true), 
  ("pizza", date6, false),
  ("pasta", date6, true)  
)
```
.. or all at once:

```
Person(fred).likes.tx.t.txInstant.op.getHistory === List(
  ("pizza", tx3, t3, date3, true), 
  ("pizza", tx6, t6, date6, false),
  ("pasta", tx6, t6, date6, true)  
)
```

#### `a`, `v`

We can even use a generic attribute `a` for the attribute name and `v` for the value of an attribute. This allow us to for instance track changes to all atrributes of an entity:

```
Person(fred).a.v.t.op.getHistory === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true), 
  (":Person/likes", "pizza", t6, false),
  (":Person/likes", "pasta", t6, true)  
)
```

### Expressions

We can apply values to generic attributes in history queries to narrow our results:

```
// "What has been retracted for the entity `fred`"
// - Fred disliked "pizza" at date6
Person(fred).a.v.txInstant.op_(false).getHistory === List(
  (":Person/likes", "pizza", date6, false) 
)

// What happened for Fred in tx 3?
// - Fred's name and liking was asserted
Person(fred).a.v.tx(tx3).op.getHistory === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true)
)
```

### Combining with tx meta data

Things become really interesting when we combine history with tx meta data since we can then go back and see what a transaction was about and what changes were involved.

Here some examples from the [Provenance](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Provenance.scala) examples from the Day of Datomic tutorials:

_"Who created/updated stories?"_

```
Story.url_(ecURL).title.op.tx_(MetaData.usecase.User.firstName).history.get.reverse === List(
  ("ElastiCache in 6 minutes", true, "AddStories", "Stu"),  // Stu adds the story
  ("ElastiCache in 6 minutes", false, "UpdateStory", "Ed"), // retraction automatically added by Datomic
  ("ElastiCache in 5 minutes", true, "UpdateStory", "Ed")   // Ed's update of the title
)
```
And we can narrow with expressions:

_"What did Ed retract and in what use cases?"_
```
Story.url_(ecURL).title.op_(false).tx_(MetaData.usecase.User.firstName_("Ed")).getHistory === List(
  ("ElastiCache in 6 minutes", "UpdateStory") 
)
```

## With

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/GetWith.scala)

By supplying some test transaction data to `getWith(testTxData)` we filter the current database with the test transaction data applied in-memory. This is a very powerful way of testing future-like "what-if" scenarios.


![](/img/time/with.png)

We could for instance add some transaction data `tx8` to a Person molecule to see if we would get the extected persons back:

```
Person.name.likes.getWith(<tx8Data>) === ... // Persons after applying tx8 
```

### Applying transaction data

To make it easier to supply transaction data to the `getWith(txData)` method, you can simply add `Tx` to a Molecule transaction function to get some valid transaction data:

Transaction data is supplied to `getWith(txData)` by calling a transaction data getter on a molecule:

```
Person(fred).likes("sushi").getUpdateTx === List(
  [:db/retract, 17592186045445, :Person/likes, "pasta"]
  [:db/add    , 17592186045445, :Person/likes, "sushi"]
) 
```
`getUpdateTx` returns the transaction data that would have been used to update Fred. In that way we can supply this data to the `getWith(txData)` method to answer the question _"What if we updated Fred?"_.

When getting the transaction data from a simulated molecule transaction like this, we call it a "transaction molecule":
```
Person.name.likes.getWith(
  Person(fred).likes("sushi").getUpdateTx // "Transaction molecule" with tx8 data
) === List(
  ("Fred", "sushi") // Expected result if applying tx8
)
```

Fred will remain unaffected in the live database after `getWith(tx8)` has been called:

```
Person.name.likes.get.head === ("Fred", "pasta") 
```
The `getWith(txData)` works on a filtered database and is automatically garbage collected. So there is no need to set up and tear down database mockups!


### Transaction molecules

We can generate transaction test data by invoking a transactional data getter on a molecule or in the case of retraction on an entity id. The tx getters return the transactional data that the 4 transaction functions `save`, `insert`, `update` or `retract` would normally have transacted.

- `<molecule>.getInsertTx`
- `<molecule>.getSaveTx`
- `<molecule>.getUpdateTx`
- `<entityId>.getRetractTx`

Here's an example of combining transaction molecules of all types:

```
Person.name.age.getWith(
  // Transaction molecules:
  Person.name("John").age(44).getSaveTx, // John saved
  Person.name.age getInsertTx List(   // Get insert tx with supplied data
    ("Lisa", 23),                     // Lisa and Pete inserted
    ("Pete", 24)
  ),
  Person(fred).age(43).getUpdateTx,   // Fred updated
  someOtherPersonId.getRetractTx      // Some other person retracted (using id)    
) === List(
  // Expected result
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24), // Inserted
  ("Fred", 43)  // Updated
  // (other person retracted)
)
```
This allow us to test any transactions and build complex "what-if" test scenarios without affecting our live database.


### Modularizing tx data

Assigning transaction molecules to variables can help us modularize tests where we could for instance be interested in seeing if various orders of transactions will produce the same result:

```
val save    = Person.name("John").age(44).getSaveTx
val insert  = Person.name.age getInsertTx List(("Lisa", 23), ("Pete", 24))
val update  = Person(fred).age(43).getUpdateTx
val retract = someOtherPersonId.getRetractTx
    
val expectedResult = List(
    ("John", 44),
    ("Lisa", 23),
    ("Pete", 24),
    ("Fred", 43) 
)     
    
Person.name.age.getWith(save, insert, update, retract) === expectedResult 
Person.name.age.getWith(insert, update, retract, save) === expectedResult
// etc..
```
Since you can apply any number of transaction molecules, the testing options are extremely powerful.



### With APIs

Data With some `txTestData` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `txTestData` can be either:

- One or more transaction molecules, each returning `Seq[Seq[Statement]]`
- Raw transaction data from edn file (`java.util.List[_]`)

Combine the needed return type with some transactional data `txTestData` and optionally a row limit by calling one of the corresponding `With` implementations. All return type/parameter combinations have a synchronous and asynchronous implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-4 column ">
        <ul>
            <li><code>getWith(txTestData)</code> (List)</li>
            <li><code>getArrayWith(txTestData)</code></li>
            <li><code>getIterableWith(txTestData)</code></li>
            <li><code>getJsonWith(txTestData)</code></li>
            <li><code>getRawWith(txTestData)</code></li>
        </ul>
        <ul>
            <li><code>getWith(txTestData, limit)</code> (List)</li>
            <li><code>getArrayWith(txTestData, limit)</code></li>
            <li><code>getJsonWith(txTestData, limit)</code></li>
            <li><code>getRawWith(txTestData, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncWith(txTestData)</code> (List)</li>
            <li><code>getAsyncArrayWith(txTestData)</code></li>
            <li><code>getAsyncIterableWith(txTestData)</code></li>
            <li><code>getAsyncJsonWith(txTestData)</code></li>
            <li><code>getAsyncRawWith(txTestData)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncWith(txTestData, limit)</code> (List)</li>
            <li><code>getAsyncArrayWith(txTestData, limit)</code></li>
            <li><code>getAsyncJsonWith(txTestData, limit)</code></li>
            <li><code>getAsyncRawWith(txTestData, limit)</code></li>
        </ul>
    </div>
</div>


>The asynchronous implementations simply wraps the synchronous result in a Future as any
>other database server would normally do internally. The difference is that the Peer (the "database server")
>runs in the same process as our application code which makes it natural to do the Future-wrapping
>in Molecule as part of running our application.

## Testing

[TestDbAsOf](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/TestDbAsOf.scala),
[TestDbSince](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/TestDbSince.scala) and
[TestDbWith](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/TestDbWith.scala)

For more complex test scenarios we can use a "test database" where we can freely make multiple separate molecule queries against a temporary filtered database.

### Test db

All molecules expect an implicit connection object to be in scope. If we then set a temporary test database on such `conn` object we can subsequentially freely perform tests against this temporary filtered database as though it was a "branch" (think git).

When the connection/db goes out of scope it is simply garbage collected automatically by the JVM. At any point we can also explicitly go back to continuing using our live production db.

To make a few tests with our filtered db we call `conn.testDbAsOfNow`:

```
// Current state
Person(fredId).name.age.get.head === ("Fred", 27)

// Create "branch" of our production db as it is right now
conn.testDbAsOfNow  

// Perform multiple operations on test db
Person(fredId).name("Frederik").update
Person(fredId).age(28).update

// Verify expected outcome of operations
Person(fredId).name.age.get.head === ("Frederik", 28)

// Then go back to production state
conn.useLiveDb

// Production state is unchanged!
Person(fredId).name.age.get.head === ("Fred", 27)
```


### Test db with domain classes

When molecules are used inside domain classes we want to test the domain operations also without affecting the state of our production database. And also ideally without having to create mockups of our domain objects. This is now possible by setting a temporary test database on the implicit connection object that all molecules expect to be present in their scope - which includes the molecules inside domain classes.

When we test against a temporary filtered database, Molecule internally uses the `with` function of Datomic to apply transaction data to a filtered database that is simply garbage collected when it goes out of scope!

To make a few tests on a domain object that have molecule calls internally we can now do like this:

```
// Some domain object that we want to test
val domainObj = MyDomainClass(params..) // having molecule transactions inside...
domainObj.myState === "some state"

// Create "branch" of our production db as it is right now
conn.testDbAsOfNow  

// Test some domain object operations
domainObj.doThis
domainObj.doThat

// Verify expected outcome of operations
domainObj.myState === "some expected changed state"

// Then go back to production state
conn.useLiveDb

// Production state is unchanged!
domainObj.myState == "some state"
```

Since internal domain methods will in turn call other domain methods that also expects an implicit conn object then the same test db is even propragated recursively inside the chain of domain operations.


### Multiple time views

We can apply the above approach with various time views of our database:

```
conn.testDbAsOfNow
conn.testDbAsOf(t)
conn.testDbSince(t)
conn.testWith(txData)
```

This make it possible to run arbitrarily complex test scenarios directly against our production data at any point in time without having to do any manual setup or tear-down of mock domain/database objects!