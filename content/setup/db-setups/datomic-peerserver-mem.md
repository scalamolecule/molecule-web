---
title: "Datomic Peer Server, mem"
weight: 30
menu:
  main:
    parent: setup-db-setups
---

# Datomic Peer Server, in-mem

Minimal project setup to test using Molecule with a [Datomic Peer Server](https://docs.datomic.com/on-prem/peer-server.html) in-memory database (protocol: mem).

```scala
import sbt.Keys._

lazy val `molecule-basic` = project.in(file("."))
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "molecule-datomic-peerserver-mem",
    scalaVersion := "2.13.7",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "clojars" at "https://clojars.org/repo",
      "my.datomic.com" at "https://my.datomic.com/repo"
    ),
    /*
      Downloading Datomic Starter/Pro requires authentication of your license:
      Create a ~/.sbt/.credentials file with the following content:

        realm=Datomic Maven Repo
        host=my.datomic.com
        id=my.datomic.com
        user=<your-username>
        pass=<your-password>

      Then let sbt provide your secret credentials:
    */
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),

    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "1.0.0",
      "com.datomic" % "datomic-pro" % "1.0.6269"
    ),

    // Important to exclude fee version when using pro to avoid clashes with pro version
    excludeDependencies += ExclusionRule("com.datomic", "datomic-free"),

    // path to domain model directory
    moleculeDataModelPaths := Seq("app"),

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),

    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )
```


### 1. Start Peer Server

First, you need to start the Peer Server with an automatically created in-memory database:

    cd <your-datomic-starter/pro-distribution>
    bin/run -m datomic.peer-server -h localhost -p 8998 -a k,s -d personDb,datomic:mem://personDb

In this setup we use a [database connection URI](https://docs.datomic.com/on-prem/javadoc/datomic/Peer.html#connect-java.lang.Object-) with the "mem" protocol which is intended for in-memory databases.

If you need persisting data, please see [other storage options](https://docs.datomic.com/on-prem/storage.html) or have a look at one of the other sample projects.

The other connection options explained:

    -m datomic.peer-server              // the peer-server command
    -h localhost                        // host name
    -p 8998                             // port number
    -a k,s                              // access-key,secret
    -d personDb,datomic:mem://personDb  // dbName-alias,URI

There can be no space after comma in the pairs of options!

For simplicity, we just chose to write "k,s" for access-key,secret. The important thing is that you need to supply the same pair when you connect to the Peer Server in your code (as with host/port names).

If successful, it will show something like "Serving datomic:mem://personDb as personDb".


### 2. Connect to Peer Server

Presuming the transactor is running and the Peer Server is serving `personDb` we can connect to it:

```scala
implicit val conn = 
  Datomic_PeerServer("k", "s", "localhost:8998")
   .connect("personDb")
```

We use the same coordinates here as when we started the Peer Server.

For the purpose of testing, we want to make sure that our schema is up-to-date and therefore transact it on every run:

```scala
implicit val conn = 
  Datomic_PeerServer("k", "s", "localhost:8998")
    .transactSchema(PersonSchema, "personDb")
```


### 3. Make molecules

From here on, we can start transacting and querying `personDb` with molecules:

```scala
// Transact
Person.name("John").age(24).save

// Query
Person.name.age.get.map(_.head ==> ("John", 24))
```


Add/change definitions in `SampleDataModel` and run `sbt clean compile -Dmolecule=true` in your project root to have Molecule re-generate boilerplate code. Then you can try out using your new attributes in new molecules in `App`.

