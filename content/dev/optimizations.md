---
title: "Optimizations"
weight: 40
menu:
  main:
    parent: dev
    identifier: dev-optimizations
---

# Optimizations



## Save on compilation time

If your code starts to have huge numbers of molecules, you can save time on compilation by importing a specialized Molecule api that sets a threshold for how many attributes are allowed in a molecule. This minimizes the amount of implicits waiting in vain to serve you and can reduce compilation time. 


So, if your longest molecule in a code file has 7 attributes, then you could for instance set the api import to allow for 7 attributes:

```scala
import molecule.datomic.api.out7._

// The above specialized import would be enough to allow this or smaller molecules:
Person.e.firstName.lastName.age.Address.street.zip.city.get
```
Varying api arities can be set for each code file. 

The Molecule library has more than 1400 tests with easily 30-100 molecules each, and we have simply set the api import to match the highest molecule arity in each file.

If you use input molecules then you can add `inX` where X is how many [inputs](/code/attributes/#input-molecules) (1, 2 or 3) your molecule expects:
```scala
import molecule.datomic.api.in2_out4._

// The above specialized import would be enough to allow this input molecule:
val personsOfAgeGender = m(Person.firstName.lastName.age_(?).gender_(?).Address.street.zip)
val male22 = personsOfAgeGender(22, "male").get
```

## Alternative collections

When using the Datomic Peer database system, the database "server" is running within your application process and not as a remote database server. This means that the complete data set is returned on every query and it is up to your application code to handle that. It gives you lower latency and greater control but also greater responsibility.


Data is by default returned as `List`s of tuples where all rows of data have been casted to the type signature of the molecule:
```scala
// List returned for convenient access to smaller data sets
val list : List[(String, Int)] = m(Person.name.age).get
```

By using the above mentioned specialized api you will also get access to the following return types when getting data: 

```scala
// Mutable Array for fast traversing and retrieval
val array: Array[(String, Int)] = m(Person.name.age).getArray

// Iterable for lazy traversing with an Iterator - casting happens on each call to `next`
val iterable: Iterable[(String, Int)] = m(Person.name.age).getIterable

// Raw untyped Datomic data
val raw: java.util.Collection[java.util.List[AnyRef]] = m(Person.name.age).getRaw
```

And the asynchronous versions follow along too.
```scala
val array   : Future[Array[(String, Int)]] = m(Person.name.age).getAsyncArray
val iterable: Future[Iterable[(String, Int)]] = m(Person.name.age).getAsyncIterable
val raw     : Future[java.util.Collection[java.util.List[AnyRef]]] = m(Person.name.age).getAsyncRaw
```

## Optimized Time getters

The default time getters return `List`s of tuples as the normal `get` method does:

```
getAsOf(…)    getAsyncAsOf(…)
getSince(…)   getAsyncSince(…)
getWith(…)    getAsyncWith(…)
```
Again, by importing the specialized api described above, the time getters can return tuples in alternative collection types:

```
getArrayAsOf(…)       getAsyncArrayAsOf(…)
getArraySince(…)      getAsyncArraySince(…)
getArrayWith(…)       getAsyncArrayWith(…)

getIterableAsOf(…)    getAsyncIterableAsOf(…)
getIterableSince(…)   getAsyncIterableSince(…)
getIterableWith(…)    getAsyncIterableWith(…)

getRawAsOf(…)         getAsyncRawAsOf(…)
getRawSince(…)        getAsyncRawSince(…)
getRawWith(…)         getAsyncRawWith(…)
```

`getHistory` is only implemented to return a List (the default) since the result order is not guaranteed and we therefore always need a fully realized sortable collection.