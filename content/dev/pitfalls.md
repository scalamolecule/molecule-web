---
title: "Pitfalls"
weight: 60
menu:
  main:
    parent: dev
    identifier: dev-pitfalls
---

# Pitfalls


#### Cannot resolve overloaded method 'inputMolecule' {#101}
When forgetting to explicitly calling `m` on an input molecule
```scala
val inputMolecule = Community.name(?)
inputMolecule("Ben") // will not compile and likely be inferred as an error in your IDE
``` 

Input molecule needs to be declared explicitly with the `m` method
```scala
val inputMolecule = m(Community.name(?))
// Now we can apply value to input molecule
inputMolecule("Ben")
``` 


#### JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused {#201}

Datomic transactor is not running - please start it again.