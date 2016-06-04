---
date: 2015-01-02T22:06:44+01:00
title: "Transaction"
weight: 30
menu:
  main:
    parent: schema
    identifier: schema-transaction
---

# Schema transaction

To create our Datomic database we need to transact some schema transaction data.


## Schema transaction data

Molecule transforms our [Schema definition file](/manual/schema) to
basically a `java.util.List` containing a `java.util.Map` of schema transaction data for each attribute defined. 
Our `name` and `url` attributes for instance requires the following map of information to be transacted in Datomic:

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
}
```
As you see, the `Community` namespace information is present in the value of the first pair in the map for 
the `name` attribute: 

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
- [partition tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/part/Partition.scala) 


## Transact/create Datomic database

Now we can simply pass the generated raw transaction data to Datomic in order to create our partitions/schema:

```scala
import datomic._
import molecule.DatomicFacade._

// Setup database
val uri = "datomic:mem://seattle"
Peer.deleteDatabase(uri)
Peer.createDatabase(uri)
implicit val conn = Peer.connect(uri)

// Transact partitions/schema
conn.transact(SeattleSchema.partitions) // Optional
conn.transact(SeattleSchema.namespaces)
```

(To be sure that we start off with a fresh in-memory database, we first delete any existing database for our URI).

Alternatively we can do all the above with `recreateDbFrom`:

```scala
implicit val conn = recreateDbFrom(SeattleSchema)
```

After saving the Datomic in an implicit val we can start issuing Molecule queries.
molecules for the loaded domain.