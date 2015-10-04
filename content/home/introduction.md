---
date: 2015-01-02T22:06:44+01:00
title: "Introduction"
weight: 20
menu:
  main:
    parent: home
---

# Molecule introduction

Molecule let's you model and query your domain data structures directly with the words of your domain.

### Query

Describing our domain with Attributes grouped in Namespaces allow us to for instance find Persons in our Datomic database by composing molecules attribute by attribute with the builder pattern:

```scala
val personMolecule = m(Person.name.age)
```

An implicit macro method `get` (or `hl` for HLists) will at compile time translate our molecule to a Datalog query. And at runtime it will get executed against Datomic to retrieve a type-inferred result set of tuples/HLists:

```scala
val persons: Seq[(String, Int)]         = Person.name.age.get
val persons: Seq[String :: Int :: HNil] = Person.name.age.hl
```

The attributes of our domain directly form our molecules. Furthermore the compiler prevents us from making any invalid queries since all molecules are type-infered already in our IDE.


### Insert and update

We also insert and update data with molecules:

```scala
// Insert and retrieve new entity id
val lisaId = Person.name("Lisa").age("27").insert.eid

// Update
Person(lisaId).age("28").update
```

### Expressive powers

We can apply conditional values, ranges etc to our molecules to express more subtle data structures:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```
which will find "names of twitter/facebook_page communities in neighborhoods of southern districts".



## Datomic and Molecule

Molecule is a domain-tailored abstraction layer on top of the Datomic database. It can therefore be good to know a little about Datomic in order to understand Molecule.


### Facts/Datoms with time built in

Instead of mapping objects to tables or documents, the core unit of data in Molecule is an atomic piece of information: an _attribute_. 

Attributes are composed to form "molecules" that describe unique and flexible data structures in endless combinations suiting your domain. Those are then translated to Datalog queries being executed against the underlying Datomic database.

An attribute in Datomic is part of a _fact_ or _Datom_ consisting of four elements:

```
 John     likes    pizza      12:35:54
   |        |        |           |
Entity  Attribute  Value  Transaction/time
```

With Molecule we could model the fact like this:

```scala
Person(johnId).likes("pizza").update
```
_likes_ is an `attribute` with `value` _pizza_. It is **asserted** that the `entity` _johnId_ likes pizza at `transaction` time 12:35:54. A timestamp is automatically set with all transactions. But if we need "domain time" we could add such attribute to the transaction as well, since this is simply a saved data structure in Datomic as our domain data.

### Immutable data

Everytime a fact is asserted the old value of the attribute is _not deleted_. A Datomic database is immutable. We can go back in time and see the values of an attribute _at any point in time_. We could for instance see all our previous addresses if this was part of our domain model.

Also when we delete data, it's actually not deleted, but "retracted". Retracted data doesn't show up when we are querying the current database. But if we look at the database at an earlier point in time we can see the data before it got retracted.

### Namespaces and attributes

`attributes` are organized in `namespaces` to group related qualities of our domain:
 
![](/img/DatomicElements1.png)

### Entity != row in an sql Table

An `entity` can have _any_ `attribute` from _any_ `namespace` associated to it:

![](/img/DatomicElements2.png)

An entity is therefore not like a row in a table but rather a "cross-cutting" thing that we can freely associate any attribute value to. Note how "attrB1" in this example is not associated to entity1.



### Further reading...

Go straight to the [Molecule Seattle tutorial][tutorial] to see a wide range of
 queries that Molecule can express, or check out first how we use Molecule to 
 [setup the database][setup] and [populated it with data][populate].
 
 
[setup]: /manual/setup
[populate]: /manual/insert
[tutorial]: /tutorials/seattle