---
title: "Errors"
weight: 30
menu:
  main:
    parent: debug
    identifier: err
up:   /manual/debug
prev: /manual/debug/debug-transactions
---


# Common errors and solutions {#contents}

As errors and solutions are encountered this list is updated to help developers working with Molecule. 

#### Compile time
- [Inferred: Cannot resolve overloaded method 'inputMolecule' 
<br>Compiled: overloaded method value apply with alternatives:](#101)

#### Runtime
- [JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused](#201)



### [☝︎](#contents) Cannot resolve overloaded method 'inputMolecule' {#101}
When forgetting to explicitly calling `m` on an input molecule
```
val inputMolecule = Community.name(?)
inputMolecule("Ben") // will not compile and likely be inferred as an error in your IDE
``` 

Input molecule needs to be declared explicitly with the `m` method
```
val inputMolecule = m(Community.name(?))
// Now we can apply value to input molecule
inputMolecule("Ben")
``` 


### [☝︎](#contents) JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused {#201}

Datomic transactor is not running - start it.