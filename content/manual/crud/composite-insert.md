---
date: 2015-01-02T22:06:44+01:00
title: "Composite insert"
weight: 30
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/insert
next: /docs/crud/get
down: /docs/transactions
---

# Composite inserts

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Composite.scala)


[Composite](/docs/relationships/composites/) data sets can be inserted with a special `insert` method
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
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)() // empty optional tx meta data parameter group
```

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
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
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
Molecule's special generic `tx` attribute lets you fetch/apply transaction meta data. See more in [Transaction meta data](/docs/transactions/tx-meta-data/)



### Next

[Read / get...](/docs/crud/get)