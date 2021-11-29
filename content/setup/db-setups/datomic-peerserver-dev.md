---
title: "Datomic Peer Server, dev"
weight: 31
menu:
  main:
    parent: setup-db-setups
---

# Datomic Peer Server, persisted

Minimal project setup to persist data to disk with Molecule and a [Datomic Peer Server](https://docs.datomic.com/on-prem/peer-server.html) database (protocol: dev).

```scala
import sbt.Keys._

lazy val demo = project.in(file("."))
  .aggregate(app)
  .settings(name := "molecule-datomic-peerserver-dev")

lazy val app = project.in(file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
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

### 1. Start transactor

First, you need to [start a Datomic transactor](https://docs.datomic.com/on-prem/storage.html#start-transactor) in its own process:

    cd <your-datomic-starter/pro-distribution>
    bin/transactor config/samples/dev-transactor-template.properties

Note that although the Peer in your application code in this project setup is the pro version of Peer, the transactor can be of any type - a free or a starter/pro. As long as your application code can reach it via a matching host:port it will work. A datomic database is in other words interchangeable between free/pro.

### 2. Create database

Peer Servers do not own databases. As such, the Peer Server cannot create or destroy a database.

You can instead manage databases with the Peer library by for instance running the code in CreateSampleDb or by using the Datomic shell (in a separate process from the transactor):

    bin/shell
    datomic % Peer.createDatabase("datomic:dev://localhost:4334/sampledb");

Exit the datomic shell with ctrl-c or similar.


### 3. Start Peer Server

    bin/run -m datomic.peer-server -h localhost -p 8998 -a k,s -d sampledb,datomic:dev://localhost:4334/sampledb

In this setup we use a [database connection URI](https://docs.datomic.com/on-prem/javadoc/datomic/Peer.html#connect-java.lang.Object-) with the "dev" protocol which is intended for development databases that are persisted on local disk. See [other storage options](https://docs.datomic.com/on-prem/storage.html) for alternative storage options.

The other connection options explained:

    -m datomic.peer-server                             // the peer-server command
    -h localhost                                       // host name
    -p 8998                                            // port number
    -a k,s                                             // access-key,secret
    -d sampledb,datomic:dev://localhost:4334/sampledb  // dbName-alias,URI

There can be no space after comma in the pairs of options!

For simplicity, we just chose to write "k,s" for access-key,secret. The important thing is that you need to supply the same pair when you connect to the Peer Server in your code (as with host/port names).

If successful, it will show something like "Serving datomic:mem://sampledb as sampledb".


### 4. Connect to Peer Server

Presuming the transactor is running and the Peer Server is serving `sampledb` we can connect to it:

```scala
implicit val conn = 
  Datomic_PeerServer("k", "s", "localhost:8998")
   .connect("sampledb")
```

We use the same coordinates here as when we started the Peer Server.

For the purpose of testing, we want to make sure that our schema is up-to-date and therefore transact it on every run:

```scala
implicit val conn = 
  Datomic_PeerServer("k", "s", "localhost:8998")
    .transactSchema(SampleSchema, "sampledb")
```


### 5. Make molecules

From here on, we can start transacting and querying `sampledb` with molecules:

```scala
// Transact
Person.name("John").age(24).save

// Query
Person.name.age.get.map(_.head ==> ("John", 24))
```


Add/change definitions in the SampleDataModel and run `sbt clean compile -Dmolecule=true` in your project root to have Molecule re-generate boilerplate code. Then you can try out using your new attributes in new molecules in `SampleApp`.
