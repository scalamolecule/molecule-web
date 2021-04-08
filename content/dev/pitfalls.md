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


#### Configuring shared sourcemaps

Adding the following configuration to a shared project in the `build.sbt` file make sure that sourcemaps for shared code are configured even on remote hosts:

    .jsConfigure(_.enablePlugins(ScalaJSWeb))

It just creates problems for the MoleculePlugin when creating jars for the project. 
After jars from compiled generated molecule source code files have been created, the MoleculePlugin deletes the source files and compiled classes. Something then goes wrong for ScalaJSWeb so that it can't find the class files:   

    [error] java.io.FileNotFoundException: /<path...>/shared/.js/target/scala-2.13/classes/db/dsl/MBrainz/Release.class (No such file or directory)

A workaround is to 

- remove `.jsConfigure(_.enablePlugins(ScalaJSWeb))` or
- set `moleculeMakeJars := false` to leave generated source code or
- find out how to get the two to work together :-)