---
title: "Index"
weight: 20
menu:
  main:
    parent: generic
    identifier: indexes
up:   /manual/generic
prev: /manual/generic/datom
next: /manual/generic/log
down: /manual/debug
---

# Index APIs

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Index.scala)


_Some index descriptions in the following sections respectfully quoted from the 
[Datomic documentation](https://docs.datomic.com/on-prem/indexes.html)._

Datomic maintains four indexes that contain ordered sets of datoms. Each of these indexes is named based on 
the sort order used.  E, A, and V are always sorted in ascending order, while T is always in descending order:

 - **EAVT** - Datoms sorted by Entity-Attribute-Value-Transaction.
 - **AVET** - Datoms sorted by Attribute-Value-Entity-Transaction.
 - **AEVT** - Datoms sorted by Attribute-Entity-Value-Transaction.
 - **VAET** - "Reverse index" for reverse lookup of ref types.

Molecule provides access to each index by instantiating a corresponding Index object with one or more
arguments and then adding generic attributes matching the data to be returned.

Contrary to Datomic's datoms API that returns Datoms, Molecule returns tuples of data matching the
generic attributes added to the Index object. This way, Index molecules transparently share the same
return type semantics as normal molecules.

### Generic Index attributes

The following standard generic Index attributes can be used to build Index molecules:

 - `e` - Entity id (`Long`)
 - `a` - Attribute (`String`)
 - `v` - Value (`Any`)
 - `t` - Transaction point in time (`Long` alternatively `Int`)
 - `tx` - Transaction entity id (`Long`)
 - `txInstant` - Transaction wall-clock time (`java.util.Date`)
 - `op` - Operation: assertion / retraction (`Boolean` true/false)



## EAVT

The EAVT index provides efficient access to everything about a given entity.
Conceptually this is very similar to row access style in a SQL database,
except that entities can possess arbitrary attributes rather then being limited
to a predefined set of columns.

```scala
// Create EAVT Index molecule with 1 entity id argument
EAVT(e1).e.a.v.t.get === List(
  (e1, ":person/name", "Ben", t1),
  (e1, ":person/age", 42, t2),
  (e1, ":golf/score", 5.7, t2)
)

// Maybe we are only interested in the attribute/value pairs:
EAVT(e1).a.v.get === List(
  (":person/name", "Ben"),
  (":person/age", 42),
  (":golf/score", 5.7)
)

// Two arguments to narrow the search
EAVT(e1, ":person/age").a.v.get === List(
  (":person/age", 42)
)
``` 


## AVET

The AVET index provides efficient access to particular combinations of attribute and value.

```scala
// Create AVET Index molecule with 1 entity id argument
AVET(":person/age").v.e.t.get === List(
  (42, e1, t2),
  (37, e2, t5),
  (14, e3, t7)
)

// Narrow search with multiple arguments
AVET(":person/age", 42).e.t.get === List( (e1, t2) )
AVET(":person/age", 42, e1).e.v.get === List( (e1, t2) )
AVET(":person/age", 42, e1, t2).e.v.get === List( (e1, t2) )
```

The AVET Index can be filtered by a range of values between `from` (inclusive) and
`until` (exclusive) for an attribute:

```scala
AVET.range(":person/age", Some(14), Some(37)).v.e.t.get === List(
  (14, e4, t7) // 14 is included in value range
               // 37 not included in value range
               // 42 outside value range
)

// If `from` is None, the range starts from the beginning
AVET.range(":person/age", None, Some(40)).v.e.t.get === List(
  (14, e3, t7),
  (37, e2, t5)
)

// If `until` is None, the range goes to the end
AVET.range(":person/age", Some(20), None).v.e.t.get === List(
  (37, e2, t5),
  (42, e1, t2)
)
```

## AEVT

The AEVT index provides efficient access to all values for a given attribute,
comparable to traditional column access style.

```scala
// Create AEVT Index molecule with 1 entity id argument
AEVT(":person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)

// Narrow search with multiple arguments
AEVT(":person/name", e1).e.v.get === List( (e1, "Ben") )
AEVT(":person/name", e1, "Ben").e.v.get === List( (e1, "Ben") )
AEVT(":person/name", e1, "Ben", t2).e.v.get === List( (e1, "Ben") )
```

## VAET

The VAET index contains all and only datoms whose attribute has a :db/valueType of :db.type/ref.
This is also known as the reverse index, since it allows efficient navigation of relationships in reverse.

```scala
// Say we have 3 entities pointing to one entity:
Release.e.name.Artists.e.name.get === List(
  (r1, "Abbey Road", a1, "The Beatles"),
  (r2, "Magical Mystery Tour", a1, "The Beatles"),
  (r3, "Let it be", a1, "The Beatles")
)

// .. then we can get the reverse relationships with the VAET Index:
VAET(a1).v.a.e.get === List(
  (a1, ":release/artists", r1),
  (a1, ":release/artists", r2),
  (a1, ":release/artists", r3)
)

// Narrow search with multiple arguments
VAET(a1, ":release/artist").e.get === List(r1, r2, r3)
VAET(a1, ":release/artist", r2).e.get === List(r2)
VAET(a1, ":release/artist", r2, t7).e.get === List(r2)
```



### Next

[Log...](/manual/generic/log)