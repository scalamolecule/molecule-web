---
title: "Intro"
weight: 10
menu:
  main:
    parent: intro
---

# ![](/img/logo/molecule-logo-m-75a.png) Molecule


## What is it?

Molecule is a Scala meta-DSL to model and query your domain data.


## Why use it?

- Your domain data model directly describes and controls your data structures.
- The compiler enforces domain model and data to be in sync.
- Even the most complex domain and its data can be modelled and queried with ease.
- No more "query construction" as a separate excercise.
- Use the powers of the [Datomic](http://datomic.com) database.
  

## How does it work?

Scala macros transform a model of your domain data into query and transaction tokens that can be run directly against a server-side database to save and retrieve typed data:

_Domain Data Model_
```scala
trait Person {
  val name    = oneString
  val age     = oneInt
  val address = one[Address]
}
trait Address {
  val street = oneString
}
```
_Typed transactions and queries with the tokens of your domain model_

```scala
Person.name.age.Address.street insert List(
  ("Lisa", 20, "Broadway"),
  ("John", 22, "Fifth Avenue")
)

Person.name.age.Address.street.get === List(
  ("Lisa", 20, "Broadway"),
  ("John", 22, "Fifth Avenue")
)
```

## Where's my data?

Molecule data is stored in the [Datomic](https://www.datomic.com) database, either in-memory, on local disk, on a remote server or in the cloud as you like.

You can choose an on-premise setup with Datomic Peer (On-Prem) or communicate remotely with a Datomic Peer Server. Both solutions can be run on local machines/server/cloud. 


##### Molecule can also run against the Datomic Cloud API/system which is a specially tailored Clojure/AWS solution, although this will likely require some deeper adaptation which hasn't yet been explored.


## Can I try it quickly?

Yes:

```
git clone https://github.com/scalamolecule/molecule-demo.git
```
Open the [demo project](https://github.com/scalamolecule/molecule-demo) in your IDE and run molecule queries with a ready-to-run in-memory database. Make your own molecules and test straight away, or add new attributes to use.

