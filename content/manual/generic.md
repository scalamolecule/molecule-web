---
title: "Generic"
weight: 100
menu:
  main:
    parent: manual
    identifier: generic
up:   /manual/time
prev: /manual/time/testing
next: /manual/generic/datom
down: /manual/debug
---

# Generic attributes

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic)

Molecule provides access to meta data about data and Schema with the following 7 generic interfaces (each with a little example - 
read more by clicking on each title):


### [Datom](datom)

Get id of Ben entity with generic Datom attribute `e`
```scala
Person.e.name.get.head === (benEntityId, "Ben")
```

### [EAVT Index](indexes)

Attributes and values of entity e1
```scala
EAVT(e1).a.v.get === List(
  (":person/name", "Ben"),
  (":person/age", 42),
  (":golf/score", 5.7)
)
``` 

### [AVET Index](indexes)

Values, entities and transactions where attribute :person/age is involved
```scala
AVET(":person/age").e.v.t.get === List(
  (42, e1, t2),
  (37, e2, t5)
  (14, e3, t7),
)

// AVET index filtered with an attribute name and a range of values
AVET.range(":person/age", Some(14), Some(40)).v.e.t.get === List(
  (14, e4, t7),
  (37, e2, t5)
)
``` 

### [AEVT Index](indexes)

Entities, values and transactions where attribute :person/name is involved 
```scala
AEVT(":person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)
``` 

### [VAET Index](indexes)

Get entities pointing to entity a1
```scala
VAET(a1).v.a.e.get === List(
  (a1, ":release/artists", r1),
  (a1, ":release/artists", r2),
  (a1, ":release/artists", r3),
)
``` 

### [Log](log)

Data from transaction t1 until t4 (exclusive)
```scala
Log(Some(t1), Some(t4)).t.e.a.v.op.get === List(
  (t1, e1, ":person/name", "Ben", true),
  (t1, e1, ":person/age", 41, true),

  (t2, e2, ":person/name", "Liz", true),
  (t2, e2, ":person/age", 37, true),

  (t3, e1, ":person/age", 41, false),
  (t3, e1, ":person/age", 42, true)
)
``` 

### [Schema](schema)

Get entities pointing to entity a1 
```scala
Schema.part.ns.attr.fuolltext$.doc.get === List(
  ("ind", "person", "name", Some(true), "Person name"), // fulltext search enabled
  ("ind", "person", "age", None, "Person age"),
  ("cat", "sport", "name", None, "Sport category name")
)
``` 


### Next

[Datom...](/manual/generic/datom)