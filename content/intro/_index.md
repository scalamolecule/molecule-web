---
title: "Intro"
weight: 10
menu:
  main:
    parent: intro
---

# ![](/img/logo/molecule-logo-m-75a.png) Molecule


Molecule is a non-blocking asynchronous Scala platform to define your domain Data Model and use the defined terms and relationships as your type-inferred query language.


Molecule works on both the jvm and Scala.js platform. On the js platform you can fetch and transact data a bit like with GraphQL. But with molecules, all your domain terms are recognized by your IDE and fully type inferred so that any molecule query/transaction is guaranteed to be valid already when writing the code. Molecule then transparently makes a RPC call retrieving typed data without having to implement a shared interface on the server side.

The exact same molecule queries/transactions can be used on either the jvm or js side.

Molecule uses the powerful [Datomic](http://datomic.com) database that can model normal SQL tables, graphs, key-value and document stores. Even auditing data and time-awareness is a core built-in functionality of Datomic. 



## Why use it?

- Express queries intuitively with type interferrence.
- Have your Data Model and data guaranteed to be in sync by the compiler.
- Dynamically compose the exact data structures/molecules that your business logic needs.
- Avoid traditional over-fetching with rigid bloating domain classes.
- Make transparent RPC calls from the Client side.
- Leverage built-in auditing, time-travel etc of the powerful [Datomic](http://datomic.com) database. 
- Avoid query construction with a separate query language.
  

## How does it work?

Scala macros transform a model of your domain data into query and transaction tokens that can be run directly against the database to save and retrieve typed data:

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
for {
  // insert data
  _ <- Person.name.age.Address.street insert List(
    ("Lisa", 20, "Broadway"),
    ("John", 24, "5th Avenue")
  )
  
  _ <- Person.name.age.Address.street.get.map(_ ==> List(
    ("Lisa", 20, "Broadway"),
    ("John", 24, "5th Avenue")
  ))
} yield ()
```

## Solid industry support

Molecule is just a thin DataModel-to-query Scala translation layer on top of the heavy-weight [Datomic](https://www.datomic.com) database made by industry leaders like Rich Hickey, Stuart Halloway and more from [Cognitect](https://www.cognitect.com/), the people behind the Clojure Language. 

Datomic serves huge complex systems like [Walmart](https://www.cognitect.com/walmart-case-study.html), [Nubank](https://www.cognitect.com/nubank-case-study.html) in Brasil and [many more industries](https://www.cognitect.com/clients.html). If you want expressive power over a complex domain, Datomic and Molecule will safely serve you well.



## Try it

```
git clone https://github.com/scalamolecule/molecule-samples.git
```
Open one of the projects in the [sample projects repo](https://github.com/scalamolecule/molecule-samples) in your IDE and run molecule queries with a ready-to-run in-memory database, no configuration needed. Make your own molecules and test straight away, or add new attributes to use. Or look at other [database setups](/setup/db-setups).



### Next

[Philosophy...](/intro/philosophy)
