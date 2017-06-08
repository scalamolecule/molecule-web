---
date: 2015-01-02T22:06:44+01:00
title: "Basics"
weight: 10
menu:
  main:
    parent: attributes
up: /docs/attributes
prev: /docs/attributes
next: /docs/attributes/modes
down: /docs/entities
---

# Attribute basics

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala)

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data
 structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```scala
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces
 are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.



### Tuples returned

Molecule returns all result sets as tuples of values (with `get`).

```scala
val persons: Iterable[(String, Int)] = Person.name.age.get
```

### Molecule max size

The size of molecules are limited to Scala's arity limit of 22 for tuples.
 
If we need to insert more than 22 attribute values we can easily do this by using the entity id to 
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

## Cardinality

The attributes `name`, `age` and `gender` that we saw above are typical cardinality-one attributes each with one value.

Datomic also has cardinality-many attributes that have a `Set` of values. This means that the same value cannot be saved 
multiple times, or that only unique values are saved. An example could be a cardinality-many attribute `hobbies` of a `Person`:

```scala
Person.name.hobbies.get.head === ("Fred", Set("Trains", "Chess"))
```

In the [Update](/docs/crud/update/) section of CRUD we will see how multiple values are managed with Molecule.  


### Next

[Mandatory/Tacet/Optional attributes...](/docs/attributes/modes)