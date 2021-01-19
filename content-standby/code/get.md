---
title: Query
weight: 80
menu:
  main:
    parent: code
    identifier: query
---

# Query with get

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/crud)


The classical Create-Read-Update-Delete operations on data are a bit different using Datomic since it never overwrites or deletes data. Facts are only _asserted_ or _retracted_ in Datomic. 

Molecule tries to bridge the vocabulary between these two worlds.


>All getters and operators below have an [asynchronous equivalent](/code/attributes/#syncasync-apis). Synchronous getters/operators are shown for brevity.

### Create

In Molecule you can either `save` a populated molecule or `insert` multiple tuples of data that match an "insert-moleceule"

#### `save`
3 facts asserted for a new entity:
```scala
Person.name("Fred").likes("pizza").age(37).save // or saveAsync
```

More on [save](/manual/crud/save/)...


#### `insert`
3 facts asserted for each of 3 new entities: 
```scala
Person.name.age.likes insert List( // or insertAsync
  ("Fred", 37, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [insert](/manual/crud/insert/)...


### Read / get

To read data from the database we call `get` on a molecule

#### `get`

```scala
Person.name.age.likes.get === List( // or getAsync
  ("Fred", 37, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [get](/manual/crud/get/)...


### Time getters

Since data is only appended in Datomic we can also go back in time to look at our data!

```scala
<molecule>.getAsOf(t)      // or getAsynAsOf(t)
<molecule>.getSince(t)     // or getAsyncSince(t)
<molecule>.getWith(txData) // or getAsyncWith(txData)
<molecule>.getHistory      // or getAsyncHistory
```
These are such cool features that we have a whole section about [time](/manual/time)...



### "Update" (retract + assert)

In Datomic an update retracts the old fact and asserts the new fact. 

For convenience, Molecule lets you think in terms of a classical "update" although two operations are performed in the background. The old fact is still in the database and available with the time getters.

#### `update`

```scala
Person(fredId).likes("pasta").update // Retracts "pizza" and Asserts "pasta"

// Current value is now "pasta"
Person(fredId).likes.get.head === "pasta"
```


More on [update](/manual/crud/update/)...


### Retract ("delete")

As mentioned, data is not deleted in Datomic. So it would be outright wrong to say that we "delete" data. Therefore, Molecule uses the Datomic terminology of "retracting" data which is like saying "this is no longer valid". Retracted data is no longer showing up when we query with `get` but it will be visible with the time getters.

To retract individual attribute values of an entity we apply an empty value and `update`:

```scala
// Retract what Fred likes by applying an empty value
Person(fredId).likes().update

// Fred now doesn't have any preference (but his old preference is still in history)
Person(fred).name.likes$.get.head === ("Fred", None)
```

#### `retract` entity

We can retract an entity (a group of facts with a common entity id) by calling `retract` on a `Long` entity id:
```scala
fredId.retract

// Fred is retracted from current view (but still in history)
Person(fredId).name.likes.get === Nil
```
More on [retract](/manual/crud/retract/)...


## Save

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/Save.scala)

In Molecule we can populate a molecule with data and save it:

```scala
Person.name("Fred").likes("pizza").age(37).save
```

This will assert 3 facts in Datomic that all share the id of the new entity id `fredId` that is automatically created by Datomic:

```scala
fredId    :Person/name    "Fred"
fredId    :Person/likes   "pizza"
fredId    :Person/age     37
```

### Type-safety

Type-safety is guaranteed since each attribute only accepts values of its defined type.


### Asynchronous save

All transactional operators have an asynchronous equivalent. Saving data asynchronously with `saveAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we map over the result of saving asynchronously:

```scala
// Map over a Future
Person.name("Fred").likes("pizza").age(37).saveAsync.map { tx => // tx report from successful save transaction
  // (synchronous get)
  Person.name.likes.age.get.head === ("Fred", "pizza", 37)
}
```

Or we could defer the resolution of the `Future`

```scala
val futureSave: Future[TxReport] = Person.name("Fred").likes("pizza").age(37).saveAsync
for {
  _ <- futureSave
  result <- Person.name.likes.age.getAsync
} yield {
  // Data was saved
  result.head === ("Ben", "pizza", 37)
}
```

For brevity, the following examples use the synchronous `save` operation.



### Related data

We can even save related date in the same operation
```scala
Person.name("Fred").likes("pizza").age(37).Home.street("Baker St. 7").city("Boston").save
```
In this case, 6 facts will be asserted for the entity of Fred. A `:Person/home` ref attribute will resolve to the value of a new Address entity with id `addrId` and thereby establish the relationship from Fred to his Address:

```
fredId    :Person/name    "Fred"
fredId    :Person/likes   "pizza"
fredId    :Person/age     37
fredId    :Person/home    addrId
addrId    :Addr/street    "Baker St. 7"
addrId    :Addr/city      "Boston"
```
And we could go on with further relationships...

### Cardinality many values

Cardinality many attributes like for instance `hobbies` hold `Set`s of values. But we can apply values in various ways:
```scala
// Vararg
Person.hobbies("golf", "chess").save

// Set
val set = Set("golf", "chess")
Person.hobbies(set).save

// Seq/List
val seq = Seq("golf", "chess")
Person.hobbies(seq).save
```


### Optional values

An optional value (`optionalLikes`) from a form submission for instance can be applied to an optional attribute (`likes$`):

```scala
Person.name(aName).likes$(optionalLikes).age(anAge).save
```
When this molecule is saved, only 2 facts will be asserted:

```
fredId    :Person/name    "Fred"
fredId    :Person/age     37
```

This is different from SQL where we would save a NULL value in a `likes` column.

Molecule lets us fetch data sets with optional facts asserted for an attribute as optional values:

```scala
Person.name.likes$.age.get === List(
  ("Fred", None, 37),
  ("Pete", Some("sushi"), 17)
)
```

If we specifically want to find Persons that have no `likes` asserted we can say
```scala
Person.name.likes_(nil).age.get === List(
  ("Fred", 37)
  // Pete not returned since he likes something
)
```
.. or
```scala
Person.name.likes$(None).age.get === List(
  ("Fred", None, 37)
  // Pete not returned since he likes something
)
```

## Insert

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/Insert.scala)

Data can be inserted by making a molecule that matches the values of each row.

One row of data can be applied directly with matching arguments

```scala
Person.name.likes.age.insert("Fred", "pizza", 37)
```

Multiple rows of data can be applied as any `Iterable` of tuples of data each matching the molecule attributes:
```scala
Person.name.likes.age insert List(
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)
```

### Type-safety

Type-safety is guaranteed since the type of each tuple of data is enforced by the compiler to conform to the molecule type.

If the data set is not accepted type-wise, then either the molecule needs to be adjusted to match the type of data rows. Or, the data set might be irregular and have some variable size of tuples or varying types within tuples that need to be sorted out.



### Asynchronous insert

All transactional operators have an asynchronous equivalent. Inserting data asynchronously with `insertAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we insert data as argument list/tuples asynchronously:

```scala
// Insert single row of data with individual args
val singleInsertFuture: Future[TxReport] = Person.name.likes.age.insertAsync("Fred", "pizza", 37)

// Insert Iterable of multiple rows of data
val multipleInsertFuture: Future[TxReport] = Person.name.likes.age insertAsync List(
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)

for {
  _ <- singleInsertFuture
  _ <- multipleInsertFuture
  result <- Person.name.likes.age.getAsync
} yield {
  // Both inserts applied
  result === List(
    ("Fred", "pizza", 37),
    ("Lisa", "pizza", 7),
    ("Ben", "pasta", 5)
  )
}
```
For brevity, the following examples use the synchronous `save` operation.


### Optional values

`null` values are not allowed as data-input values whereas Optional values are:

```scala
Person.name.likes$.age insert List(
  ("Fred", None, 37),
  ("Pete", Some("sushi"), 17)
)
```
As with `save`, None values are simply not asserted. No `likes` value is asserted for Fred in the example above.


### Related data

Related data can be inserted

```scala
Person.name.likes$.age.Home.street.city insert List(
  ("Fred", None, 37, "Baker St. 7", "Boston"),
  ("Pete", Some("sushi"), 17, "Sunset Boulevard 1042", "Foxville")
)
```
When the Fred entity is created, a Baker St Address entity is also created and a relationship from Fred to that Address entity is created. The same for Pete, and so on...


### Composite data

Data with associative relationships can be inserted with a Composite molecule
```scala
Article.name.author + Tag.name.weight insert List(
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)
```
Note how each sub-molecule type-safely corresponds to each sub-tuple of data.

Up to 22 sub-molecules can be associated in a single Composite which allows for wide data sets to be saved with up to 22 x 22 = 484 attributes per row of data!


### Data variables
Likewise we might often have the whole data set saved in a variable that we can insert too:

```scala
val data = List(
  ("Fred", None, 37),
  ("Pete", Some("sushi"), 17)
)
Person.name.likes$.age insert data
```

### Entity ids

If we have some previously saved entities we can also insert their ids. Here we save some Address entity ids with the ref attribute `home`:

```scala
val bakerSt7 = Addr.street("Baker St. 7").city("Boston").save.eid
val sunsetB = Addr.street("Sunset Boulevard 1042").city("Foxville").save.eid

Person.name.likes$.age.home insert List(
  ("Fred", Some("pizza"), 37, bakerSt7),
  ("Lisa", None, 12, bakerSt7),
  ("Ben", Some("pasta"), 7, bakerSt7),
  ("Pete", Some("sushi"), 17, sunsetB)
)
```


### Insert-molecule as template

We can assign an Insert-molecule to a variable in order to re-use it as a temple to insert data with various inputs.

```scala
// Insert-molecule
val insertPerson = Person.name.likes.age.insert

// Insert 3 persons re-using the insert-molecule
insertPerson("Fred", "pizza", 37)
insertPerson("Lisa", "pizza", 12)
insertPerson("Ben", "pasta", 7)
```

We can use insert-molecules with data assigned to variables too:

```scala
val insertPerson = Person.name.likes.age.insert

val personsData = List(
  ("Fred", "pizza", 37),
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)

// Re-use insert-molecules with larger data sets 
insertPerson(personsData)
```

## Get

We get/read data from the database by calling `get` on a molecule. This returns a `List` of tuples that match the molecule attributes (except for arity-1):


```scala
val persons1attr: List[String] = Person.name.get

val persons2attrs: List[(String, Int)] = Person.name.age.get

val persons3attrs: List[(String, Int, String)] = Person.name.age.likes.get

// Etc.. to arity 22
```

Data can be returned in 5 different formats:

```scala
// List for convenient access to smaller data sets
val list : List[(String, Int)] = m(Person.name.age).get

// Mutable Array for fastest retrieval and traversing
val array: Array[(String, Int)] = m(Person.name.age).getArray

// Iterable for lazy traversing with an Iterator
val iterable: Iterable[(String, Int)] = m(Person.name.age).getIterable

// Raw untyped Datomic data if data doesn't need to be typed
val raw: java.util.Collection[java.util.List[AnyRef]] = m(Person.name.age).getRaw
```

### Async API


Molecule provide all operations both synchronously and asynchronously, so the 5 getter methods also has equivalent asynchronous methods returning data in a Future:
```scala
val list    : Future[List[(String, Int)]] = m(Person.name.age).getAsync
val array   : Future[Array[(String, Int)]] = m(Person.name.age).getAsyncArray
val iterable: Future[Iterable[(String, Int)]] = m(Person.name.age).getAsyncIterable
val raw     : Future[java.util.Collection[java.util.List[AnyRef]]] = m(Person.name.age).getAsyncRaw
```



### With entity id

Attributes of some entity are easily fetched by applying an entity id to the first namespace in the molecule

```scala
Person(fredId).name.age.likes.get.head === List("Fred", 37, "pizza")
```
The entity id is used for the first attribute of the molecule, here `name` having entity id `fredId`.

`Person` is just the namespace for the following attributes, so that we get `:Person/name`, `:Person/age`, `:Person/likes` etc..


### 2-steps with Entity API

Molecules can get optional attributes which make them flexible to get irregular data sets. We could for instance fetch some Persons where some of them has no `likes` asserted:

```scala
Person.name.age.likes$.get === List(
  ("Fred", 37, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```
An alternative is to get the data in 2 steps:

1. Define the shape of the data set with a molecule and get the entity ids
2. Get attribute values using the entity api

This way we can for instance in the first step get the mandatory data (`name` and `age`) with a molecule and then in a second step ask for optional data (`likes`) for each entity:

```scala
// Step 1
val seed: List[(Long, String, Int)] = Person.e.name.age.get

// Step 2
val data: List[(String, Int, Option[String])] = seed.map { case (e, name, age) =>
  // Add optional `likes` value via entity api
  (name, age, e[String](":Person/likes"))
}
```
For this simple example, the original molecule with an optional `likes` attribute would of course have been sufficient and more concise. But for more complex interconnected data this approach can be a good extra tool in the toolbox.



### Render strategies...

Various render strategies could rather easily be added if necessary. In that case, please file an issue with a description of a desired format.


## Update

An "update" is a two-step process in Datomic:

1. Retract old fact
2. Assert new fact

Datomic doesn't overwrite data. "Retract" is a statement that says "this data is no longer current" which means that it won't turn up when you query for it _as of now_. If you query for it _as of before_ you will see it!

Being able to see how data develops over time is a brillant core feature of Datomic. We don't need to administrate cumbersome historical changes manually. Auditing is built-in at the core of Datomic.


### Cardinality one

We need an entity id to update data so we get it first with the special generic Molecule attribute `e` (for _**e**ntity_):

```scala
val fredId = Person.e.name_("Fred").get.head
```

#### `apply(<value>)`
Now we can update the entity Fred's age by applying the new value 39 to the `age` attribute:

```scala
Person(fredId).age(39).update
```

Molecule uses the `fredId` to

1. find the current `age` attribute value (37) and retract that value
2. assert the new `age` attribute value 39


#### `apply()`

We can retract ("delete") an attribute value by applying no value
```scala
Person(fredId).age().update
```
This will retract the `age` value 39 of the Fred entity.



### Cardinality-many

A cardinality many attribute like `hobbies` holds a `Set` of values:

```scala
Person(fredId).hobbies.get.head === Set("golf", "cars")
```


### Only unique values

Since cardinality many attributes hold Sets, Molecule rejects duplicate values in all cardinality-many operations shown below.

Duplicate variables or primitive values will throw a compile-time error. Duplicate values that can only be discovered at runtime will throw an IllegalArgumentException at runtime.

### Operations on card-many attrs

All operations generally accepts varargs or `Lists` of the type of the attribute. So even if the attribute holds a `Set` of values we can also supply a `Seq` of values.


#### `assert` ("add")


```scala
// Assert vararg values
Person(fredId).hobbies.assert("walks", "jogging").update
Person(fredId).hobbies.get.head === Set("golf", "cars", "walks", "jogging")

// Add Set of values
Person(fredId).hobbies.assert(Set("skating", "biking")).update
Person(fredId).hobbies.get.head === Set("golf", "cars", "walks", "jogging", "skating", "biking")
```


#### `replace` (retract + assert)

Since Cardinality-many attributes have multiple values we need to specify which of those values we want to replace:

```scala
// Cardinality-many attribute value updated
Person(fredId).hobbies.replace("skating" -> "surfing").update
Person(fredId).hobbies.get.head === Set("golf", "cars", "walks", "jogging", "surfing", "biking")
```
Here we tell that the "skating" value should now be "surfing". The old value is retracted and the new value asserted so that we can go back in time and see what the values were before our update.

Update several values in one go

```scala
Person(fredId).hobbies(
  "golf" -> "badminton",
  "cars" -> "trains").update
Person(fredId).hobbies.get.head === Set("badminton", "trains", "walks", "jogging", "surfing", "biking")
```


#### `retract`

We can retract one or more values from the set of values

```scala
Person(fredId).hobbies.retract("badminton").update
Person(fredId).hobbies.get.head === Set("trains", "walks", "jogging", "surfing", "biking")

Person(fredId).hobbies.retract(List("walks", "surfing")).update
Person(fredId).hobbies.get.head === Set("trains", "jogging", "biking")
```
The retracted facts can still be tracked in the history of the database.


#### `apply`

As with cardinality one attributes we can `apply` completely new values to an attribute. All old values are retracted. It's like an "overwrite all" operation except that we can see the retracted old values in the history of the database.


```scala
Person(fredId).hobbies("meditaion").update
Person(fredId).hobbies.get.head === Set("meditation")
```

#### `apply()`

Applying nothing (empty parenthesises) retracts all values of an attribute

```scala
Person(fredId).hobbies().update
Person(fredId).hobbies.get === Nil
```


### Update multiple entities


Update multiple entities in one transaction so that they have the same values:
```scala
// Both Bob and Ann turned 25 and became cool club members
Person(bobId, annId).age(25).memberOf("cool club").update
```

See [tests](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/UpdateMultipleEntities.scala) for variations of updating multiple entities.


### Asynchronous update

All transactional operators have an asynchronous equivalent. Updating data asynchronously with `updateAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we map over the result of updating an entity asynchronously:

```scala
for {
  // Initial data
  saveTx <- Person.name.age insertAsync List(("Ben", 42), ("Liz", 37))
  List(ben, liz) = saveTx.eids

  // Update Liz' age
  updateTx <- Ns(liz).age(37).updateAsync

  // Get result
  result <- Person.name.age.getAsync
} yield {
  // Liz had a birthday
  result === List(("Ben", 42), ("Liz", 37))
}
```

### Updating multiple entities asynchronously

```scala
// Initial data
Ns.str.int insertAsync List(
  ("a", 1),
  ("b", 2),
  ("c", 3),
  ("d", 4)
) map { tx => // tx report from successful insert transaction
  // 4 inserted entities
  val List(a, b, c, d) = tx.eids
  Ns.int.get === List(
    ("a", 1),
    ("b", 2),
    ("c", 3),
    ("d", 4)
  )

  // Update multiple entities asynchronously
  Ns(a, b).int(5).updateAsync.map { tx2 => // tx report from successful update transaction
    // Current data
    Ns.int.get.sorted === List(
      ("a", 5),
      ("b", 5),
      ("c", 3),
      ("d", 4)
    )
  }
}
```

### Tests
Update-tests with:

- [Various types](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/crud/update)
- [Map attribute](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/crud/updateMap)
- [Multiple attributes](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/UpdateMultipleAttributes.scala)
- [Multiple entities](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/UpdateMultipleEntities.scala)


## Retract

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/Retract.scala)


In Datomic, retracting a fact saves a retracted Datom with the `added` operation set to `false`. Retracted datoms will not show up in queries of the current data. But if you query historical data with for instance [asOf](/manual/time/asof-since/) you'll see what the value was before it was retracted. This mechanism provides Datomic with built-in auditing of all of its data since none is deleted!

### Retract facts

To retract individual attributre values apply empty parenthesises to the attribute we want to retract and then update the molecule:


```scala
Community(belltownId).name().category().update
```
Here we retracted the `name` and `category` attribute values of the Belltown Community entity:


### Retract an entity

To delete a whole entity with all its attribute values we can call `retract` on a `Long` entity id

```scala
fredId.retract
```
All attributes having the entity id `fredId` are retracted.

### Add Tx meta data to retraction on entity id

Associate transaction meta data to a retraction on an entity id
```scala
fredId.Tx(MyUseCase.name("Terminate membership")).retract
```

We can then afterwards use the tx meta data to get information of retracted data:
```scala
// Who got their membership terminated and when?
Person.e.name.t.op(false).Tx(MyUseCase.name_("Termminate membership")).getHistory === List(
  (fredId, "Fred", t3, false) // Fred terminated his membership at transaction t3 and was retracted
)
```


### Retract multiple entities

Alternatively we can use the `retract` method (available via `import molecule.imports._`)

```scala
retract(fredId)
```
This `retract` method can also retract multiple entities

```scala
val eids: List[Long] = // some entity ids 

// Retract all supplied entity ids
retract(eids)
```

### Add Tx meta data to retraction of multiple entity ids

.. and even associate transaction meta data to the retraction
```scala
// Retract multiple entity ids and some tx meta data about the transaction
retract(eids, MyUseCase.name("Terminate membership"))
```
Again, we can then afterwards use the tx meta data to get information of retracted data:
```scala
// Who got their membership terminated and when?
Person.e.name.t.op(false).Tx(MyUseCase.name_("Termminate membership")).getHistory === List(
  (fredId, "Fred", t3, false), // Fred terminated his membership at transaction t3 and was retracted
  (lisaId, "Lisa", t5, false)  // Lisa terminated her membership at transaction t5 and was retracted
)
```


### Retract component entity

If a ref attribute is defined with the option `isComponent` then it "owns" its related entities - or "subcomponents", as when an `Order` own its `LineItem`s.

```scala
object ProductsOrderDataModel {

  trait Order {
    val id    = oneInt
    val items = many[LineItem].isComponent // Order owns its line items
  }

  trait LineItem {
    val product = oneString
    val price   = oneDouble
    val qty     = oneInt
  }
}
```

If we retract such `Order`, then all of its related `LineItem`s are also retracted:

```scala
orderId.retract // All related `LineItem`s are also retracted!

// or
retract(orderId)
```
Component entities are recursively retracted! So if `LineItem` would have had subcomponents then those would have been retracted too when the order was retracted - and so on down the hierarchy of subcomponents.




### Asynchronous retract

All transactional operators have an asynchronous equivalent.

Retracting entities asynchronously uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we map over the result of retracting an entity asynchronously (in the inner mapping):

```scala
// Initial data
Ns.int.insertAsync(1, 2).map { tx => // tx report from successful insert transaction
  // 2 inserted entities
  val List(e1, e2) = tx.eids
  Ns.int.get === List(1, 2)

  // Retract first entity asynchronously
  e1.retractAsync.map { tx2 => // tx report from successful retract transaction
    // Current data
    Ns.int.get === List(2)
  }
}
```
Retract multiple entities asynchronously:
```scala
// Initial data
Ns.int.insertAsync(1, 2, 3).map { tx => // tx report from successful insert transaction
  // 2 inserted entities
  val List(e1, e2, e3) = tx.eids
  Ns.int.get === List(1, 2, 3)

  // Retract first entity asynchronously
  retractAsync(Seq(e1, e2)).map { tx2 => // tx report from successful retract transaction
    // Current data
    Ns.int.get === List(3)
  }
}
```
