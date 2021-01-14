---
title: "Connect to db"
weight: 50
menu:
  main:
    parent: setup
---

# Connect to db

To use Molecule we need a connection to a [Datomic](https://www.datomic.com/) database. 


### Choosing a db setup

In [Setup sbt](/setup/setup-sbt) we saw examples of 7 different [molecule-sample-projects](https://github.com/scalamolecule/molecule-sample-projects) with various Datomic setups: 

{{< bootstrap-table "table table-bordered" >}}
| &nbsp;                | Free       | Starter/Pro | Dev-Tools |
| :-                    | :-         | :-          | :-        |
| **Peer**              | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-free-mem) / [free](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-free-free) | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-pro-mem) / [dev](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-pro-dev)   |           |
| **Peer Server**       |            | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peerserver-mem) / [dev](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peerserver-dev)   |           |
| **Dev Local (Cloud)** |            |             | [dev-local](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-devlocal) |
{{< /bootstrap-table >}}

There are some smaller variations in how the different setups connect to the database. But the principles are the same, so let's walk through those:


## Imports

First, we need to import the Molecule api:

```scala
import molecule.datomic.api._
```

And the generated boilerplate code for your Data Model:

```scala
import app.dsl.yourDomain._ 
```





## Recreate database

In the beginning of developing a new project you'll likely repeat this iteration often:

1. Apply changes to the Data Model
2. `sbt compile -Dmolecule=true`
3. Re-create in-memory database and test

To re-create an in-memory database, simply call `recreateDbFrom` with the generated Schema transaction file:
```scala
import molecule.datomic.peer.facade.Datomic_Peer._
implicit val conn = recreateDbFrom(app.schema.YourDomainSchema)
```
We assign the returned connection to an implicit `val` so that all following molecules can implicitly use it to communicate with the database.

In this case the database is just given a random name that we don't care about yet. But if you like, you can give it a name:

```scala
implicit val conn = recreateDbFrom(app.schema.YourDomainSchema, "your-db")
```

If you have a Transactor running locally on port 4334 saving data to disk, you also need to supply the protocol name "free" if you use the free Datomic Peer (else "dev" for Starter/Pro):

```scala
implicit val conn = recreateDbFrom(app.schema.YourDomainSchema, "localhost:4334://your-db", "free")
```


### Datomic URI

Datomic uses a URI to configure the [connection](https://docs.datomic.com/on-prem/peer-getting-started.html#connecting) to its database systems and describing what underlying [Storage Service](https://docs.datomic.com/on-prem/overview.html#the-storage-service) is used in this basic form:


    datomic:<protocol>://<db-identifiers> 


The protocol is "mem" for an in-memory database, "free" (Free)/"dev" (Starter/Pro) for a development database saving to local disk, "cass" for an underlying Cassandra disk storage system and [so on](https://docs.datomic.com/on-prem/javadoc/datomic/Peer.html#connect-java.lang.Object-).

The db-identifiers for an in-memory database could be "hello", and the complete uri would be "datomic:mem://hello".

With a transactor running and a local Datomic database saving to disk, the identifier could be "localhost:4334/mbrainz-1968-1973" which tells Datomic how the database is reached through localhost on port 4334 and that the database name is "mbrainz-1968-1973".





## Transact Schema

When your Data Model / Schema stabilizes and you want to persist data, you no longer want to recreate the database but instead transact the updated Schema:

```scala
import molecule.datomic.peer.facade.Datomic_Peer._
transactSchema(app.schema.YourDomainSchema, "localhost:4334://your-db")
```
This is basically how you migrate your database schema!


## Connect only

And if your Data Model hasn't changed, you can get a connection only:


```scala
import molecule.datomic.peer.facade.Datomic_Peer._
val conn = connect("localhost:4334/your-db", "dev")
```





### Next

It's time to [code some molecules](/code)
