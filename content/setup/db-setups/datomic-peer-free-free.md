---
title: "Datomic Peer free, free"
weight: 11
menu:
  main:
    parent: setup-db-setups
---

# Datomic Peer (free), persisted

Minimal project setup to persist data to disk with Molecule and a free [Datomic Peer](https://docs.datomic.com/on-prem/peer-getting-started.html) database (protocol: free).

```scala
import sbt.Keys._

lazy val demo = project.in(file("."))
  .aggregate(app)
  .settings(name := "molecule-datomic-peer-free-free")

lazy val app = project.in(file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
    scalaVersion := "2.13.5",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "clojars" at "https://clojars.org/repo",
    ),

    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "1.0.0",
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

### 1. Start transactor

First, you need to [start a Datomic transactor](https://docs.datomic.com/on-prem/storage.html#start-transactor) in its own process:

    cd <your-datomic-free-distribution>
    bin/transactor config/samples/free-transactor-template.properties

Note that although the Peer in your application code in this project setup is the free version of Peer, the transactor can be of any type - a free or a starter/pro. As long as your application code can reach it via a matching host:port it will work. A datomic database is in other words interchangeable between free/pro.

### 2. Connect to Peer

Then connect to the database:

```scala
implicit val conn = Datomic_Peer.connect("localhost:4334/sampledb", "free")
```

Or, if we want to test a clean database each time, we could recreate the database and transact the schema on each run:

```scala
implicit val conn = Datomic_Peer.recreateDbFrom(SampleSchema, "localhost:4334/sampledb", "free")
```

In this setup we use the "free" protocol which is intended for development databases that are persisted on local disk. See [other storage options](https://docs.datomic.com/on-prem/storage.html) for alternative storage options.


### 3. Make molecules

Having an implicit connection in scope, we can start transacting and querying `sampledb` with molecules:

```scala
// Transact
Person.name("John").age(24).save

// Query
Person.name.age.get.map(_.head ==> ("John", 24))
```

Add/change definitions in the SampleDataModel and run `sbt clean compile -Dmolecule=true` in your project root to have Molecule re-generate boilerplate code. Then you can try out using your new attributes in new molecules in `SampleApp`.

