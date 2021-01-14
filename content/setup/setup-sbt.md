---
title: "Setup sbt"
weight: 30
menu:
  main:
    parent: setup
---

# Setup sbt

Once you have defined your [Data Model](/setup/data-model) sbt needs to be set up to use Molecule and be told where your Data Model is.


## MoleculePlugin

The sbt [MoleculePlugin](https://github.com/scalamolecule/sbt-molecule) creates Molecule boilerplate when you `sbt compile` your project. 

Add the latest version of the plugin in `project/buildinfo.sbt`:

```scala
addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "0.12.0")
```

## Sbt project settings

In `build.sbt`, enable the plugin, set dependency resolvers, add library dependencies and tell where the MoleculePlugin should look for your Data Model:

```scala
lazy val yourProject = project.in(file("app"))
  // Enable the MoleculePlugin
  .enablePlugins(MoleculePlugin)
  .settings(
    // Let sbt resolve datomic/molecule dependencies
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "clojars" at "https://clojars.org/repo" 
    ),
    // Import Molecule and Datomic (here the free Peer version)
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "0.23.2",
      "com.datomic" % "datomic-free" % "0.9.5697"
    ),

    // Read a flag if we should generate molecule 
    // boilerplate code when running `sbt compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    
    // paths to your Data Model files...
    moleculeDataModelPaths := Seq("app"),
    
    // Let IDE detect created Molecule jars in unmanaged lib directory
    exportJars := true
  )
```
Here, we imported the free Datomic Peer library which is a natural choice to start with since you can use it as an in-memory database straight away with no further downloads or configuration.

See examples of project setups with various Datomic databases in [molecule-sample-projects](https://github.com/scalamolecule/molecule-sample-projects):

{{< bootstrap-table "table table-bordered" >}}
| &nbsp;                | Free       | Starter/Pro | Dev-Tools |
| :-                    | :-         | :-          | :-        |
| **Peer**              | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-free-mem) / [free](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-free-free) | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-pro-mem) / [dev](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peer-pro-dev)   |           |
| **Peer Server**       |            | [mem](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peerserver-mem) / [dev](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-peerserver-dev)   |           |
| **Dev Local (Cloud)** |            |             | [dev-local](https://github.com/scalamolecule/molecule-sample-projects/tree/main/datomic-devlocal) |
{{< /bootstrap-table >}}
                      

## Data Model paths

Molecule needs to know where your Data Model(s) are. You can have a single or several Data Model files in a project. Each Data Model file corresponds to a single database. This is useful if you for instance want to experiment with various Data Models / database designs during development or want to test using molecules with various databases.

Data Model files should reside in directories named `dataModel` anywhere in your source code. Use the `moleculeDataModelPaths` sbt settings key to list paths to those directories.

Say you have a project `app` like the [molecule-demo](https://github.com/scalamolecule/molecule-demo) project and a single Data Model file `YourDomainDataModel.scala`:

![](/img/page/setup/setup1.png)

Then you simply add `moleculeDataModelPaths := Seq("app")` as we saw above.

### Multiple Data Models

The Molecule project tests shows an example of having multiple Data Model files:

![](/img/page/setup/setup2.png)

We list these paths like this in our `build.sbt`:

```scala
moleculeDataModelPaths := Seq(
  "molecule/tests/core/base",
  "molecule/tests/core/bidirectionals",
  "molecule/tests/core/ref",
  "molecule/tests/core/schemaDef",

  "molecule/tests/examples/datomic/dayOfDatomic",
  "molecule/tests/examples/datomic/mbrainz",
  "molecule/tests/examples/datomic/seattle",
  "molecule/tests/examples/gremlin/gettingStarted"
)
```




## MoleculePlugin options

Apart from the mandatory `moleculeDataModelPaths` setting key that we saw above, the MoleculePlugin has 3 more optional keys that you can set in your project build file.


### moleculePluginActive

```scala
moleculePluginActive := false // (default)
```

Set this to `true` to have Molecule boilerplate code re-generated on each project compilation.

The default value is `false` to avoid re-generating boilerplate code when your Data Model hasn't changed and you simply want to re-compile your project without affecting the existing generated boilerplate code.

A flexible way to control when re-generation should happen is to pass a property value when you want Molecule boilerplate code generated with your project compilation:

```
sbt clean compile -Dmolecule=true
```

and then set the key to this boolean value in your project build file:

```scala
moleculePluginActive := sys.props.get("molecule") == Some("true")
```
(You can call the property anything your like)

Then you can freely recompile your project without re-generating the Molecule boilerplate code:

```
sbt compile
```

### moleculeMakeJars

Normally you want generated Molecule boilerplate code available as an immutable jar:

```scala
moleculeMakeJars := true // (default)
```



If you want to be able to modify or test things with the generated boilerplate code, you can set the key to false:

```scala
moleculeMakeJars := false
```
Then the jars are not created and the generated source code available in the `src_managed` directory in `target`.


### moleculeAllIndexed

This is a Datomic-specific sbt settings key that tells wether schema attributes should all be indexed:
```scala
moleculeAllIndexed := true // (default)
```
If you set it to false, your settings in your Data Model determines which attributes are indexed.


## Project compilation

When you have set up the sbt build file you can compile your project (here with the generate-code flag as described above):

```
sbt clean compile -Dmolecule=true
```

During project compilation, a series of operations are then performed:

1. Boilerplate source code files based on your Data Model are generated.
2. Generated source code is compiled.
3. Sources and classes are packaged into two `jar`s in the `lib` directory.
4. Generated source code and compiled classes are removed.


In our [demo example](https://github.com/scalamolecule/molecule-demo) these two jars are created:

![](/img/page/setup/setup3.png)

`dsl` contains the generated Molecule boilerplate code for the defined Person Data Model and `schema` contains boilerplate code for transacting our Datomic schema. 

Whenever you make changes to your Data Model, you can simply `sbt compile -Dmolecule=true` and have your Schema transaction boilerplate code re-generated with your latest changes.


### Next

Now you can [connect to the database](/setup/connect)...