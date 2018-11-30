---
date: 2015-01-02T22:06:44+01:00
title: "Building molecules"
weight: 10
menu:
  main:
    parent: attributes
up: /manual/attributes
prev: /manual/attributes
next: /manual/attributes/modes
down: /manual/entities
---

# Building molecules

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala)

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data
 structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```scala
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces
 are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.



## Sync API for returning data

Molecule returns all result sets as a List of tuples of values (with `get`).

```scala
val persons: List[(String, Int)] = Person.name.age.get
```
Data can be returned in 5 different formats:

```scala
// List for convenient access to smaller data sets
val list : List[(String, Int)] = m(Person.name.age).get

// Mutable Array for fastest retrieval and traversing of large data sets
val array: Array[(String, Int)] = m(Person.name.age).getArray

// Iterable for lazy traversing with an Iterator
val iterable: Iterable[(String, Int)] = m(Person.name.age).getIterable

// Json formatted string 
val json: String = m(Person.name.age).getJson

// Raw untyped Datomic data if data doesn't need to be typed
val raw: java.util.Collection[java.util.List[AnyRef]] = m(Person.name.age).getRaw
```

## Async API


Molecule provide all operations both synchronously and asynchronously, so the 5 getter methods also has
equivalent asynchronous methods returning data in a Future:
```scala
val list    : Future[List[(String, Int)]] = m(Person.name.age).getAsync
val array   : Future[Array[(String, Int)]] = m(Person.name.age).getAsyncArray
val iterable: Future[Iterable[(String, Int)]] = m(Person.name.age).getAsyncIterable
val json    : Future[String] = m(Person.name.age).getAsyncJson
val raw     : Future[java.util.Collection[java.util.List[AnyRef]]] = m(Person.name.age).getAsyncRaw
```




### Molecule max size

The size of molecules are limited to Scala's arity limit of 22 for tuples.
 
If we need to insert more than 22 attribute values we can easily do this by using the entity id to 
work with further attributes/values:

```scala
// Insert maximum of 22 facts and return the created entity id
val eid = Ns.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.insert(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
).eid

// Use entity id to continue adding more values for the same entity if necessary
Ns.a23.a24.a25.insert(eid, 23, 24, 25)
```

Likewise we can retrieve more than 22 values in 2 steps

```scala
val first22values = Ns(eid).a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.get

// Use entity id to continue adding more values
val next3values = Ns(eid).a23.a24.a25.get
```

## Cardinality

The attributes `name`, `age` and `gender` that we saw above are typical cardinality-one attributes each with one value.

Datomic also has cardinality-many attributes that have a `Set` of values. This means that the same value cannot be saved 
multiple times, or that only unique values are saved. An example could be a cardinality-many attribute `hobbies` of a `Person`:

```scala
Person.name.hobbies.get.head === ("Fred", Set("Trains", "Chess"))
```

In the [Update](/manual/crud/update/) section of CRUD we will see how multiple values are managed with Molecule.


### Next

[Mandatory/Tacit/Optional attributes...](/manual/attributes/modes)