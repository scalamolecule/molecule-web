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
object SeattleSchema extends Schema {

  lazy val tx = Util.list(

    // Community ------------------------------------------------

    Util.map(":db/id"                , Peer.tempid(":db.part/db"),
             ":db/ident"             , ":community/name",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db.install/_attribute", ":db.part/db"),

    Util.map(":db/id"                , Peer.tempid(":db.part/db"),
             ":db/ident"             , ":community/url",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db.install/_attribute", ":db.part/db"),
           
    // etc...
  )
}
```
We transact our schema by simply feeding `tx` into Datomic:

```scala
conn.transact(SeattleSchema.tx).get()
```