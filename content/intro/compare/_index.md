---
title: "Compare"
weight: 30
menu:
  main:
    parent: intro
    identifier: intro-compare
---

# Molecule vs other query languages

Every database language has its strengths and weaknesses and we'll try here to compare existing languages with Molecule and see how they accomplish similar tasks.

Many query languages let you define data objects matching your domain that are then _consumed_ by the host language (Scala) in combination with DSL tokens. Molecule instead let you use your domain terms directly as the query tokens.


### Slick

If we take [Slick](http://slick.typesafe.com/doc/3.0.0-M1/queries.html#joining-and-zipping) for instance we could say that it "_consumes_" the domain terms `coffees` and `suppliers` in this Scala sequence comprehension:

```
// Slick
val coffeeSupliers = for {
  c <- coffees
  s <- suppliers
} yield (c.name, s.name)
```
Whereas in Molecule, we only _declare_ which attributes we are interested in. Molecule attributes _themselves_ form the query - they are not _consumed_ by an outer construct. That way, the domain terms directly form the query without additional keywords and constructs. Only the final `get` is a query keyword:

```scala
// Molecule
val coffeeSupliers = Coffees.name.Suppliers.name.get
```
We get the exact same type-inferred result back, a `Future[List[(String, String)]]`


### Squeryl

[Squeryl](https://www.squeryl.org/) also _consumes_ data objects, now in another DSL construct:

```
// Squeryl
def songs = from(MusicDb.songs)(s => where(s.artistId === id) select(s))
```
In Molecule we don't need to use keywords like `from`, `where` and `select` (apart from the final `get` method). We instead get song entities (`e`) filtered by Artist by simply applying a required value directly to the `artist` attribute:

```scala
// Molecule
val songs = Song.e.artist_(id).get
```
(The underscore after artist makes the attribute tacit - that we don't need to return its value)

### Next

Let's compare with [Datomic / Datalog](/intro/compare/datomic/)...
