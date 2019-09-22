---
title: "Update"
weight: 60
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud/get-json
next: /manual/crud/retract
down: /manual/transactions
---

# Update data

An "update" is a two-step process in Datomic:

1. Retract old fact
2. Assert new fact

Datomic doesn't overwrite data. "Retract" is a statement that says "this data is no longer current" which means 
that it won't turn up when you query for it _as of now_. If you query for it _as of before_ you will see it! 

Being able to see how data develops over time is a brillant core feature of Datomic. We don't need to administrate 
cumbersome historical changes manually. Auditing is built-in at the core of Datomic.


## Cardinality one

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



## Cardinality-many

A cardinality many attribute like `hobbies` holds a `Set` of values:

```scala
Person(fredId).hobbies.get.head === Set("golf", "cars")
```


### Only unique values

Since cardinality many attributes hold Sets, Molecule rejects duplicate values in all cardinality-many operations
shown below.

Duplicate variables or primitive values will throw a compile-time error. Duplicate values that 
can only be discovered at runtime will throw an IllegalArgumentException at runtime.

### Operations on card-many attrs

All operations generally accepts varargs or `Lists` of the type of the attribute. So even if the attribute holds
a `Set` of values we can also supply a `Seq` of values.


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
Here we tell that the "skating" value should now be "surfing". The old value is retracted and the new 
value asserted so that we can go back in time and see what the values were before our update.

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

As with cardinality one attributes we can `apply` completely new values to an attribute. All old values are retracted. 
It's like an "overwrite all" operation except that we can see the retracted old values in the history of 
the database.


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


## Update multiple entities


Update multiple entities in one transaction so that they have the same values:
```scala
// Both Bob and Ann turned 25 and became cool club members
Person(bobId, annId).age(25).memberOf("cool club").update
```

See [tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/UpdateMultipleEntities.scala)
for variations of updating multiple entities.


## Asynchronous update

All transactional operators have an asynchronous equivalent. Updating data asynchronously with 
`updateAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`. 

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
- [Multiple attributes](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/UpdateMultipleAttributes.scala)
- [Multiple entities](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/UpdateMultipleEntities.scala)


### Next

[Delete / retract...](/manual/crud/retract)