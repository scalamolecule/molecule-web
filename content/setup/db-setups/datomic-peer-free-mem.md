---
title: "Datomic Peer free, mem"
weight: 10
menu:
  main:
    parent: setup-db-setups
---

# Datomic Peer (free), in-mem

Minimal project setup to test using Molecule with a free [Datomic Peer](https://docs.datomic.com/on-prem/peer-getting-started.html) in-memory database (protocol: mem).

```scala
import sbt.Keys._

lazy val `molecule-basic` = project.in(file("."))
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "molecule-datomic-peer-free-mem",
    scalaVersion := "2.13.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "clojars" at "https://clojars.org/repo"
    ),
  
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "1.0.3",
      "com.datomic" % "datomic-free" % "0.9.5697"
    ),

    // path to domain model directory
    moleculeDataModelPaths := Seq("app"),

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
  
    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )
```

### 1. Connect to Peer

Connect, recreate in-memory database and get database connection

```scala
implicit val conn = Datomic_Peer.recreateDbFrom(SampleSchema) 
```

Since we are not persisting the database, we let Molecule create a random database name. We don't need to supply the default "mem" protocol either. So this is really simple.

If you need persisting data, please see [other storage options](https://docs.datomic.com/on-prem/storage.html) or have a look at one of the other sample projects.


### 2. Make molecules

Having an implicit connection in scope, we can start transacting and querying `sampleDb` with molecules:
```scala
// Transact
Person.name("John").age(24).save

// Query
Person.name.age.get.map(_.head ==> ("John", 24))
```


Add/change definitions in the SampleDataModel and run `sbt clean compile -Dmolecule=true` in your project root to have Molecule re-generate boilerplate code. Then you can try out using your new attributes in new molecules in `App`.

