---
title: "3. Db connection"
weight: 40
menu:
  main:
    parent: setup
---

# Database connection

To use Molecule we need a few imports and a connection to a [Datomic](https://www.datomic.com/) database. 

There are some smaller variations in how the different setups connect to the database. But the principles are the same, so let's walk through those:


## API & DSL imports

Import the Molecule api and the generated DSL for your Data Model

```scala
import molecule.datomic.api._
import app.dsl.yourDomain._
```

## Database system

Choose a Datomic database system:

#### Peer
The [Datomic Peer](https://docs.datomic.com/on-prem/peer-getting-started.html) runs in your application process.
```scala
import molecule.datomic.peer.facade.Datomic_Peer._
```

#### Peer Server
The [Datomic Peer Server](https://docs.datomic.com/on-prem/peer-server.html) runs on a remote server (could be local also).
```scala
import molecule.datomic.client.facade.Datomic_PeerServer._
```

#### Dev-local (Cloud)
[Datomic dev-local](https://docs.datomic.com/cloud/dev-local.html) provides a local testing environment for Datomic Cloud without connecting to a server.
```scala
import molecule.datomic.client.facade.Datomic_DevLocal._
```

## Connection

Get a connection to the database in 3 different ways:

### Re-create database and schema


```scala
import app.schema.YourDomainSchema

// In-memory connection (default) 
implicit val conn = recreateDbFrom(YourDomainSchema)

// .. or with storage service
// WARNING: this completely destroys the database and creates a new empty one!
implicit val conn = recreateDbFrom(YourDomainSchema, datomic-db-uri, protocol)
```

### Transact schema (migrate)

```scala
import app.schema.YourDomainSchema

// In-memory connection (default) 
implicit val conn = transactSchema(YourDomainSchema)

// .. or with storage service
implicit val conn = transactSchema(YourDomainSchema, datomic-db-uri, protocol)
```
We can transact our complete schema as often as we want. If a transaction value is the same as the current value in the database, Datomic simply ignores it. Whereas changes we make to your Data Model / Schema will be transacted.

This makes it easy to migrate our schema: we make changes to our Data Model, `sbt compile -Dmolecule=true` and transact our updated generated Schema transaction file. 

And if some of our code is still using an old attribute definition, the compiler will warn us. The compiler in this way help us enforce that our code, data, schema and Data Model all stay in sync! We won't be able to save data with a molecule that is outdated since it won't compile.


### Connect

When no changes are needed to our schema, we simply connect to our database.
```scala
implicit val conn = connect(datomic-db-uri, protocol)
```


### Datomic db URI + protocol

Datomic uses a URI to configure the [connection](https://docs.datomic.com/on-prem/peer-getting-started.html#connecting) to its database systems and describing what underlying [Storage Service](https://docs.datomic.com/on-prem/overview.html#the-storage-service) is used in this basic form:


    datomic:<protocol>://<db-identifiers> 


The protocol is "mem" for an in-memory database, "free" (Free)/"dev" (Starter/Pro) for a development database saving to local disk, "cass" for an underlying Cassandra disk storage system and [so on](https://docs.datomic.com/on-prem/javadoc/datomic/Peer.html#connect-java.lang.Object-).

The db-identifiers for an in-memory database could be "hello", and the complete uri would be "datomic:mem://hello".

With a transactor running and a local Datomic database saving to disk, the identifier could be "localhost:4334/mbrainz-1968-1973" which tells Datomic how the database is reached through localhost on port 4334 and that the database name is "mbrainz-1968-1973".




### Next

To help you choose a database setup, you can explore the [Example database setups...](/setup/examples)
