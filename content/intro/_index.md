---
title: "Intro"
weight: 10
menu:
  main:
    parent: intro
---

# ![](/img/logo/molecule-logo-m-75a.png) Molecule


## What is it?

Molecule is an ultra-intuitive and compact query language in Scala that uses your domain terms as query tokens.


## Why use it?

- Sleep well, knowing that domain model and data are kept in sync by the compiler.
- Eliminate "query construction" - model your domain, and you'll get the matching data.
- Compose expressive type-safe molecular domain/data structures for any use case.
- Enjoy using molecules with the semantically most powerful database: [Datomic](http://datomic.com). 
  

## How does it work?

Scala macros transform a Schema definition of your domain into query and transaction tokens that can be run directly against a server-side database to save and retrieve typed data:

_Schema Definition_
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
_Typed transactions and queries_

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

Molecule can run against the three [Datomic](https://www.datomic.com) database systems Peer (On-Prem), Peer Server and Cloud, and more databases are being added.

See [more advanced molecules]()...


## Can I try it within 60 seconds?

Yes:

```
git clone https://github.com/scalamolecule/molecule-demo.git
```
Open the [demo project](https://github.com/scalamolecule/molecule-demo) in your IDE and run queries with your own molecules.


