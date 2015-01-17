---
date: 2014-05-14T02:13:50Z
title: "Compare"
weight: 0
menu:
  main:
    parent: compare
---

# Molecule and others...

Molecule takes data modelling and querying one step further towards simplicity than Todays type-safe object/functional-relational mappers. 

### Some _consume_ while Molecule "_is_"



[Slick](http://slick.typesafe.com/doc/3.0.0-M1/queries.html#joining-and-zipping) for instance "_consume_" data objects in a Scala sequence comprehension:

```scala
// Slick
val implCrossJoin = for {
  c <- coffees
  s <- suppliers
} yield (c.name, s.name)
```
Whereas in Molecule, _the data objects themselves form the query_:

```scala
// Molecule
val coffeeSupliers = Coffees.name.Suppliers.name.get
```
Even though the Slick approach is no doubt very intuitive and smart, Molecule instead applies a meta-programming approach trying to push the limit for how little code we can possibly write as end users given the knowledge we have of the code and schemas we write.

While there's nothing wrong in "consuming" objects in itself, it is still a tiny layer of abstraction squeezed in between the programmer and his/her mental model of a domain. As an example (I hope) we don't think in terms of sequence comprehensions, maps and flatMaps when we think of things in our domain. If we can "stay close" to "things", "Person names" etc when writing queries the closer we stay to our mental model of our domain which is a main goal of Molecule.

[Squeryl](http://squeryl.org/selects.html) _consume_ data objects also in a DSL construct:

```scala
// Squeryl
def songs = from(MusicDb.songs)(s => where(s.artistId === id) select(s))
```
In Molecule we would get song entities (`e`) filtered by Artist by applying a required value directly to an attribute like this:

```scala
// Molecule
val songs = Song.e.artist(id).get
```

### Compare with your current database!

Over time we'll compare Molecule with as many database systems as our time permits to explore. If you know some of them well and want to translate it to Molecule, please feel free to submit a pull request or bring up ideas...

- [Datomic](/compare/datomic)
- SQL
  - Slick
  - Squeryl
  - Sqltyped tutorial
- Mongodb
  - Native
  - Rogue
- Titan
- Neo4j
- Gremlin
- More...?