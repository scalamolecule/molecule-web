---
prev: /database/quick-start
---

# SBT setup


Molecule uses the sbt [MoleculePlugin](https://github.com/scalamolecule/sbt-molecule) to generate boilerplate code that enables you to write molecules. 

Add the latest version of the plugin in `project/plugins.sbt`:

```scala
addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "1.21.0")
```

Enable the plugin in your `build.sbt` file and import the molecule library that corresponds to the database(s) that you will use:
```scala
lazy val app = project
  .enablePlugins(MoleculePlugin)
  .settings(
      libraryDependencies ++= Seq(
        // One or more database-specific imports
        "org.scalamolecule" %% "molecule-db-h2" % "0.26.0",
        "org.scalamolecule" %% "molecule-db-mariadb" % "0.26.0",
        "org.scalamolecule" %% "molecule-db-mysql" % "0.26.0",
        "org.scalamolecule" %% "molecule-db-postgres" % "0.26.0",
        "org.scalamolecule" %% "molecule-db-sqlite" % "0.26.0",
      )
    )
```
Use `%%%` instead of `%%` for cross-compiled Scala.js projects.

Define a [Domain Structure](/database/setup/domain-structure) and then run

```
sbt moleculeGen
```
That's it.

The MoleculePlugin will generate a minimal set of boilerplate code for your domain(s).


## What is generated?

Molecule automatically scans your project (with ScalaMeta) to find DomainStructure definitions and generate boilerplate for those. 

Say that you have two domain structure definitions Foo and Bar here:

```
src
└── main
    └── scala
        └── app
            └── Bar.scala
            └── Foo.scala
```
Then the plugin will for each domain generate 

- SQL schemas to create SQL databases and
- DSL code to build molecules

### SQL schemas

SQL schema files for each available database are generated in the resources folder under the `moleculeGen` namespace to isolate them from other resources:

```
src
└── main
    ├── resources
    │   └── moleculeGen
    │       └── app
    │           ├── Bar
    │           │   ├── Bar_h2.sql
    │           │   ├── Bar_mariadb.sql
    │           │   ├── Bar_mysql.sql
    │           │   ├── Bar_postgresql.sql
    │           │   └── Bar_sqlite.sql
    │           └── Foo
    │               ├── Foo_h2.sql
    │               ├── Foo_mariadb.sql
    │               ├── Foo_mysql.sql
    │               ├── Foo_postgresql.sql
    │               └── Foo_sqlite.sql
    └── scala
        └── app
            ├── Bar.scala
            └── Foo.scala
```

### DSL code

Boilerplate DSL code is generated in `target/scala-3.7.3/src_managed/main/moleculeGen`. Managed source code there is not supposed to be modified since it will be overwritten on each new generation with `sbt moleculeGen`. But you can inspect the code as normal code if you like. The following files are generated:

```
moleculeGen
└── app
    └── dsl
        ├── Bar
        │   ├── Address.scala   // Address entry point
        │   ├── Person.scala    // Person entry point
        │   ├── metadb
        │   │   ├── Bar_.scala
        │   │   ├── Bar_h2.scala
        │   │   ├── Bar_mariadb.scala
        │   │   ├── Bar_mysql.scala
        │   │   ├── Bar_postgresql.scala
        │   │   └── Bar_sqlite.scala
        │   └── ops
        │       ├── Address_.scala   // Address operations
        │       └── Person_.scala    // Person operations
        └── Foo
            // Same as for Bar...
```

In the [domain structure](/database/setup/domain-structure) for the domain `Bar`, two entities `Person` and `Address` have been defined. These are the starting point for building molecules:
```scala
import app.dsl.Bar.*

Person.name.age. // etc...
Address.street.zip. // etc...
```


## Make jars

Instead of running `sbt moleculeGen` you can also choose to package the generated code in a source and class jar with

```
sbt moleculePackage
```

This can save time on compiling the generated code on rebuilds. But at the moment IntelliJ can't infer Scala 3 files correctly in generated source jars (see this [IntelliJ issue SCL-23157](https://youtrack.jetbrains.com/issue/SCL-23157/Source-jar-in-lib-added-as-Classes)). So for now, generating sources only with `sbt moleculeGen` is recommended.





