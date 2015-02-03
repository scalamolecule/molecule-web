---
date: 2015-01-02T22:06:44+01:00
title: "xxBasics"
weight: 30
menu:
  main:
    parent: manual
    identifier: xx
---

# Basics

Molecule is a domain-tailored abstraction layer on top of the Datomic database. It can therefore be good to first know a little about Datomic in order to understand Molecule.


### Facts/Datoms

The core unit of data in Datomic is a fact or a "_datom_" that consists of 4 elements:

```
 John     likes    pizza      12:35:54
   |        |        |           |
Entity  Attribute  Value  Transaction/time
```
The fact that John likes pizza at a certain time is modelled as a datom in Datomic.

_likes_ is an `attribute` with `value` _pizza_. It is **asserted** that the `entity` _johnId_ likes pizza at `transaction` time 12:35:54 (a timestamp is automatically set with all transactions). 

### Datomic Attributes

Attributes are often (but not necessarily!) prefixed with a Namespace in Datomic. The `likes` Attribute could for instance have been prefixed with a `Person` Namespace so that we can refer to it as `:person/likes`. 

Namespaces are simply used to prefix Attributes with a meaningful common characteristic of your domain which can also help prevent Attribute name clashes.

### Molecule Attributes

With Molecule we model Namespaces with uppercase words and Attributes with lower-case words. We can model the above fact like this:

```scala
Person.name("John").likes("pizza")
```
This is like a "molecular data structure" of Attributes with some values that we are interested in. In this case Entities with two Attributes having certain values. We can imagine that we "project" a data template onto the graph of data that we operate on to see if there's some "matches".

### Entities

An Entity is basically an id number that is created when we assert one or more facts in a transaction. An Entity is comprised by the Attributes associated with it. Or we could say that an Entity is a bunch of associations to Attribute values - or _Datoms_ in the words of Datomic.

Those Attributes could come from multiple Namespaces which make Entities free and not bound to any Namespace.

### Inserting data

We can create a new Entity with two Attributes and return the created id:

```scala
val johnId = Person.name("John").likes("pizza").insert.id
```

There's now an entity with two Attributes associated with it and together we can think of this entity as representing John. A transaction time is automatically set with each transaction by Datomic.

### Querying data





### Manipulating data

If John at a later point likes something else than pizza we can assert a new fact that will then get a later timestamp of this transaction:

```scala
Person(johnId).likes("pizza").update
```


. You can see it as a "template" for d





As you see we can apply values to an Attribute to insert and update 



Attributes are composed to form "molecules" that describe unique and flexible data structures in unique combinations suiting your domain. Those are then translated to Datalog queries being executed against the underlying Datomic database.

An attribute in Datomic is part of _fact_ or _Datom_ consisting of four elements:



_likes_ is an `attribute` with `value` _pizza_. It is **asserted** that the `entity` _johnId_ likes) pizza at `transaction` time 12:35:54. A timestamp is automatically set with all transactions. But if we need "domain time" we could add such attribute to the transaction as well, since this is simply a saved data structure in Datomic as our domain data.

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
 
 
[setup]: https://github.com/scalamolecule/wiki/Setup-a-Datomic-database
[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial





From a Schema that we define, Molecule will create the necessary boilerplate code so that we can build unique strings of "molecular data structures" like for instance names and ages of Persons:

```scala
val persons = Person.name.age.get
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name` and `age`. Attributes are not dependent of or a sub-unit of Namespaces like Columns are to Tables in the SQL world. Namespaces are rather a way of organizing Attributes to avoid name clashes.


. We could for instance build a 


### Building molecules

A molecule starts with a Namespace and builds on with attributes and/or other Namespaces/Attributes to form a desired data structure to work with. We could for instance have some attributes that relates to how we model a Person and organize those attributes in a `Person` namespace:

```scala
trait Person {
  val name = oneString
  val age  = oneInt
}
```

We can then use the Attributes `name` and `age` to build a molecule that will query for names and ages of person in the database:


```scala
val persons = Person.name.age.get
```




a `Person` namespace



Attributes are organized in Namespaces that are more organizational than operational as tables are in the SQL world. Namespaces are more a way of organizing attributes that have something in common according to your domain.





### Safe

Our query asks for entities having values defined for all three attributes. If some entity doesn't have the `street` attribute set it won't be returned. So we can safely assume that our result set contains no null values and we therefore return the raw values (without using Optional for instance).