---
title: "Attributes"
weight: 40
menu:
  main:
    parent: manual
    identifier: attributes
---

# Attributes

Molecules are built by chaining attributes together with the builder pattern. Here are some groups of different attribute types and their use with links to their manual pages:

<br>

[Attribute basics](/manual/attributes/basics), return types, arity, cardinality ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala))
```
val persons: List[(String, Int)] = Person.name.age.get
```
<br>

[Mandatory/Tacit/Optional](/manual/attributes/modes) attributes ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala))
```
Person.name.age.get  // all required values              ("mandatory value")
Person.name.age_.get // age is required but not returned ("tacit value")
Person.name.age$.get // optional age returned            ("optional value")
```
<br>

[Map attributes](/manual/attributes/mapped) - mapped attribute values
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap))
```
Person.id.name.get.head === (
  1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```
<br>

[Expressions](/manual/attributes/expressions) - filter attribute values with expressions
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression))
```
Person.age(42)                  // equality
Person.name.contains("John")    // fulltext search
Person.age.!=(42)               // negation
Person.age.<(42)                // comparison
Person.age(nil)                 // nil (null)
Person.name("John" or "Jonas")  // OR-logic
```
<br>

[Aggregates](/manual/attributes/aggregates) - aggregate attribute values
([tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))
```
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```
<br>

[Parameterize](/manual/attributes/parameterized) - re-use molecules and let Datomic cache queries and optimize performance
(tests: 
[1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1),
[2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2),
[3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3))
```
val person = m(Person.name(?).age(?))

// Re-use `person` input molecule
val Johan  = person("John", 33).get.head
val Lisa   = person("Lisa", 27).get.head
```


# Building molecules

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala)

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data
structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces
are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.



## Sync API for returning data

Molecule returns all result sets as a List of tuples of values (with `get`).

```
val persons: List[(String, Int)] = Person.name.age.get
```
Data can be returned in 5 different formats:

```
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
```
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

```
// Insert maximum of 22 facts and return the created entity id
val eid = Ns.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.insert(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
).eid

// Use entity id to continue adding more values for the same entity if necessary
Ns.a23.a24.a25.insert(eid, 23, 24, 25)
```

Likewise we can retrieve more than 22 values in 2 steps

```
val first22values = Ns(eid).a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.get

// Use entity id to continue adding more values
val next3values = Ns(eid).a23.a24.a25.get
```

## Cardinality

The attributes `name`, `age` and `gender` that we saw above are typical cardinality-one attributes each with one value.

Datomic also has cardinality-many attributes that have a `Set` of values. This means that the same value cannot be saved
multiple times, or that only unique values are saved. An example could be a cardinality-many attribute `hobbies` of a `Person`:

```
Person.name.hobbies.get.head === ("Fred", Set("Trains", "Chess"))
```

In the [Update](/manual/crud/update/) section of CRUD we will see how multiple values are managed with Molecule.



# 3 attribute modes

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

#### 1. Mandatory `attr`

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them.

_Note that this is different from selecting rows from a sql table where you can also get null values back!_

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated
with them then this query will _not_ return those entities:

```
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


#### 2. Tacit `attr_`

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values.
We call the un-returning attributes "tacit attributes".

If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age
values, then we can add an underscore `_` after the `age` Attribute:

```
val names = Person.name.age_.get
```
This will return names of person entities having both a name and age Attribute set. Note how the age values are no
longer returned from the type signatures:

```
val persons: List[(String, Int)] = Person.name.age.get
val names  : List[String]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures
we look for.


#### 3. Optional `attr$`

[tests..](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala)


If an attribute value is only sometimes set, we can ask for it's optional value by adding a dollar sign `$` after the attribute:

```
val names: List[(String, Option[String], String)] = Person.firstName.middleName$.lastName.get
```
That way we can get all person names with or without middleNames. As you can see from the return type, the middle
name is wrapped in an `Option`.

