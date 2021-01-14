---
title: "Getting started"
weight: 10
menu:
  main:
    parent: code
---

# Getting started

Presuming you have [set up](/setup) your project or cloned the [demo](https://github.com/scalamolecule/molecule-demo) project or some [sample project](https://github.com/scalamolecule/molecule-sample-projects), we can now get started coding using Molecules.


## Imports

4 imports is needed to get started using Molecule:

### 1. Molecule api

The Molecule api itself:

```scala
import molecule.datomic.api._
```


### 2. Database system

Currently you can choose between using Molecule with 

- [Datomic Peer (On-Prem)](https://www.datomic.com/on-prem.html), having the Peer process within the application process.
```scala
import molecule.datomic.peer.facade.Datomic_Peer
```
or
- [Datomic Peer Server](https://docs.datomic.com/on-prem/peer-server.html), calling the database server remotely.
```scala
import molecule.datomic.client.facade.Datomic_PeerServer  
```

### 3. Molecule boilerplate code for your domain

```scala
import app.dsl.yourDomain._   // dsl for your molecules
import app.schema.YourDomainSchema   // Your db schema
```




There's also a third option, Datomic Cloud, altough this likely requires some adaptation.

Depending on which database setup

The first thing we to is to do some imports and then connect to a dat
The first thing we to is to import our generated molecule boilerplate code and then the molecule api

The first thing we do is to import our Data Model and the Molecule API, and then create a connection to our database.  

```scala
import app.dsl.yourDomain._   // Your dsl to make your molecules
import molecule.datomic.api._ // Molecule api

import app.dsl.yourDomain._          // Your dsl to make molecules
import app.schema.YourDomainSchema   // Your db schema
import molecule.datomic.api._        // Molecule api
// The peer to transact your schema and return a conn
import molecule.datomic.peer.facade.Datomic_Peer  



```
Then we need a connection to our database. As we saw in [Setup/Create db](/setup/create-db) we can do this in various ways, also depending on which of the Datomic database systems you are using. For now we'll asume an in-memory Datomic Peer database that we recreate freshly from the generated `YourDomainSchema` file: 

```scala
implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema) 

```




The classical Create-Read-Update-Delete operations on data are a bit different using Datomic since it never overwrites or deletes data. Facts are only _asserted_ or _retracted_ in Datomic.

Molecule tries to bridge the vocabulary between these two worlds.


>All getters and operators below have an [asynchronous equivalent](/code/attributes/#syncasync-apis). Synchronous getters/operators are shown for brevity.

### Create

In Molecule you can either `save` a populated molecule or `insert` multiple tuples of data that match an "insert-moleceule"

#### `save`
3 facts asserted for a new entity:
```scala
Person.name("Fred").likes("pizza").age(38).save // or saveAsync
```

More on [save](/manual/crud/save/)...


#### `insert`
3 facts asserted for each of 3 new entities:
```scala
Person.name.age.likes insert List( // or insertAsync
  ("Fred", 38, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [insert](/manual/crud/insert/)...


### Read / get

To read data from the database we call `get` on a molecule

#### `get`

```scala
Person.name.age.likes.get === List( // or getAsync
  ("Fred", 38, "pizza"),
  ("Lisa", 7, "sushi"),
  ("Ben", 5, "pizza")
)
```
More on [get](/manual/crud/get/)...


### Time getters

Since data is only appended in Datomic we can also go back in time to look at our data!

```scala
<molecule>.getAsOf(t)      // or getAsynAsOf(t)
<molecule>.getSince(t)     // or getAsyncSince(t)
<molecule>.getWith(txData) // or getAsyncWith(txData)
<molecule>.getHistory      // or getAsyncHistory
```
These are such cool features that we have a whole section about [time](/manual/time)...



### "Update" (retract + assert)

In Datomic an update retracts the old fact and asserts the new fact.

For convenience, Molecule lets you think in terms of a classical "update" although two operations are performed in the background. The old fact is still in the database and available with the time getters.

#### `update`

```scala
Person(fredId).likes("pasta").update // Retracts "pizza" and Asserts "pasta"

// Current value is now "pasta"
Person(fredId).likes.get.head === "pasta"
```


More on [update](/manual/crud/update/)...


### Retract ("delete")

As mentioned, data is not deleted in Datomic. So it would be outright wrong to say that we "delete" data. Therefore, Molecule uses the Datomic terminology of "retracting" data which is like saying "this is no longer valid". Retracted data is no longer showing up when we query with `get` but it will be visible with the time getters.

To retract individual attribute values of an entity we apply an empty value and `update`:

```scala
// Retract what Fred likes by applying an empty value
Person(fredId).likes().update

// Fred now doesn't have any preference (but his old preference is still in history)
Person(fred).name.likes$.get.head === ("Fred", None)
```

#### `retract` entity

We can retract an entity (a group of facts with a common entity id) by calling `retract` on a `Long` entity id:
```scala
fredId.retract

// Fred is retracted from current view (but still in history)
Person(fredId).name.likes.get === Nil
```
More on [retract](/manual/crud/retract/)...
