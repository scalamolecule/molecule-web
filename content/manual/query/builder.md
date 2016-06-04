---
date: 2015-01-02T22:06:44+01:00
title: "Builder"
weight: 10
menu:
  main:
    parent: query
---

# Attribute builder pattern

(See [attribute tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/Attribute.scala))

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data
 structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```scala
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces
 are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.


## 3 ways of getting data

#### 1. Mandatory Attributes

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them. 

_Note that this is different from selecting rows from a sql table where you can also get null values back!_ 

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated 
with them then this query will _not_ return those entities:

```scala
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


#### 2. Tacet Attributes with `_` suffix

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values. 
We call the un-returning attributes "tacet attributes" (music term for "silent"). 

If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age 
 values, then we can add an underscore `_` after the `age` Attribute:

```scala
val names = Person.name.age_.get
```
This will return names of person entities having both a name and age Attribute set. Note how the age values are no 
longer returned from the type signatures:

```scala
val persons: List[(String, Int)] = Person.name.age.get
val names  : List[String]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures 
we look for.


#### 3. Optional Attributes with `$` suffix (like Null values)

If an attribute value is only sometimes set, we can ask for it's optional value by adding a dollar sign `$` after the attribute:

```scala
val names: List[(String, Option[String], String)] = Person.firstName.middleName$.lastName.get
```
That way we can get all person names with or without middleNames. As you can see from the return type, the middle 
name is wrapped in an `Option`.


### Tuples returned

Molecule returns all result sets as tuples of values (with `get`).

```scala
val persons: List[(String, Int)] = Person.name.age.get
```

## Molecule max size
The size of molecules are limited to Scala's arity limit of 22 for tuples. If you need to return more you can 
use an additional molecule that takes an entity id and then retrieve further attribute values for that entity.

If we need to insert/return more than 22 attribute values we can easily do this by using the entity id to 
work with further attributes/values:

```scala
// Insert maximum of 22 facts and return the created entity id
val eid = Ns.someId.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.insert(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
).eid

// Use entity id to continue adding more values for the same entity
Ns.x.y.z.insert(eid, 23, 24, 25)
```

Likewise we can retrieve more than 22 values in 2 steps

```scala
val first22values = Ns.someId_(1).b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.get

// Use entity id to continue adding more values
val next3values = Ns.x.y.z.insert(eid, 23, 24, 25)
```
