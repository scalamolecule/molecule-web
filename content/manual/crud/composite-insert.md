---
date: 2015-01-02T22:06:44+01:00
title: "Composite insert"
weight: 30
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud/insert
next: /manual/crud/get
down: /manual/transactions
---

# Composite insert

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Composite.scala)


[Composite](/manual/relationships/composites/) data sets can be inserted with a special `insert` method
that takes 3 parameter groups:

```scala
insert(<sub-molecules>)(<data>)(<optional-tx-meta-data>)
```

We could for instance insert 2 Article entities each with 2 tags:

```scala
insert(
  // 2 sub-molecules
  Article.name.author, Tag.name.weight
)(
  // 2 rows of data (Articles) 
  // The 2 sub-tuples of each row matches the 2 sub-molecules
  List(
    (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
    (("Best jokes ever", "John Cleese"), ("fun", 3))
  )
)() // empty optional tx meta data parameter group
```

### Avoid mega molecules

Long molecules can take a long time to compile. So, instead of inserting wide data sets with a mega molecule:
```scala
// One mega molecule
Ns.bool.bools.date.dates.double.doubles.enum.enums
  .float.floats.int.ints.long.longs.ref1
  .refSub1.str.strs.uri.uris.uuid.uuids
  .insert(
    // 2 rows with each one mega tuple of data
    Seq(
      (
        true, Set(true), date1, Set(date2, date3), 1.0, Set(2.0, 3.0), "enum1", Set("enum2", "enum3"),
        1f, Set(2f, 3f), 1, Set(2, 3), 1L, Set(2L, 3L), 11L,
        12L, "a", Set("b", "c"), uri1, Set(uri2, uri3), uuid1, Set(uuid2)
      ),
      (
        false, Set(false), date4, Set(date5, date6), 4.0, Set(5.0, 6.0), "enum4", Set("enum5", "enum6"),
        4f, Set(5f, 6f), 4, Set(5, 6), 4L, Set(5L, 6L), 21L,
        22L, "d", Set("e", "f"), uri4, Set(uri5, uri6), uuid4, Set(uuid5)
      )
    )
  )
```

.. then instead split up the molecule and data into sub-molecules/tuples with a composite insert:

```scala
insert(
  // 3 sub-molecules
  Ns.bool.bools.date.dates.double.doubles.enum.enums,
  Ns.float.floats.int.ints.long.longs.ref1,
  Ns.refSub1.str.strs.uri.uris.uuid.uuids
)(
  // Two rows with tuples of 3 sub-tuples that type-safely match the 3 molecules above
  Seq(
    (
      (true, Set(true), date1, Set(date2, date3), 1.0, Set(2.0, 3.0), "enum1", Set("enum2", "enum3")),
      (1f, Set(2f, 3f), 1, Set(2, 3), 1L, Set(2L, 3L), 11L),
      (12L, "a", Set("b", "c"), uri1, Set(uri2, uri3), uuid1, Set(uuid2))
    ),
    (
      (false, Set(false), date4, Set(date5, date6), 4.0, Set(5.0, 6.0), "enum4", Set("enum5", "enum6")),
      (4f, Set(5f, 6f), 4, Set(5, 6), 4L, Set(5L, 6L), 21L),
      (22L, "d", Set("e", "f"), uri4, Set(uri5, uri6), uuid4, Set(uuid5))
    )
  )
)()
```
The result is identical but allows for much faster compiles.


### Transaction meta data

If we have custom transaction meta data that we want to insert with the insert transaction we could
for instance record who put in the information and what use case was involved to do so:

```scala
insert(
  // 2 sub-molecules
  Article.name.author, Tag.name.weight
)(
  // 2 rows of data (Articles)
  // The 2 sub-tuples of each row matches the 2 sub-molecules
  List(
    (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
    (("Best jokes ever", "John Cleese"), ("fun", 3))
  )
)(
  MetaData.submitter_("Brenda Johnson").usecase_("AddArticles")
)
```
Transaction meta data is inserted as facts with the transaction entity id as their entity id. That makes it possible
 to afterwards know that it was Brenda that added the Articles.

We can add as much custom transaction meta data to inserts as we want.

Transaction meta data can be queried as easy as our normal data. Here we find highly weighed serious articles _that Brenda submitted:_


```scala
m(Article.name.author ~ Tag.name_("serious").weight.>=(4)
  .tx_(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```
Molecule's special generic `tx` attribute lets you fetch/apply transaction meta data. See more in [Transaction meta data](/manual/transactions/tx-meta-data/)



### Next

[Read / get...](/manual/crud/get)