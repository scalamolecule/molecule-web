---
date: 2014-05-14T02:13:50Z
title: "Schema transaction"
weight: 10
menu:
  main:
    parent: developer
    identifier: schema-transaction
---

# Schema transaction

In [Schema definition files](/manual/schema/attributes) like the 
[Seattle example definition](https://github.com/scalamolecule/molecule/blob/master/examples/src/main/scala/molecule/examples/seattle/schema/SeattleDefinition.scala)
```scala
object SeattleDefinition {
  trait Community {
    val name = oneString.fullTextSearch
    // more attributes...
  }
}
```
we define a number of namespaces each containing some attributes that can be of various types as we saw in 
[Define Attributes in a Schema](http://www.scalamolecule.org/manual/schema/attributes/). 

Above we see the 
definition of a `name` attribute of the `Community` namespace that gives us the following information:

- name of the attribute ("name")
- cardinality ("one")
- type (String)
- options (fullTextSearch)


### Schema transaction data

In order to transact our desired Datomic schema, Molecule transforms our schema definition file to
basically a `java.util.List` containing a `java.util.Map` 
of information for each attribute that we want.

Our `name` attribute 
for instance requires the following map of information to be transacted in Datomic:

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
If you look closely, you'll see that the `Community` namespace information is present in the
value of the first pair in the map, namely 

```scala
":db/ident", ":community/name",
```
The rest of the lines are pretty self-describing except from the last two that create and save the 
internal id of the attribute in Datomic. [Datomic schemas](http://docs.datomic.com/schema.html) are 
literally a set of datoms that have been transacted as any other data!


### Partition transaction data

Partition transaction data looks almost like attribute transaction data:

```scala
lazy val partitions = Util.list(

Util.map(":db/ident"             , ":gen",
         ":db/id"                , Peer.tempid(":db.part/db"),
         ":db.install/_partition", ":db.part/db"),
```
... except that the information is now installed internally in Datomic as partition data instead of as attribute data.

Partition examples:

- [partitioned schema definition](https://github.com/scalamolecule/molecule/blob/master/coretest/src/main/scala/molecule/part/schema/PartitionTestDefinition.scala) 
- [partition tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/main/scala/molecule/part/schema/PartitionTestDefinition.scala) 


### Transacting Partitions and Schema

Now we can simply pass the generated raw transaction data to Datomic in order to create our partitions/schema:

```scala
val uri = "datomic:mem://your-db-name"
val conn = datomic.Peer.connect(uri)
conn.transact(SeattleSchema.partitions)
conn.transact(SeattleSchema.namespaces)
```
And voilá, the Datomic partitions and schema are created!
