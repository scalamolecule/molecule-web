---
date: 2014-05-14T02:13:50Z
title: "Compare"
weight: 0
menu:
  main:
    parent: compare
---

# Other databases vs. Molecule

Every database/abstraction layer has its strengths and weaknesses and we'll try here to compare existing systems with Molecule with some examples of how they accomplish similar tasks.

### Others _consume_, Molecule _declares_ data structures

Many systems lets you define data objects matching your domain that are then _consumed_ by the host language (Scala) in combination with DSLs. 

If we take [Slick](http://slick.typesafe.com/doc/3.0.0-M1/queries.html#joining-and-zipping) for instance we could say that it "_consumes_" the data objects `coffees` and `suppliers` in this Scala sequence comprehension:

```scala
// Slick
val implCrossJoin = for {
  c <- coffees
  s <- suppliers
} yield (c.name, s.name)
```
Whereas in Molecule, we only _declare_ which attributes we are interested in. Molecule attributes _themselves_ form the query - they are not _consumed_ by an outer construct. That way, the domain terms directly form the query without additional keywords and constructs:

```scala
// Molecule
val coffeeSupliers = Coffees.name.Suppliers.name.get
```
We get the exact same type-inferred result back, a `Seq[(String, String)]`

[Squeryl](http://squeryl.org/selects.html) also _consumes_ data objects, now in a DSL construct:

```scala
// Squeryl
def songs = from(MusicDb.songs)(s => where(s.artistId === id) select(s))
```
In Molecule we don't need to use keywords like `from`, `where` and `select` (apart from the final `get` method). We instead get song entities (`e`) filtered by Artist by simply applying a required value directly to the `artist` attribute:

```scala
// Molecule
val songs = Song.e.artist(id).get
```

### More databases to compare...

Over time we'll compare Molecule with as many database systems as possible. Those not linked yet below are on our wish list:

- [Datomic](/compare/datomic)
- [SQL](/compare/sql)
  - [Slick](/compare/sql/slick)
  - Squeryl
  - Sqltyped tutorial
- Mongodb
  - Native
  - Rogue
- Titan
- Neo4j
- Gremlin
- More...?

If you want to translate one into Molecule, please feel free to submit a pull request or bring up ideas. 