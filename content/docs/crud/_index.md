---
date: 2015-01-02T22:06:44+01:00
title: "CRUD"
weight: 70
menu:
  main:
    parent: docs
    identifier: crud
up:   /docs/relationships
prev: /docs/relationships/bidirectional
next: /docs/crud/save
down: /docs/transactions
---

# CRUD

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/manipulation)


The classical Create-Read-Update-Delete operations on data are a bit different using Datomic since it never overwrites or deletes data. Facts are only
_asserted_ or _retracted_ in Datomic. 

Molecule tries to bridge the vocabulary between these two worlds:

## (Create)

In Molecule you can either `save` a populated molecule or `insert` multiple tuples of data that match an "insert-moleceule"

#### `save`
3 facts asserted for a new entity:
```scala
Person.name("Fred").likes("pizza").age(38).save
```

More on [save](/docs/crud/save/)...


#### `insert`
3 facts asserted for each of 3 new entities: 
```scala
Person.name.age.likes insert List(
  ("Fred", 38, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [insert](/docs/crud/insert/)...


## (Read)

To read data from the database we call `get` on a molecule

#### `get`

```scala
Person.name.age.likes.get === List(
  ("Fred", 38, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [get](/docs/crud/get/)...


### Time getters

Since data is only appended in Datomic we can also go back in time to look at our data!

```
<molecule>.getAsOf(t)
<molecule>.getSince(t)
<molecule>.getWith(txData) // Imagining the future
<molecule>.getHistory
```
These are such cool features that we have a whole section about [time](/docs/time)...



## Update

In Datomic an update retracts the old fact and asserts the new fact. 

For convenience, Molecule lets you think in terms of a classical "update" although two operations are performed in the background. The old fact is still 
in the database and available with the time getters.

#### `update`

```scala
Person(fredId).likes("pasta").update // Retracts "pizza" and Asserts "pasta"

// Current value is now "pasta"
Person(fredId).likes.get.head === "pasta"
```


More on [update](/docs/crud/update/)...


## (Delete)

As mentioned, data is not deleted in Datomic. So it would be outright wrong to say that we "delete" data. Therefore, Molecule goes along with the terminology of
"retracting" data which is like saying "this is no longer valid". Retracted data is no longer showing up when we query with `get` but it will be visible with the
 time getters.

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
More on [retract](/docs/crud/retract/)...



### Next

[Create / Save...](/docs/crud/save)