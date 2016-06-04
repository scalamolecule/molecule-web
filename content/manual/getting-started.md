---
date: 2015-01-02T22:06:44+01:00
title: "Getting started"
weight: 10
menu:
  main:
    parent: manual
    identifier: getting-started
---

# Getting started with Molecule

To use Molecule we need to define our database schema in a [Schema definition](/manual/schema) file and then tell 
sbt about it. When compiling our project from the command line, all necessary boilerplate code is 
then automatically generated.

## 1. SBT build settings

For sbt 0.13.6+ add sbt-molecule as a dependency in `project/buildinfo.sbt`:

```scala
addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "0.1.0")
```

Add the following in your `build.sbt`:

```scala
lazy val yourProject = project.in(file("demo"))
  .enablePlugins(MoleculePlugin)
  .settings(
    resolvers ++= Seq(
      "datomic" at "http://files.datomic.com/maven",
      "clojars" at "http://clojars.org/repo",
      Resolver.sonatypeRepo("releases"),
      "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
    ),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "0.7.0",
      "com.datomic" % "datomic-free" % "0.9.5359"
    ),
    moleculeSchemas := Seq("demo") // paths to your schema definition files...
  )
```
Molecule 0.7.0 for Scala 2.11.8 is available at 
[Sonatype](https://oss.sonatype.org/content/repositories/releases/org/scalamolecule/molecule_2.11/).


# 2. Paths to Schema definition files

We use the `moleculeSchemas` sbt settings key to tell sbt where we have our Schema definition files.

A [Schema definition](/manual/schema) file contains a plain Scala object where you define 
partitions/namespaces/attributes of your Datomic database. The MoleculePlugin uses the information
defined there to create all the boilerplate code needed to use Molecule in your code.

You can have a single or several Schema definition files in a project. Each definition file defines a single database. 
This is useful if you for instance want to experiment with various database designs during development.

Schema definiton files should reside in directories named `schema` anywhere in your source code.

Use the `moleculeSchemas` sbt settings key to list the directories in your project source
code that contains your `schema` directories.

Say you have a project `demo` and a single Schema definition file `YourDomainDefinition.scala`
defining your database:

![](/img/dirs1.png)

Then you simply add `moleculeSchemas := Seq("demo")` as we saw above.

In the main Molecule project's examples module we have several Schema definition files:

![](/img/dirs2.png)

And we then list the paths to those like this in our `build.sbt`:

```scala
moleculeSchemas := Seq(
  "molecule/examples/dayOfDatomic",
  "molecule/examples/graph",
  "molecule/examples/mbrainz",
  "molecule/examples/seattle"
)
```

## 3. Compile

Now that you have created a schema definition file and told sbt about where to find it, you can compile 
your project from the terminal

```
> cd yourProjectRoot
> sbt compile
```

The MoleculePlugin will now automatically as part of the compilation process do 5 things:

1. Generate Molecule boilerplate dsl source code files (in the `src_managed` directory in target)
2. Generate a schema file with the necessary code to transact the Datomic schema  
3. Compile the generated code
4. Package both the source code and compiled classes into two `jar`s and place them in the `lib` directory of your module
5. Remove the generated source code and compiled classes

The MoleculePlugin create the `jars` so that you can use the boilerplate code without having to recompile any 
generated boilerplate code each time you recompile your project. In our demo example two jars are created:

![](/img/jars.png)


## 4. Use Molecule

The MoleculePlugin has now created all the necessary boilerplate code so that we can start using Molecule. We can
create a fresh in-memory Datomic database by supplying the generated Schema transaction code in `YourDomainSchema` 
(from the example above):

```scala
import molecule._
implicit val conn = recreateDbFrom(demo.schema.YourDomainSchema)
```

With the implicit Datomic connection available we can start making molecules:

```scala
import demo.dsl.yourDomain._

// Insert data
Person.name("John").age(26).gender("male").add

// Retrieve data
val (person, age, gender) = Person.name.age.gender.one
```

#### Read more

- [Schema definition](/manual/schema)
- [Create Datomic database](/manual/schema/transaction)

