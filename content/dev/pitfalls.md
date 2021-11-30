---
title: "Pitfalls"
weight: 60
menu:
  main:
    parent: dev
    identifier: dev-pitfalls
---

# Pitfalls


### Cannot resolve overloaded method 'inputMolecule' {#101}
When forgetting to explicitly calling `m` on an input molecule
```scala
val inputMolecule = Community.name(?)
inputMolecule("Ben") // will not compile and likely be inferred as an error in your IDE
``` 

Input molecule needs to be declared explicitly with the `m` method
```scala
val inputMolecule = m(Community.name(?))
// Now we can apply value to the input molecule
inputMolecule("Ben")
``` 


### JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused {#201}

Datomic transactor is not running - please [start it again](https://docs.datomic.com/on-prem/storage.html#start-transactor), with for instance one of the following:

    cd <your-datomic-free-distribution>
    bin/transactor config/samples/free-transactor-template.properties
    
    cd <your-datomic-starter/pro-distribution>
    bin/transactor config/samples/dev-transactor-template.properties
    
    cd <your-datomic-starter/pro-distribution>
    bin/run -m datomic.peer-server -h localhost -p 8998 -a k,s -d personDb,datomic:mem://personDb

See [Database setups](/setup/db-setups/) for more info.


### Configuring shared source maps

Adding the following configuration to a shared project in the `build.sbt` file makes sure that sourcemaps for shared code are configured even on remote hosts (see [explanation](https://github.com/vmunier/play-scalajs.g8/issues/112#issuecomment-815252252)):

    .jsConfigure(_.enablePlugins(ScalaJSWeb))

It just creates problems for the MoleculePlugin when creating jars for the project. 
After jars from compiled generated molecule source code files have been created, the MoleculePlugin deletes the source files and compiled classes. Something then goes wrong for ScalaJSWeb so that it can't find the class files:   

    [error] java.io.FileNotFoundException: /<path...>/shared/.js/target/scala-2.13/classes/db/dsl/MBrainz/Release.class (No such file or directory)

A workaround is to 

- remove `.jsConfigure(_.enablePlugins(ScalaJSWeb))` or
- set `moleculeMakeJars := false` to leave generated source code as is or
- find out how to get the two to work together :-)


### Gobal / onload

Another similar problem when creating jars from generated source code is when the following configuration is applied to automatically load the server project when running `sbt`:

    Global / onLoad := (Global / onLoad).value.andThen(state => "project server" :: state)

As with the above pitfall, the workaround is the same: skip this setting, set `moleculeMakeJars := false` or explore how both can co-exist. 