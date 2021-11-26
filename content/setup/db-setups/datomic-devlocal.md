---
title: "Datomic dev-local/cloud"
weight: 40
menu:
  main:
    parent: setup-db-setups
---

# Datomic dev-local/cloud

Minimal project setup to test a [Cloud setup locally](https://docs.datomic.com/cloud/dev-local.html) without connecting to a server.


```scala
import sbt.Keys._

lazy val demo = project.in(file("."))
  .aggregate(app)
  .settings(name := "molecule-datomic-devlocal")

lazy val app = project.in(file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
    scalaVersion := "2.13.5",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "clojars" at "https://clojars.org/repo",
      Resolver.mavenLocal
    ),

    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "1.0.0",
      "com.datomic" % "dev-local" % "0.9.232"
    ),

    // path to domain model directory
    moleculeDataModelPaths := Seq("app"),

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),

    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )
```


### 1. Create local client

```scala
implicit val conn = Datomic_DevLocal("datomic-samples").recreateDbFrom(SampleSchema, "sampledb")
```

Or, when the database has been created, only connect to it:

```scala
implicit val conn = Datomic_DevLocal("datomic-samples").connect("sampledb")
```


### 2. Make molecules

Having an implicit connection in scope, we can start transacting and querying `sampledb` with molecules:

```scala
// Transact
Person.name("John").age(24).save

// Query
Person.name.age.get.map(_.head ==> ("John", 24))

// etc..
```


Add/change definitions in the SampleDataModel and run `sbt clean compile -Dmolecule=true` in your project root to have Molecule re-generate boilerplate code. Then you can try out using your new attributes in new molecules in `SampleApp`.

