---
date: 2015-01-02T22:06:44+01:00
title: "Get"
weight: 30
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/insert
next: /docs/crud/getJson
down: /docs/transactions
---

# Get (read) Data

We get/read data from the database by calling `get` on a molecule. This returns an `Iterable` of tuples that match 
the molecule attributes (except for arity-1):
 

```scala
val persons1attr: Iterable[String] = Person.name.get

val persons2attrs: Iterable[(String, Int)] = Person.name.age.get

val persons3attrs: Iterable[(String, Int, String)] = Person.name.age.likes.get

// Etc.. to arity 22
```

### With entity id

Attributes of some entity are easily fetched by applying an entity id to the first namespace in the molecule 
 
```scala
Person(fredId).name.age.likes.get.head === List("Fred", 38, "pizza") // (Iterable implicitly converted to List
```
The entity id is used for the first attribute of the molecule, here `name` having entity id `fredId`. 

`Person` is just the namespace for the following attributes, so that we get `:person/name`, `:person/age`, `:person/likes` etc..

### Big molecules

The more attributes a molecule has, the longer it takes to compile. Once we get over 14-16 attributes we might start seeing compilation 
slowing down depending on our hardware. There's an easy trick to split up large molecules into
[composite](/docs/relationships/composites/) molecules consisting of smaller sub-molecules:
 
```scala
// Tough on the compiler
m(Ns(id).a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22).get

// faster
m(Ns(id).a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11 ~ Ns.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22).get

// Fastest
m(Ns(id).a1.a2.a3.a4.a5.a6.a7 ~ Ns.a8.a9.a10.a11.a12.a13.a14 ~ Ns.a15.a16.a17.a18.a19.a20.a21.a22).get
```

With this technique we can even build molecules bigger than arity-22
 
```scala
m(Ns(id).a1.a2.a3.a4.a5.a6.a7 
    ~ Ns.a8.a9.a10.a11.a12.a13.a14 
    ~ Ns.a15.a16.a17.a18.a19.a20.a21.a22
    ~ Ns.a23.a24.a25.a26.a27.a28.a29.a30
).get
// Etc...
```


## 2-steps with Entity API

Molecules can get optional attributes which make them flexible to get irregular data sets. We could for instance fetch some
Persons where some of them has no `likes` asserted:

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

This way we can for instance in the first step get the mandatory data (`name` and `age`) with a molecule and then in a second step
ask for optional data (`likes`) for each entity:  

```scala
// Step 1
val seed: Iterable[(Long, String, Int)] = Person.e.name.age.get

// Step 2
val data: Iterable[(String, Int, Option[String])] = seed.map { case (e, name, age) =>
  // Add optional `likes` value via entity api
  (name, age, e[String](":person/likes"))
}
```
For this simple example, the original molecule with an optional `likes` attribute would of course have been sufficient and 
more concise. But for more complex interconnected data this approach can be a good extra tool in the toolbox.


## Raw untyped data

If you need big data sets returned for batch processing for instance, then you might not need to cast every row of data. In that case you
can get raw data from a Datomic query by calling `getRaw` on a molecule:


```scala
val rawData: Iterable[java.util.List[Object]] = Person.name.age.likes.getRaw
         //    rows         row      attrs
```



### Next

[Get Json...](/docs/crud/getjson)