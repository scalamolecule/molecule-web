---
title: "Time"
weight: 90
menu:
  main:
    parent: code
    identifier: time
---

# Time

Datomic has powerful ways of accessing all the immutable data that accumulates over time in the database:

![](/img/page/time/all.png)


The 5 time getters offer us valuable insight into the database from various perspectives:

{{< bootstrap-table "table table-bordered" >}}
| Semantics | Sync | Async |
| :- | :- | :- |
| Current view of the database                       | get                                  | getAsync              |                     
| How the db looked after tx2 was transacted        | [getAsOf(t2)](/code/time/#asof)      | getAsyncAsOf(t2)      |
| Db with only data since (excluding) tx2 until now | [getSince(t2)](/code/time/#since)    | getAsyncSince(t2)     |
| All transactions over time                         | [getHistory](/code/time/#history)    | getAsyncHistory       |
| "What if"-look into the future of Now + tx4data    | [getWith(tx4data)](/code/time/#with) | getAsyncWith(tx4data) |
{{< /bootstrap-table >}}

The synchronous getters return a `List` of tuples and the asynchronous a `Future` of `List` of tuples.


### json getters
  
We can also return the data as json:

```
getJson                getAsyncJson
getJsonAsOf(t2)        getAsyncJsonAsOf(t2)
getJsonSince(t2)       getAsyncJsonSince(t2)
getJsonHistory         getAsyncJsonHistory
getJsonWith(tx4data)   getAsyncJsonWith(tx4data)
```


### Limit returned data

The amount of data returned can be limited by adding a max row parameter (to any of the getters above):
```scala
val some30persons               = Person.name.age.get(30)
val some20personsAsOfNov5       = Person.name.age.getAsOf(nov5date, 20) 
val some10personsAddedSinceNov5 = Person.name.age.getSince(nov5date, 10)

// The `with` methods have the limit parameter as their first argument since 
// the last argument is a vararg for the test data.
val some25personsWithNewData = Person.name.age.getWith(25, <txTestData>) 
```

`getHistory(n: Int)` (with a limit) is not implemented since the whole history data set normally needs to be retrieved and sorted to give chronological meaningful information.









## Point in time

The two methods `getAsOf(t)` and `getSince(t)` takes a _point in time_ in the database. Each transaction in Datomic is an _entity_ with an id like all other entities saved in the database. The transaction id, or `tx`, and its equivalent transaction value `t`, both of type `Long`, or a `java.util.Date` can be used as a _point in time_ for the time getters. 

`t` is a `Long` value that Datomic creates along the transaction id. It's not necessarily continuous like a traditional auto-increment id, so you can't expect a previous or next `t` to represent a transaction.

### Getting a point in time from a transaction 
When we perform a transaction, a Molecule `TxReport` is returned with information about the transaction result. We can get the three _points in time_ mentioned above from this:

````scala
val txReport = Person.name("bob").save
val t        = txReport.t
val tx       = txReport.tx
val date     = txReport.inst // `inst` for Datomics `instant` type (java.util.Date)
````


### Getting a point in time with a molecule

Another way to get a point in time is to add one or the other generic point-in-time attribute to a molecule. We could for instance ask at what point in time, bob's name was asserted:

```scala
val t     : Long = Person.name_("bob").t.get
val tx    : Long = Person.name_("bob").tx.get
val txInst: Date = Person.name_("bob").txInst.get
```

### Using a clock time/date

Or we can simply apply a `java.util.Date` of interest, like `getAsOf(nov5at1015am)`.

### Applying a point in time

As a convenience, we can also simply pass the txReport itself as a "_point in time_".

So, here are , and we end being able to call for instance `getAsOf(...)` in 4 different ways:

```scala
Person.name.getAsOf(t)
Person.name.getAsOf(tx)
Person.name.getAsOf(txInst) // or some clock time
Person.name.getAsOf(txReport)
```
Whenever `t` is mentioned in the following text, you can also think of `tx`, `txInst` (`Date`) or `txReport`.









## AsOf

`getAsOf(t)` and `getSince` are complementary functions that either get us a snapshop of the database at some point in time or a current snapshot filtered with only changes after a point in time. Like before/after scenarios.


Calling `getAsOf(t)` on a molecule gives us the data as of a certain point in time like `t2`:


![](/img/page/time/as-of.png)


Let's look at an example of a database that has 3 transactions:

```scala
val txReport1 = Person.name("Fred").likes("pizza").save
val fred      = txReport1.eid // getting created entity id from tx report
val t1        = txReport1.t   // getting t from tx report

val t2 = Person(fred).likes("sushi").update.t 

val t3 = Person.name("Lisa").likes("thai").update.t 
```

We can then get the db value as it looked like at 3 points in time:
```scala
Person.name.likes.getAsOf(t1) === List(("Fred", "pizza"))
Person.name.likes.getAsOf(t2) === List(("Fred", "sushi"))
Person.name.likes.getAsOf(t3) === List(("Fred", "sushi"), ("Lisa", "thai"))
```










## Since


As a complementary function to `getAsOf(t)` we have `getSince(t)` that gives us a snapshot of the current database filtered with only changes added _after/since_ `t`:

![](/img/page/time/since.png)

Contrary to the getAsOf(t) method, the `t` is _not_ included in `getSince(t)`. Using our example we can ask of accumulated values since various points in time:

```scala
// since t1 (not included): t2 + t3 accumulated
Person.name.likes.getAsOf(t1) === List(("Fred", "sushi"), ("Lisa", "thai"))

// since t2 (not included): t3 
Person.name.likes.getAsOf(t2) === List(("Lisa", "thai"))

// since t3 (not included): nothing since t3
Person.name.likes.getAsOf(t3) === List()
```

As you can see, it can be valuable to ask "What happened since t2?" and get the answer "'Lisa liked thai' was added". 










## History

The history perspective gives us all the assertions and retractions that has happened in the lifetime of the database(!)


![](/img/page/time/history.png)


Theoretically we could ask for all historical values (although Datomic doesn't allow a full scan of the whole database):

```scala
// Theoretical full scan (just to show all datoms in our example)
Person.e.a.v.t.op.getHistory === List(
  fred, ":Person/name",  "Fred",  t1, true,
  fred, ":Person/likes", "pizza", t1, true,
  
  fred, ":Person/likes", "pizza", t2, false,
  fred, ":Person/likes", "sushi", t2, true,
  
  lisa, ":Person/name",  "Lisa",  t3, true,
  lisa, ":Person/likes", "thai",  t3, true  
)
```

We use the generic Molecule attributes `e`, `a`, `v`, `t`/`tx`/ `txInst`, `op` to retrieve the Datom values: 
{{< bootstrap-table "table table-bordered" >}}
`e`|`a`|`v`|`t` / `tx`/ `txInst` |`op`
:-:|:-:|:-:|:-:|:-:
Entity|Attribute|Value|Transaction|Operation<br>Assert (true) / Retract (false)
`Long`|`String`|`Any`|`Long`/`Long`/`Date`|`Boolean`
{{< /bootstrap-table >}}


### History of an entity

Now let's extract some useful information, like how the `fred` entity has changed over time:

```scala
Person(fred).e.a.v.t.op.getHistory === List(
  fred, ":Person/name",  "Fred",  t1, true,
  fred, ":Person/likes", "pizza", t1, true,
  
  fred, ":Person/likes", "pizza", t2, false,
  fred, ":Person/likes", "sushi", t2, true
)
```
As you see, we have now filtered the history database to only contain datoms with entity id `fred`. A Molecule convenience method lets us apply an entity id to the initial Namespace. We could also apply it to the generic `e` attribute and get the same result.

Another thing is also, that order of the returned data set is not guaranteed, so we will normally need to sort the output, in this case by transaction value, then operation to get the desired order (in the following examples we'll skip sorting for clarity though):

```scala
Person.e(fred).a.v.t.op.getHistory.sortBy(r => (r._4, r._5)) === List(
  fred, ":Person/name",  "Fred",  t1, true,
  fred, ":Person/likes", "pizza", t1, true,
  
  fred, ":Person/likes", "pizza", t2, false,
  fred, ":Person/likes", "sushi", t2, true
)
```

We can involve specific attributes like `Person.like`:

_How has Fred's taste developed?:_

```scala
Person(fred).like_.v.t.op.getHistory === List(
  "pizza", t1, true,  // pizza
  "pizza", t2, false, // no longer pizza
  "sushi", t2, true   // sushi
)
```
Since we declared which attribute to look for, there was no reason to return it, and we made it tacit with an underscore to `like_`.

_What has Fred liked?:_

```scala
Person(fred).like_.v.op_(true).getHistory === List(
  "pizza",  // pizza
  "sushi"   // sushi
)
```

### History of an attribute

We could also follow the values of an attribute for multiple entities:

_Who liked what and when?_
```scala
Person.e.like_.v.txInst.op_(true).getHistory === List(
  fred, "pizza", date1,
  fred, "sushi", date2,
  lisa, "thai",  date3  
)
```

_What was disliked and when?_
```scala
Person.like_.v.txInst.op_(false).getHistory === List(
  "sushi", date2
)
```

### History with Tx meta data

We can even track historical [transaction meta data](/code/transactions/#tx-meta-data), here with an example from the [Provenance example](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/examples/datomic/dayOfDatomic/Provenance.scala) in the Day-of-Datomic test suite:

_Who changed the title and when?_

```scala
Story.url_(ecURL).title.op.tx
  .Tx(MetaData.usecase.User.firstName).getHistory === List(
  ("ElastiCache in 6 minutes", true, stuTxId, "AddStories", "Stu"), // Stu adds story
  ("ElastiCache in 6 minutes", false, edTxId, "UpdateStory", "Ed"), // Ed updates title
  ("ElastiCache in 5 minutes", true, edTxId, "UpdateStory", "Ed")   // Ed updates title
)
```

_"What titles did Ed retract and in what use cases?"_
```scala
Story.url_(ecURL).title.op_(false)
  .Tx(MetaData.usecase.User.firstName_("Ed")).getHistory === List(
  ("ElastiCache in 6 minutes", "UpdateStory")
)
```

Note the `Tx` (with capital T) that initiates adding a transaction meta data molecule. This is information that is added to the _transaction entity_, independently of the main data - like cross-cutting audit data.

Another example of transaction meta data could be internal company auditing data like "use case id", "who took this step in the use case", "what state is the use case currently in" etc. Being able to model and query such auditing data back in time seems like an extremely valuable feature. 










## With

We can make a "fake" transaction with `getWith(txData)` and see how the database would look then:



![](/img/page/time/with.png)

The current database is filtered in-memory with the applied extra transaction data. This is a very powerful way of testing future-like "what-if" scenarios. We don't need to do any clean-up since all transaction data is automatically garbage-collected.

#### with save tx

Continuing our example we could add another person and see how the database would then look. We can construct the transaction data to add with a call to `getSaveTx` on a save molecule:

```scala
Person.name.likes.getWith( 
  // "Transaction molecule" with "transact John" tx data
  Person.name("Eddy").likes("cakes").getSaveTx 
) === List(
  ("Fred", "sushi"),
  ("Lisa", "thai"),
  ("Eddy", "cakes") // Eddy correctly saved
)
```

Likewise we can test the effect of other operations:

#### with insert tx

```scala
Person.name.likes.getWith( 
  Person.name.likes.getInsertTx(
    ("John", "burger"),
    ("Sara", "french")
  ) 
) === List(
  ("Fred", "sushi"),
  ("Lisa", "thai"),
  ("John", "burger"), // John and Sara were correctly inserted
  ("Sara", "french")  
)
```

#### with update tx

```scala
Person.name.likes.getWith( 
  Person(lisa).likes("lebanese").getUpdateTx 
) === List(
  ("Fred", "sushi"),
  ("Lisa", "lebanese") // Lisa correctly now likes lebanese food
)
```

#### with retract tx

```scala
Person.name.likes.getWith( 
  fred.getRetractTx 
) === List(
  ("Lisa", "thai")
  // (Fred was correctly retracted) 
)
```

#### with multiple operations
And here we transact all the above operations as one what-if scenario:

```scala
Person.name.likes.getWith(
  Person.name("Eddy").likes("cakes").getSaveTx,
  Person.name.likes.getInsertTx(
    ("John", "burger"),
    ("Sara", "french")
  ),
  Person(lisa).likes("lebanese").getUpdateTx,
  fred.getRetractTx 
) === List(
  ("Eddy", "cakes"),   // saved
  ("John", "burger"),  // inserted
  ("Sara", "french"),  // inserted
  ("Lisa", "lebanese") // updated
                       // (fred retracted)
)
```

### Modularizing tx data

Assigning transaction molecules to variables can help us modularize tests where we could for instance be interested in seeing if various orders of transactions will produce the same result:

```scala
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
There's no limit on the amount of transaction data applied, so the most complex scenarios can be simulated.



## Test against TestDb


All molecules expect an implicit database connection object to be in scope. The connection object normal communicates with the real database, but we can ask it to communicate with an in-memory "test database" instead that accepts what-if transactional data as we saw above. But this time continuously for all ordinary transaction molecules. 

It's a bit lit a git branch where we can always go back to the master branch / the live database.

When the connection/db goes out of scope it is simply garbage collected automatically by the JVM. At any point we can also explicitly go back to continuing using our live db.

### Test against current db

To make a few tests with our filtered db we can call `conn.testDbAsOfNow`:

```scala
// Current state
Person(fredId).name.age.get.head === ("Fred", 27)

// Create "branch" of our production db as it is right now
conn.testDbAsOfNow  

// Perform multiple operations on test db
Person(fredId).name("Frederik").update
Person(fredId).age(28).update

// Verify expected outcome of operations
Person(fredId).name.age.get.head === ("Frederik", 28)

// Then go back to live db
conn.useLiveDb

// Live state is unchanged!
Person(fredId).name.age.get.head === ("Fred", 27)
```


### Test db with domain classes

When molecules are used inside domain classes we want to test the domain operations also without affecting the state of our production database. And also ideally without having to create mockups of our domain objects. This is now possible by setting a temporary test database on the implicit connection object that all molecules expect to be present in their scope - which includes the molecules inside domain classes.

When we test against a temporary filtered database, Molecule internally uses the `with` function of Datomic to apply transaction data to a filtered database that is simply garbage collected when it goes out of scope.

To make a few tests on a domain object that have molecule calls internally we can now do like this:

```scala
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

```scala
conn.testDbAsOfNow
conn.testDbAsOf(t)
conn.testDbSince(t)
conn.testWith(txData)
```

This make it possible to run arbitrarily complex test scenarios directly against our production data at any point in time without having to do any manual setup or tear-down of mock domain/database objects!