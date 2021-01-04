---
title: "Compare"
weight: 10
menu:
  main:
    parent: compare
---

# Molecule vs other query languages

Every database/abstraction layer has its strengths and weaknesses and we'll try here to compare existing systems with Molecule with some examples of how they accomplish similar tasks.

Many systems lets you define data objects matching your domain that are then _consumed_ by the host language (Scala) in combination with DSLs. 


### Slick

If we take [Slick](http://slick.typesafe.com/doc/3.0.0-M1/queries.html#joining-and-zipping) for instance we could say that it "_consumes_" the data objects `coffees` and `suppliers` in this Scala sequence comprehension:

<pre class="clean">
// Slick
val implCrossJoin = for {
  c <- coffees
  s <- suppliers
} yield (c.name, s.name)
</pre>
Whereas in Molecule, we only _declare_ which attributes we are interested in. Molecule attributes _themselves_ form the query - they are not _consumed_ by an outer construct. That way, the domain terms directly form the query without additional keywords and constructs. Only the final `get` is a query keyword:

```
// Molecule
val coffeeSupliers = Coffees.name.Suppliers.name.get
```
We get the exact same type-inferred result back, a `List[(String, String)]`


### Squeryll

[Squeryl](https://www.squeryl.org/) also _consumes_ data objects, now in a DSL construct:

<pre class="clean">
// Squeryl
def songs = from(MusicDb.songs)(s => where(s.artistId === id) select(s))
</pre>
In Molecule we don't need to use keywords like `from`, `where` and `select` (apart from the final `get` method). We instead get song entities (`e`) filtered by Artist by simply applying a required value directly to the `artist` attribute:

```
// Molecule
val songs = Song.e.artist(id).get
```

### Next

Let's compare with [Datomic / Datalog](/compare/datomic)...
