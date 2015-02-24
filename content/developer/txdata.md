---
date: 2014-05-14T02:13:50Z
title: "Tx data"
weight: 10
menu:
  main:
    parent: developer
    identifier: txdata
---

# Schema transaction data

A [Schema definition file](/schema/definition) like
```scala
trait SeattleDefinition {
  trait Community {
    val name = oneString.fullTextSearch
  }
}
```
defines 

- name ("name")
- cardinality ("one")
- type (String)
- options (fullTextSearch)

of a `name` attribute in the `Community` namespace.

This is enough information to generate the necessary code to transact the schema in Datomic:

```scala
object SeattleSchema extends Schema {

  lazy val tx = Util.list(

    // Community ------------------------------------------------

    Util.map(":db/id"                , Peer.tempid(":db.part/db"),
             ":db/ident"             , ":community/name",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db.install/_attribute", ":db.part/db")
  )
}
```

To avoid having to write this code manually, the `MoleculeBoilerplate` file generates is for us based on our schema definition. It has to be in our project folder for sbt to use it to generate our boilerplate code when we run `sbt compile`.

- [File organization](/manual/schema/files)
- [Types and options](/manual/schema/definition)