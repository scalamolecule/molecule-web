---
date: 2015-01-02T22:06:44+01:00
title: "Datomic"
weight: 88
menu:
  main:
    parent: schema
    identifier: datomic-schema
---

# Datomic schema

Our [Schema definition file](/schema/definition) will generate a corresponding Datomic schema file:

```scala
object SeattleSchema extends Transaction {
  
  lazy val partitions = Util.list()

  lazy val namespaces = Util.list(
    
    // Community --------------------------------------------------------

    Util.map(":db/ident"             , ":community/name",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db/index"             , true.asInstanceOf[Object],
             ":db/id"                , Peer.tempid(":db.part/db"),
             ":db.install/_attribute", ":db.part/db"),

    Util.map(":db/ident"             , ":community/url",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/index"             , true.asInstanceOf[Object],
             ":db/id"                , Peer.tempid(":db.part/db"),
             ":db.install/_attribute", ":db.part/db"),
           
    // etc...
  )
}
```
We transact our schema by simply supplying the `SeattleSchema` object to the `load` method of `molecule.DatomicFacade`:

```scala
implicit val conn = load(SeattleSchema)
```

When we assign the returned `datomic.Connection` to an implicit value, we can start making molecules.