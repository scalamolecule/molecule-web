---
prev: /database/quick-start
---

# SBT setup


Molecule uses the sbt [MoleculePlugin](https://github.com/scalamolecule/sbt-molecule) to generate boilerplate code that enables you to write molecules. 

Add the latest version of the plugin in `project/plugins.sbt`:

```scala
addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "1.19.4")
```

Enable the plugin in your `build.sbt` file and import the molecule library that corresponds to the database(s) that you will use:
```scala
lazy val app = project
  .enablePlugins(MoleculePlugin)
  .settings(
      libraryDependencies ++= Seq(
        // one or more db-specific imports
        "org.scalamolecule" %% "molecule-db-h2" % "0.24.2",
        "org.scalamolecule" %% "molecule-db-mariadb" % "0.24.2",
        "org.scalamolecule" %% "molecule-db-mysql" % "0.24.2",
        "org.scalamolecule" %% "molecule-db-postgres" % "0.24.2",
        "org.scalamolecule" %% "molecule-db-sqlite" % "0.24.2",
      )
    )
```
Use `%%%` instead of `%%` for cross-compiled Scala.js projects.

Define your [Domain Structure](/database/setup/domain-structure) and then run

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
    └── resources
        └── moleculeGen
            └── Bar
                └── Bar_Schema_h2.sql
                └── Bar_Schema_mariadb.sql
                └── Bar_Schema_mysql.sql
                └── Bar_Schema_postgres.sql
                └── Bar_Schema_sqlite.sql
            └── Foo
                └── Foo_Schema_h2.sql
                └── Foo_Schema_mariadb.sql
                └── Foo_Schema_mysql.sql
                └── Foo_Schema_postgres.sql
                └── Foo_Schema_sqlite.sql
    └── scala
        └── app
            └── Bar.scala
            └── Foo.scala
```

### DSL code

Boilerplate code is generated in `target/scala-3.7.1/src_managed/main/moleculeGen`. Managed source code there is not supposed to be modified since it will be overwritten on each new generation with `sbt moleculeGen`. But you can inspect the code as normal code if you like. The following files are generated there:

```
moleculeGen
└── app
    └── dsl
        └── Bar
            └── metadb // Internal db meta information
                └── Bar_MetaDb
                └── Bar_MetaDb_h2
                └── Bar_MetaDb_mariadb
                └── Bar_MetaDb_mysql
                └── Bar_MetaDb_postgres
                └── Bar_MetaDb_sqlite
            └── ops
                └── Person_     // Person operations                 
                └── Address_    // Address operations
            └── Person      // Person entry point
            └── Address     // Address entry point
        └── Foo
            // Same as for Bar...
```
The entry points files contain the starting points (`Person`, `Address` etc.) for your molecules:

```scala
import app.dsl.Bar.*

Person.name.age. // etc...
Address.street.zip. // etc...
```


## Make jars

You can also choose to package the generated code in a source and class jar with

```
sbt moleculePackage
```

This can save time on compiling the generated code on rebuilds. But at the moment IntelliJ can't infer Scala 3 files correctly in generated source jars (see this [IntelliJ issue SCL-23157](https://youtrack.jetbrains.com/issue/SCL-23157/Source-jar-in-lib-added-as-Classes)). So for now, generating sources only with `sbt moleculeGen` is recommended.





