---
date: 2018-11-14T02:13:50Z
title: "Changelog"
---

# Molecule changelog {#top}


[Github releases](https://github.com/scalamolecule/molecule/releases)

Main changes:

- 2018-12-02 v0.16.1 [Sbt-molecule 0.7.0, bootstrap speed optimization](#21)
- 2018-11-25 v0.16.0 [Async API + tx functions](#20)
- 2018-10-25 v0.15.0 [10x-100x Compilation speed boost](#19)
- 2018-09-06 v0.14.0 [Scala docs and semantic updates](#18)
- 2017-11-13 v0.13.0 [Native Json output](#17)
- 2017-05-28 v0.12.0 [Optional values in save-molecules](#16)
- 2017-05-19 v0.11.0 [Time API](#15)
- 2016-10-19 v0.10.0 [Entity selection retrieval and manipulation](#14)
- 2016-07-24 v0.9.0 [Bidirectional references](#13)
- 2016-06-13 v0.8.0 [Composites & Tx meta data](#12)
- 2016-06-04 v0.7.0 [sbt-molecule plugin code generation](#11)
- 2016-05-30 v0.6.2 [Various improvements and bug fixes](#10)
- 2016-04-25 v0.6.1 [Keyed attribute maps](#9)
- 2015-12-14 v0.5.0 [Map Attributes - Multilingual support](#8)
- 2015-11-14 v0.4.3 [Nested adjacent references](#7)
- 2015-11-13 v0.4.2 [Back references in nested inserts/gets](#6)
- 2015-10-28 v0.4.1 [Differentiating between optional values in insert/get](#5)
- 2015-10-25 v0.4.0 [Optional values (like Null)](#4)
- 2015-10-04 v0.3.0 [Nested data structures](#3)
- 2014-12-25 v0.2.0 [Implemented Day-Of-Datomic and MBrainz](#2)
- 2014-07-02 v0.1.0 [Initial commit - Seattle tutorial](#1)




## [☝](#top) Sbt-molecule plugin compilation speed optimizations {#21}
_2018-11-25 v0.16.1_

Minor upgrade to match [sbt-molecule](https://github.com/scalamolecule/sbt-molecule) plugin v0.7.0. 

When compiling a molecule project with `sbt compile`, compilation of boilerplate
code is now 4-5x faster than with v0.6.2. Using lazy vals for attributes and
methods for reference namespaces did the trick.

Since attributes are now defined as lazy vals in boilerplate code, we can no longer
override some super/base structure where we earlier saved the doc comments. Alternatively
we could add doc comments to all arities of boilerplate attributes. But that would be
a lot of redundancy. Combined with the massive compilation speed improvement doc comments
are therefore skipped.  


## [☝](#top) Async API + tx functions {#20}
_2018-11-25 [v0.16.0](https://github.com/scalamolecule/molecule/releases/tag/v0.16.0)_

### Sync/AsyncAPIs

All getter methods now have an asynchronous equivalent method that returns a Scala Future with the data:

- `get` / `getAsync` - Default List of typed tuples for convenient access to smaller data sets.
- `getArray` / `getAsyncArray` - Array of typed tuples for fast retrieval and traversing of large data sets.
- `getIterable` / `getAsyncIterable` - Iterable of typed tuples for lazy evaluation of data
- `getJson` / `getAsyncJson` - Json formatted result data
- `getRaw` / `getAsyncRaw` - Raw untyped data from Datomic

All transactional operations on molecules now similarly have async implementations returning a Future with
a `TxReport` containing data about the transaction.

- `save` / `saveAsync`
- `insert` / `insertAsync`
- `update` / `updateAsync`
- `retract` / `retractAsync`

### Tx functions

Molecule now implements typed transaction functions.

Within the tx function you have access to the transaction database value so that you can ensure any 
synchronization constraints before returning the resulting tx statements to be transacted. To abort
the whole transaction if a constraint is not met, simply throw an exception. Either all tx statements 
will transact successfully or none will thereby ensuring atomicity of the transaction.

Any complexity of logic can be performed within a tx function as long as no side effects are produced
(like trying to update the database within the tx method body).


### Tx function definitions

Tx functions in Datomic are untyped (takes arguments of type `Object`). But Molecule allows you to 
define typed tx methods inside a `@TxFns`-annotated object that will automatically create equivalent "twin" 
functions with the shape that Datomic expects and save them in the Datamic database transparently for you.

```scala
@TxFns
object myTxFns {
  // Constraint check before multiple updates
  def transfer(from: Long, to: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
    // Validate sufficient funds in from-account
    val curFromBalance = Ns(from).int.get.headOption.getOrElse(0)
    
    if (curFromBalance < amount)
      // Throw exception to abort the whole transaction
      throw new TxFnException(s"Can't transfer $amount from account $from having a balance of only $curFromBalance.")

    // Calculate new balances
    val newFromBalance = curFromBalance - amount
    val newToBalance = Ns(to).int.get.headOption.getOrElse(0) + amount

    // Update accounts
    Ns(from).int(newFromBalance).getUpdateTx ++ Ns(to).int(newToBalance).getUpdateTx
  }
}
```

Tx function are invoked in application code with the `transact` or `transactAsync` method:
```scala
transact(transfer(fromAccount, toAccount, 20))
```
`transact` (or `transactAsync`) is a macro that analyzes the tx function signature to be able to
invoke its generated twin method within Datomic.


### Bundled transactions

If the transactional logic is not dependent on access to the transaction database value, 
multiple "bundled" tx statements can now be created by adding molecule tx statements to
one of the bundling `transact` or `transactAsync` methods:

```scala
transact(
  // retract
  e1.getRetractTx,
  // save
  Ns.int(4).getSaveTx,
  // insert
  Ns.int.getInsertTx(List(5, 6)),
  // update
  Ns(e2).int(20).getUpdateTx
)
```
Tx statement getters for the molecule operations are used to get the tx statements to be transacted
in one transaction. As with tx functions, only all tx statements will atomically transact or none will
if there is some transactional error. 


### Composite syntax
Composite molecules are now tied together with `+` instead of `~`.
```scala
m(Ref2.int2 + Ns.int).get.sorted === Seq(
  (1, 11),
  (2, 22)
)
```
This change was made to avoid collision with the upcoming splice operator `~` in the next
major version of Scala/Dotty (see [MACROS: THE PLAN FOR SCALA 3](https://www.scala-lang.org/blog/2018/04/30/in-a-nutshell.html))

Composite inserts previously had its own special insert method but now shares syntax 
with other inserts

```scala
val List(e1, e2) = Ref2.int2 + Ns.int insert Seq(
  // Two rows of data
  (1, 11),
  (2, 22)
) eids
``` 




## [☝](#top) 10x-100x Compilation speed boost! {#19}
_2018-10-25 [v0.15.0](https://github.com/scalamolecule/molecule/releases/tag/v0.15.0)_

The core macro transformation engine has been re-written from the ground up and 
[micro-optimizations](http://www.lihaoyi.com/post/MicrooptimizingyourScalacode.html#bit-packing) 
applied wherever possible. This has resulted in dramatic compilation speed improvements, some several orders of magnitude!

Macro materialization of molecules earlier produced a lot of code that has now been moved out to static methods. An absolute
minimal amount of code is now generated minimizing the job of the macros and the compiler. As an example, the 
[Seattle tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTests.scala)
file sometimes took up to 70 seconds to compile and now average around 4 seconds. Some long molecules with close to 22
attributes almost never finished compiling but now take about 2 seconds to compile! This is good news since users of 
Molecule can therefore now freely create as large molecules as they please without any speed penalty.


### 5 optimized getter groups

Type casting of returned data from Datomic was earlier not completely optimized. Taking advice from
[Haoyi's "Benchmarking Scala Collections"](http://www.lihaoyi.com/post/BenchmarkingScalaCollections.html)
Molecule now also returns super fast mutable pre-allocated Arrays of typed data for large data sets. 

Json has also been thoroughly optimized to build as fast as possible directly from raw Datomic data. 

So Molecule now offers 5 optimized [getter groups](http://www.scalamolecule.org/api/molecule/action/get/):

- `get` - Default List of typed tuples for convenient access to smaller data sets.
- `getArray` - Array of typed tuples for fast retrieval and traversing of large data sets.
- `getIterable` - Iterable of typed tuples for lazy evaluation of data
- `getJson` - Json formatted result data
- `getRaw` - Raw untyped data from Datomic

Each getter group comes with all time-related variations:

- `get`
- `getAsOf(t)`
- `getSince(t)`
- `getWith(txData)`
- `getHistory` (only implemented for List getter)







## [☝](#top) Scala docs and semantic updates {#18}
_2018-09-06 [v0.14.0](https://github.com/scalamolecule/molecule/releases/tag/v0.14.0)_

Major overhaul of Molecule:

### Thorough Scala docs API documentation

All relevant public interfaces have been documented in the new [Scala docs](http://www.scalamolecule.org/api/molecule). Shortcuts to Scala docs sub packages and documents are also directly available via "API docs" in the menu on the [Molecule website](http://www.scalamolecule.org).

To aid constructing molecules in your code, all attributes defined now also have Scala docs automatically defined by the [sbt-molecule plugin](https://github.com/scalamolecule) upon compilation.

 
### Input molecules correctly implemented 

Input molecules are now semantically correctly implemented and thoroughly tested ([1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1), [2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2), [3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3)).

### Interfaces updated and streamlined

The standard getters return Lists of tuples of type-casted tuples.

- `get`
- `getAsOf(t)`
- `getSince(t)`
- `getHistory`
- `getWith(txData)`

If large data sets are expected, an Iterable of tuples of lazily type-cased tuples can be retrieved instead. 
Data is type-casted on each call to `next` on the iterator.

- `getIterable`
- `getIterableAsOf(t)`
- `getIterableSince(t)`
- `getIterableHistory`
- `getIterableWith(txData)`

If typed data is not required we can get the raw untyped java collections of Lists of objects.

- `getRaw`
- `getRawAsOf(t)`
- `getRawSince(t)`
- `getRawHistory`
- `getRawWith(txData)`

### Breaking changes

The whole directory layout of the Molecule library has been re-arranged and optimized, including many interfaces. So you might have to change some method names if you have used earlier versions of Molecule.









## [☝](#top) Native Json output {#17}
_2017-11-13 [v0.13.0](https://github.com/scalamolecule/molecule/releases/tag/v0.13.0)_

We can now get data in json format directly from the database by calling `getJson` on a molecule. So instead of converting tuples of data to json with some 3rd party library we can call `getJson` and pass the json data string directly to an Angular table for instance.

Internally, Molecule builds the json string in a StringBuffer directly from the raw data coming from Datomic 
(with regards to types being quoted or not). This should make it the fastest way of supplying json data when needed.


### Flat data

Normal "flat" molecules creates json with a row on each line in the output:

```scala
Person.name.age.getJson ===
  """[
    |{"name": "Fred", "age": 38},
    |{"name": "Lisa", "age": 35}
    |]""".stripMargin
```


### Composite data

Composite data has potential field name clashes so each sub part of the composite is rendered 
as a separate json object tied together in an array for each row: 
 
```scala
m(Person.name.age ~ Category.name.importance).getJson ===
  """[
    |[{"name": "Fred", "age": 38}, {"name": "Marketing", "importance": 6}],
    |[{"name": "Lisa", "age": 35}, {"name": "Management", "importance": 7}]
    |]""".stripMargin
``` 
Note how a field `name` appears in each sub object. Since the molecule is defined in client code it is presumed that 
the semantics of eventual duplicate field names are also handled by client code.


### Nested data

Nested date is rendered as a json array with json objects for each nested row: 

```scala
(Invoice.no.customer.InvoiceLines * InvoiceLine.item.qty.amount).getJson ===
  """[
    |{"no": 1, "customer": "Johnson", "invoiceLines": [
    |   {"item": "apples", "qty": 10, "amount": 12.0},
    |   {"item": "oranges", "qty": 7, "amount": 3.5}]},
    |{"no": 2, "customer": "Benson", "invoiceLines": [
    |   {"item": "bananas", "qty": 3, "amount": 3.0},
    |   {"item": "oranges", "qty": 1, "amount": 0.5}]}
    |]""".stripMargin
```





## [☝](#top) Optional values in save-molecules {#16}
_2017-05-28 [v0.12.0](https://github.com/scalamolecule/molecule/releases/tag/v0.12.0)_

Often, form submissions have some optional field values. Molecule now allow us to `save` molecules with both mandatory and optional attributes.

We could for instance have `aName`,  `optionalLikes` and `anAge` values from a form submission that we want to save. We can now apply those values directly to a mandatory `name` attribute, an optional `likes$` attribute (`$` appended makes it optional) and a mandatory `age` attribute of a save-molecule and then save it:

```scala
Person
  .name(aName)
  .likes$(optionalLikes)
  .age(anAge)
  .save
```

We can also, as before, _insert_ the data using an "insert-molecule" as a template:

```scala
Person.name.likes$.age.insert(
  aName, optionalLikes, anAge
)
```
It can be a matter of taste if you want to `save` or `insert` - but now you can choose :-)








## [☝](#top) Time API {#15}
_2017-05-19 [v0.11.0](https://github.com/scalamolecule/molecule/releases/tag/v0.11.0)_

Datomic has some extremely powerful time functionality that Molecule now makes available in an intuitive way: 

### Ad-hoc time queries

Ad-hoc time queries against our database can now be made with the following time-aware getter methods on a molecule:


```scala
Person.name.age.getAsOf(t) === ... // Persons as of a point in time `t`

Person.name.age.getSince(t) === ... // Persons added after a point in time `t`

Person(fredId).age.getHistory === ... // Current and previous ages of Fred in the db
```

`t` can be a transaction entity id (`Long`), a transaction number (`Long`) or a `java.util.Date`.

If we want to test the outcome of some transaction without affecting the production db we can apply transaction data to the `getWith(txData)` method:

```scala
Person.name.age.getWith(
  // Testing adding some transactional data to the current db
  Person.name("John").age(42).saveTx, // Save transaction data
  Person.name.age.insertTx(
    List(("Lisa", 34), ("Pete", 55)) // Insert transaction data
  ),
  Person(fredId).age(28).updateTx, // Update transaction data
  someEntityId.retractTx // Retraction transaction data
) === ... // Persons from db including transactions tested 
```

By adding the `Tx` suffix to the standard molecule commands (`save`, `insert`, `update` or `retract`) 
we can get the transactional data that those operations would normally transact directly. Here, `<command>Tx` methods on transaction molecules just return the transaction data so that we can apply it to the `getWith(txData)` method. This make it convenient for us to ask speculative questions like "what would I get if I did those transactions". 

For more complex test scenarios we can now use a test database:

### Test db

All molecules expect an implicit connection object to be in scope. If we then set a temporary test database on such `conn` object we can subsequentially freely perform tests against this temporary database as though it was a "branch" (think git).

When the connection/db goes out of scope it is simply garbage collected automatically by the JVM. At any point we can also explicitly go back to continuing using our live production db.
 
To make a few tests with a "branch" of our live db we can now do like this:

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

// Then go back to production state
conn.useLiveDb

// Production state is unchanged!
Person(fredId).name.age.get.head === ("Fred", 27)
```

### Test db with domain classes

When molecules are used inside domain classes we want to test the domain operations also without affecting the state of our production database. And also ideally without having to create mockups of our domain objects. This is now possible by setting a temporary test database on the implicit connection object that all molecules expect to be present in their scope - which includes the molecules inside domain classes.

When we test against a temporary database, Molecule internally uses the `with` function of Datomic to apply transaction data to a "branch" of the database that is simply garbage collected when it goes out of scope!

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

We can apply the above approach with several time views of our database:
 
```scala
conn.testDbAsOfNow
conn.testDbAsOf(t)
conn.testDbSince(t)
conn.testWith(txData)
```

This make it possible to run arbitrarily complex test scenarios directly against our production data at any point in time without having to do any manual setup or tear-down of mock domain/database objects!





## [☝](#top) Entity selection retrieval and manipulation {#14}
_2016-10-19 [v0.10.0](https://github.com/scalamolecule/molecule/releases/tag/v0.10.0)_

Molecule now allows retrieving attribute values of selected entities:

``` scala
val List(e1, e2, e3) = Ns.int.insert(1, 2, 3).eids

// Use selected entity ids to access attributes of those entities
Ns(e1, e2).int.get === List(1, 2)

// Or use a variable with a collection of entity ids
val e23 = Seq(e2, e3)
Ns(e23).int.get === List(2, 3)
```

Likewise we can update attribute values of selected entities (group editing):

``` scala
val List(a, b, c) = Ns.str.int insert List(("a", 1), ("b", 2), ("c", 3)) eids

// Apply value to attribute of multiple entities
Ns(a, b).int(4).update
Ns.str.int.get.sorted === List(("a", 4), ("b", 4), ("c", 3))
```

See more examples [here](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/attr/EntitySelection.scala) and [here](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/manipulation/UpdateMultipleEntities.scala).

Datomic encourages multi-step queries where you find some entities ids with one query and then pass those ids on as input to the following query. Since we don't have the cost of round-trips to a database server, this is a powerful technique that Molecule now supports with ease.





## [☝](#top) Bidirectional references {#13}
_2016-07-24 [v0.9.0](https://github.com/scalamolecule/molecule/releases/tag/v0.9.0)_

Major upgrade of Molecule introducing _Bidirectional_ references. 

Normal Datomic references are unidirectional. If we add a friend reference from Ann to Ben

``` scala
Person.name("Ann").Friends.name("Ben").save
```

Then we can naturally query to get friends of Ann

``` scala
Person.name_("Ann").Friends.name.get === List("Ben")
```

But what if we want to find friends of Ben? This will give us nothing:

``` scala
Person.name_("Ben").Friends.name.get === List()
```

Instead we would have to think backwards to get the back reference

``` scala
 Person.name.Friends.name_("Ben").get === List("Ann")
```

If we want to traverse deeper into a friendship graph we would have to query both forward and backward for each step in the graph which would quickly become a pain. With Molecules new bidirectional references we can uniformly query from both ends:

``` scala
 Person.name_("Ann").Friends.name.get === List("Ben")
 Person.name_("Ben").Friends.name.get === List("Ann")
```

Please see [Bidirectional refs](http://www.scalamolecule.org/manual/query/bidirectional%20refs/) for more information and the [Gremlin graph examples](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/).
- This release also adds support for BigInts and BigDecimals. Only bytes is not supported now due to the limited 
  capabilities this type has in Datomic.
- Input molecules can now also include nested data structures.







## [☝](#top) Composites & Tx meta data {#12}
_2016-06-13 [v0.8.0](https://github.com/scalamolecule/molecule/releases/tag/v0.8.0)_

[Composites](http://www.scalamolecule.org/manual/query/composites/) and [Transaction meta data](http://www.scalamolecule.org/manual/query/txMetaData/) are two new major functionalities added to Molecule.

Merge up to 22 sub-molecules as a Composite. Composite inserts create entities with data of attributes type-checking against each sub-molecule. Sub-molecules don't need to be related. The created entity is what ties it all together which is a core feature of Datomic that sets it apart from table/join-thinking.

An obvious candidate for composites is cross-cutting data that could be applied to any entity of our domain - like Tags. No need anymore to litter all parts of your domain with refs to Tags. Keep your domain namespaces intrinsic and compose instead!

Even transaction meta data can now be applied:

``` scala
// Insert comma-separated molecules that become one composite molecule
insert(
  Article.name.author, Tag.name.weight
)(
  // 2 entities/"rows" created 
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)(
  // Transaction meta data is saved with the tx entity created
  MetaData.submitter_("Brenda Johnson").usecase_("AddReviews")
)
```

And we can then query the composed molecule:

``` scala
// Important articles submitted by Brenda Johnson
// In queries we tie composite molecule parts together with `~`
m(Article.name.author ~ Tag.weight.>=(4).tx_(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```




## [☝](#top) sbt-molecule plugin code generation {#11}
_2016-06-04 [v0.7.0](https://github.com/scalamolecule/molecule/releases/tag/v0.7.0)_

Previous fragile build system now standardized to use the new [sbt-molecule plugin](https://github.com/scalamolecule/sbt-molecule) to generate and package Molecule boilerplate code.

Several updates to [scalamolecule.org](http://www.scalamolecule.org).






## [☝](#top) Various improvements and bug fixes {#10}
_2016-05-30 [v0.6.2](https://github.com/scalamolecule/molecule/releases/tag/v0.6.2)_

- HList support dropped (was very incomplete anyway)
- [Support for inserting more than 22 facts for a single namespace](https://github.com/scalamolecule/molecule/commit/70a951c5742ffe3925d777c0dfc284f223414445)
- [Null references](https://github.com/scalamolecule/molecule/commit/2c18c1e0255e0179d07272179ad1140d748abeec) now supported
- [Boilerplate in/out separated to avoid too big boilerplate files](https://github.com/scalamolecule/molecule/commit/508e93753eb1e8080a24b40af6fb9ab8c229d0aa)
- [Negation now takes Seq of values](https://github.com/scalamolecule/molecule/commit/a4680b2ec483ec66ce4128e4cdd9657a9ad09fdb)
- Upgrade to sbt 0.13.11 using a build.sbt file



## [☝](#top) Keyed attribute maps {#9}
_2016-04-25 [v0.6.1](https://github.com/scalamolecule/molecule/releases/tag/v0.6.1)_

Each defined attribute map now adds an additional attribute with a "K" appended to the attribute name. This "Keyed attribute map" expects a key and will then return the single value type instead of a Map of key/values:

``` Scala
// Normal attribute map returning maps of key/values
Ns.int.greetings("en").get === List(
  (1, Map("en" -> "Hi there")),
  (2, Map("en" -> "Hi")),
  (3, Map("en" -> "Hello"))
)

// "Keyed attribute map" returning values directly (matching the key)
Ns.int.greetingsK("en").get === List(
  (1, "Hi there"),
  (2, "Hi"),
  (3, "Hello")
)
```

This makes it more convenient to get to the values directly instead of having to extract them from returned Maps.







## [☝](#top) Map Attributes - Multilingual support {#8}
_2015-12-14 [v0.5.0](https://github.com/scalamolecule/molecule/releases/tag/v0.5.0)_

Molecule now supports an easy way to handle multilingual values; say, names of greetings in different languages for various entities - all with one "Map Attribute" like `greetings`:

``` Scala
Ns.int.greetings insert List(
  (1, Map("en" -> "Hi there")),
  (2, Map("fr" -> "Bonjour", "en" -> "Hi")),
  (3, Map("en" -> "Hello")),
  (4, Map("da" -> "Hej"))
)
```

Other types are supported too. So we could for instance have timezones as Float values for different countries etc.

Each key is prepended to its value and saved as a cardinality many value ("en@Hi there") in Datomic. When we then retrieve the values, Molecule automatically splits the values into typed key/value pairs in a map so that we can conveniently continue to work with them in Scala:

``` Scala
Ns.int.greetings.get === List(
  (1, Map("en" -> "Hi there")),
  (2, Map("fr" -> "Bonjour", "en" -> "Hi")),
  (3, Map("en" -> "Hello")),
  (4, Map("da" -> "Hej"))
)

// English values only (find all values having "en" as key)
Ns.int.greetings("en").get === List(
  (1, Map("en" -> "Hi there")),
  (2, Map("en" -> "Hi")),
  (3, Map("en" -> "Hello"))
)

// English values containing the substring "Hi"
Ns.int.greetings("en" -> "Hi").get === List(
  (1, Map("en" -> "Hi there")),
  (2, Map("en" -> "Oh, Hi"))
)

// All values containing the substring "He"
Ns.int.greetings("_" -> "He").get === List(
  (3, Map("en" -> "Hello")),
  (4, Map("da" -> "Hej"))
)
```





## [☝](#top) Nested adjacent references {#7}
_2015-11-14 [v0.4.3](https://github.com/scalamolecule/molecule/releases/tag/v0.4.3)_

If we insert a nested data structure like this:

``` Scala
m(Ns.str.Refs1 * (Ref1.int1.Refs2 * Ref2.int2)) insert List(
  ("a", List(
    (1, List(11)))),
  ("b", List(
    (2, List(21, 22)),
    (3, List(31)))))
```

we can now query for each `Ns.str` paired with all its nested `Ref2.int2`'s:

``` Scala
m(Ns.str.Refs1 * Ref1.Refs2.int2).get === List(
  ("a", List(11)),
  ("b", List(22, 21, 31)))
```

Resolution is recursive, so Molecule handles arbitrarily deep nested data structures.





## [☝](#top) Back references in nested inserts/gets {#6}
_2015-11-13 [v0.4.2](https://github.com/scalamolecule/molecule/releases/tag/v0.4.2)_

Now we can insert/get related values from multiple related namespaces in one go:

``` scala
m(lit_Book.title.Author.name._Book.Reviewers * gen_Person.name) insert List(
  ("book", "John", List("Marc"))
)
m(lit_Book.title.Author.name._Book.Reviewers * gen_Person.name).get === List(
  ("book", "John", List("Marc"))
)
```

From the `Book` namespace we reference first the `Author` namespace and then go back to the `Book` namespace again with the `_Book` back reference so that we can then reference the `Reviewers` namespace. Like multiple connected "arms" of a complex molecule.







## [☝](#top) Differentiating between optional values in insert/get {#5}
_2015-10-28 [v0.4.1](https://github.com/scalamolecule/molecule/releases/tag/v0.4.1)_

Single optional values should be retrievable. But when inserting we want to avoid creating orphan referenced entities with no asserted attribute values. Example from [Relations](https://github.com/scalamolecule/molecule/blob/8e5c0437c245201f5e174cb407ae74cdbc654257/coretest/src/test/scala/molecule/Relations.scala#L196-L236) test:

``` scala
"No mandatory attributes after card-one ref" in new CoreSetup {
  m(Ns.str.Ref1.int1) insert List(
    ("a", 1),
    ("b", 2))

  // Ok to ask for an optional referenced value
  m(Ns.str.Ref1.int1$).get === List(
    ("a", Some(1)),
    ("b", Some(2)))

  // But in insert molecules we don't want to create referenced orphan entities
  (m(Ns.str.Ref1.int1$).insert must throwA[RuntimeException]).message === "Got the exception" +
    "java.lang.RuntimeException: " +
    "[output.Molecule:modelCheck (4)] Namespace `Ref1` in insert molecule" + 
    "has no mandatory attributes. Please add at least one."
}
```









## [☝](#top) Optional values (like Null) {#4}
_2015-10-25 [v0.4.0](https://github.com/scalamolecule/molecule/releases/tag/v0.4.0)_

Molecule now supports optional values:

``` scala
val names = Person.firstName.middleName$.lastName.get map { 
  case (firstName, Some(middleName), lastName) => s"$firstName $middleName $lastName" 
  case (firstName, None, lastName)             => s"$firstName $lastName" 
}
```

Also when inserting varying data sets, this comes in handy, like for instance if middle names are sometimes present and sometimes not:

``` scala
Person.firstName.middleName$.lastName insert List(
  ("John", None, "Doe"),
  ("Lisa", Some("van"), "Hauen")
)
```

See more examples of even deeply nested optional values in the [OptionalValues](https://github.com/scalamolecule/molecule/blob/80765bfbae17657cd0fa0fa099a6ba22f41a179f/coretest/src/test/scala/molecule/OptionalValues.scala) tests.







## [☝](#top) Nested data structures {#3}
_2015-10-04 [v0.3.0](https://github.com/scalamolecule/molecule/releases/tag/v0.3.0)_

[Products and Orders](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/ProductsAndOrders.scala) shows how Molecule can now retrieve nested data structures at up to 10 levels deep (more can be implemented if necessary). This means that you don't need to manually organize the flat output from Datomic into hierarchical data structures yourself when you query for one-many or many-many data. Molecule does it for you.





## [☝](#top) Implemented Day-Of-Datomic and MBrainz {#2}
_2014-12-25 [v0.2.0](https://github.com/scalamolecule/molecule/releases/tag/v0.2.0)_

- Nested molecules
- Transitive molecules
- Simplifying API





## [☝](#top) Initial commit - [Seattle tutorial](/resources/tutorials/seattle/) {#1}
_2014-07-02 [v0.1.0](https://github.com/scalamolecule/molecule/releases/tag/v0.1.0)_

