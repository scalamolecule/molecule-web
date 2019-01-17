---
title: "Introduction"
weight: 11
menu:
  main:
    parent: quick-start
    
up:   /manual/
prev: /manual/
next: /manual/setup
down: /manual/setup
---

# Molecule introduction

Molecule let's you model and query your domain data structures directly with the words of your domain.

bldskjfl ksdfjl sadkf lj

### Query

Let's say we want to find Persons in the Datomic database. Then we can build a molecule to get this data
for us:

```scala
val persons: List[(String, Int)] = m(Person.name.age).get
```
This fetches a List of tuples of Strings/Int's that are the types of the `name` and `age` Attributes that 
we asked for. We can continue adding more and more Attributes as with the builder pattern to define what data
we are interested in.

There are 5 different getters that return data in various formats and all synchronous getters have an
asynchronous equivalent getter. See [Building molecules](/manual/attributes/basics) for more info on
getters.


### Building blocks

Attributes are atomic pieces of information that are prefixed by a Namespace, in this case `Person`. Namespaces
are not like SQL tables but just a common meaningful prefix to Attributes that have something in common. 

The `m` method (for **_m_**olecule) is an implicit macro method in the Molecule library that consumes the
data structure that we have modelled with our custom DSL and produces a molecule. We can then for instance 
call `get` on the molecule to fetch data from Datomic that matches the data structure of the molecule. 
Since the `m` method is implicit we can simply write
```scala
val persons = Person.name.age.get
```
That way, we can use our domain terms directly in our code in a type-safe and semantically meaningful way to
 communicate with our Datomic database. Since we are working with auto-generated boilerplate code for
 our domain terms, our IDE can help us infer the type of each of our molecules and prevents us from making 
 any invalid queries.


### Operations on molecules

For single entities we can apply data to the attributes of a molecule and then save it:

```scala
// Save Lisa entity and retrieve new entity id
val lisaId = Person.name("Lisa").age("27").save.eid
```
Or we can add data for multiple entities with an `insert`:

```scala
// Save Lisa entity and retrieve new entity id
Person.name.age insert List(
  ("Lisa", 27), // Inserting Lisa entity
  ("John", 32)  // Inserting John entity  
)
```
Using an entity id we can update attribute values of an entity:
```scala
// Update age attribute value of Lisa entity
Person(lisaId).age("28").update
```
In Datomic, an update is actually a retraction of the old data and an assertion of the new data. In this example, 
Lisa's age 27 is retracted and her new age 28 asserted. With this information model, Datomic allow us to go
back in time and see when Lisa's age was changed to 28.

Or we can retract an entity entirely by calling `retract` on an entity id
```scala
// Retract ("delete") Lisa entity
lisaId.retract
```
Since the entity is retracted and not deleted, Datomic allow us to go back in time before the retraction
to see that Lisa existed. 


### Expressive powers

We can apply conditional values, ranges etc to our molecules to express more subtle data structures:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```
which will find "names of twitter/facebook_page communities in neighborhoods of southern districts".



## Datomic and Molecule

Molecule is a domain-tailored abstraction layer on top of the [Datomic database](https://www.datomic.com). It can therefore be good to know 
a little about Datomic in order to understand Molecule.


### Facts/Datoms with time built in

Instead of mapping objects to tables or documents, the core unit of data in Datomic is an atomic piece of 
information: a _Datom_. A Datom describes a _fact_, for instance that "John likes pizza". A timestamp
adds information about _when_ John stated that he likes pizza. A fifth piece of information "added" tells
if the fact is asserted (true - John likes) or retracted (false - John no longer likes). So, a Datom
consists of 5 pieces of information:
```
 John     likes    pizza      12:35:54       true
   |        |        |           |            |
Entity  Attribute  Value  Transaction/time  Added
```
With Molecule we could model asserting the fact like this:

```scala
val txTime = Person(johnId).likes("pizza").update.txInstant
```
_likes_ is an `attribute` with `value` _pizza_. It is **asserted** that the `entity` _johnId_ likes pizza 
at `transaction` time 12:35:54. A timestamp is automatically set with all transactions. But if we need 
"domain time" we could add such attribute to the transaction as well, since this is simply a saved data structure 
in Datomic as our domain data (more on ["transaction meta data"](/manual/transactions/tx-meta-data))

As you saw, Molecule simply models Datomic datoms by chaining together _attributes_ to form "_molecules_" in unlimited 
combinations suiting your domain. You can then call different operations on a molecule as we saw above in order to 
interact with the underlying Datomic database.

### Immutable data

Everytime a fact is asserted the old value of the attribute is _not deleted_. Data is only appended to a Datomic database, and
an update of an attribute value internally creates a retraction of the old value and an assertion of the new value.
In this way, we can [go back in time](/manual/time/asof-since) and see the values of an attribute _at any point in time_. We could for instance see all 
our previous addresses if this was part of our domain model.

Also when we delete data, it's actually not deleted, but "retracted". Retracted data doesn't show up when we are 
querying the current database. But if we look at the database at an earlier point in time we can see the data 
before it got retracted.

### Namespaces and attributes

In Molecule, `attributes` are organized in `namespaces` to group related qualities of our domain:
 
![](/img/DatomicElements1.png)

### Entity != row in an sql Table

An `entity` can have _any_ `attribute` from _any_ `namespace` associated to it:

![](/img/DatomicElements2.png)

An entity is therefore not like a row in a table but rather a "cross-cutting" thing that we can freely associate 
any attribute value to. Note how "attrB1" in this example is not associated to entity1.



### Next...

[Setup molecule](/manual/setup/)
