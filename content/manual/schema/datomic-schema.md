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

A Molecule schema definition of attributes `name` and `url` like

```scala
trait SeattleDefinition {
  trait Community {
    val name         = oneString.fullTextSearch
    val url          = oneString
    // etc...
  }
}
```
will generate a corresponding Datomic schema definition with the following list of maps of key/values:

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
Note how each attribute name is prefixed with the namespace name (":community/name"). 

Our definition is transformed to a transactional data format that we can directly feed into Datomic to create a Datomic schema:

```scala
conn.transact(SeattleSchema.tx).get()
```