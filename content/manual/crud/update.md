---
date: 2015-01-02T22:06:44+01:00
title: "Update"
weight: 60
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/getjson
next: /docs/crud/retract
down: /docs/transactions
---

# Update data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/manipulation/)

An "update" is a two-step process in Datomic:

1. Retract old fact
2. Assert new fact

Datomic doesn't overwrite data. "Retract" is a statement that says "this data is no longer current" which means that it won't turn up when you query for it _as of now_. If you query for it _as of before_ you will see it! 

Being able to see how data develops over time is a brillant core feature of Datomic. We don't need to administrate cumbersome historial changes manually. It's all built in to Datomic.


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

All operations generally accepts varargs or `Iterables` of the type of the attribute. So even if the attribute holds
a `Set` of values we can also supply `Seq`, `List` or any `Iterable` of values.


#### `add`


```scala
// Add vararg values
Person(fredId).hobbies.add("walks", "jogging").update
Person(fredId).hobbies.get.head === Set("golf", "cars", "walks", "jogging")

// Add Set/Seq/Iterable of values
Person(fredId).hobbies.add(Set("skating", "biking")).update
Person(fredId).hobbies.get.head === Set("golf", "cars", "walks", "jogging", "skating", "biking")
```


#### `replace`

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


#### `remove`

We can remove one or more values from the set of values

```scala
Person(fredId).hobbies.remove("badminton").update
Person(fredId).hobbies.get.head === Set("trains", "walks", "jogging", "surfing", "biking")

Person(fredId).hobbies.remove(List("walks", "surfing")).update
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



### Next

[Delete / retract...](/docs/crud/retract)