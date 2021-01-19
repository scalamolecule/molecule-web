---
title: Query
weight: 50
menu:
    main:
        parent: code
identifier: code-op-get
---

# Get

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
val list    : Future[List[(String, Int)]]                          = m(Person.name.age).getAsync
val array   : Future[Array[(String, Int)]]                         = m(Person.name.age).getAsyncArray
val iterable: Future[Iterable[(String, Int)]]                      = m(Person.name.age).getAsyncIterable
val raw     : Future[java.util.Collection[java.util.List[AnyRef]]] = m(Person.name.age).getAsyncRaw
```



### With entity id

Attributes of some entity are easily fetched by applying an entity id to the first namespace in the molecule

```scala
Person(fredId).name.age.likes.get.head === List("Fred", 38, "pizza")
```
The entity id is used for the first attribute of the molecule, here `name` having entity id `fredId`.

`Person` is just the namespace for the following attributes, so that we get `:Person/name`, `:Person/age`, `:Person/likes` etc..


### 2-steps with Entity API

Molecules can get optional attributes which make them flexible to get irregular data sets. We could for instance fetch some Persons where some of them has no `likes` asserted:

```scala
Person.name.age.likes$.get === List(
  ("Fred", 38, Some("pizza")),
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

