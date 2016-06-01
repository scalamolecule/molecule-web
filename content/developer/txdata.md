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

A [Schema definition file](/manual/schema/attributes) like
```scala
object SeattleDefinition {
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
             
    // etc...
}
```

To avoid having to write this code manually, the 
[MoleculeBoilerplate](https://github.com/scalamolecule/molecule/blob/master/project/MoleculeBoilerplate.scala) 
file generates this for us based on our schema definition. It has to be in our 
[project folder](https://github.com/scalamolecule/molecule/tree/master/project) for sbt to use it to generate 
our boilerplate code when we run `sbt compile`.
