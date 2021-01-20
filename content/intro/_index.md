---
title: "Intro"
weight: 10
menu:
  main:
    parent: intro
---

# ![](/img/logo/molecule-logo-m-75a.png) Molecule


## What is it?


Molecule is a Scala eco-system to define your domain Data Model and use that as your query language.

Molecule uses the powerful [Datomic](http://datomic.com) database. 



## Why use it?

- Express queries more intuitively than ever possible - since it's _your_ language.
- Enjoy having the compiler enforce that your Data Model and data stay in sync.
- Dynamically compose the exact data structures/molecules that your business logic needs.
- Avoid traditional over-fetching with rigid bloating domain classes.
- Leverage built-in auditing, time-travel etc of the powerful [Datomic](http://datomic.com) database. 
- Let go of "query construction" and a separate "query language".
- Save time, increase clarity and have more fun!
  

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
  ("John", 24, "5th Avenue")
)

Person.name.age.Address.street.get === List(
  ("Lisa", 20, "Broadway"),
  ("John", 24, "5th Avenue")
)
```

## Is there a future here?

Molecule is just a thin DataModel-to-query Scala translation layer on top of the heavy-weight [Datomic](https://www.datomic.com) database made by industry leaders like Rich Hickey, Stuart Halloway and more from [Cognitect](https://www.cognitect.com/), the people behind the Clojure Language. 

Datomic serves huge complex systems like [Walmart](https://www.cognitect.com/walmart-case-study.html), [Nubank](https://www.cognitect.com/nubank-case-study.html) in Brasil and [many more industries](https://www.cognitect.com/clients.html). If you want expressive power over a complex domain, Datomic and Molecule will serve you well.



## Can I try it out quickly?

Yes:

```
git clone https://github.com/scalamolecule/molecule-demo.git
```
Open the [demo project](https://github.com/scalamolecule/molecule-demo) in your IDE and run molecule queries with a ready-to-run in-memory database, no configuration. Make your own molecules and test straight away, or add new attributes to use. Or look at other [database setups](/setup/db-setups).



### Next

[Philosophy...](/intro/philosophy)
