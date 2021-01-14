---
title: Update
weight: 52
menu:
  main:
    parent: code
    identifier: code-op-update
---


# Update

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

1. find the current `age` attribute value (38) and retract that value
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
  updateTx <- Ns(liz).age(38).updateAsync

  // Get result
  result <- Person.name.age.getAsync
} yield {
  // Liz had a birthday
  result === List(("Ben", 42), ("Liz", 38))
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
object ProductsOrderDefinition {

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
