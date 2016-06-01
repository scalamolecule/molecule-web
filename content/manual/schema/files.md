---
date: 2015-01-02T22:06:44+01:00
title: "Files"
weight: 10
menu:
  main:
    parent: schema
---

# Schema files organization

Your Molecule schema definition file should be in a folder named "schema" anywhere within your source code:

![](/img/boilerplate/definition-file.jpg)

### build.sbt

In your `build.sbt` settings you then tell sbt where such "schema" folders reside:

```scala
.settings(Seq(definitionDirectories(
  "molecule/examples/seattle"
)))
```

Copy and paste the [`definitionDirectories` method](https://github.com/scalamolecule/molecule-demo/blob/master/build.sbt#L37-L52) 
into your `build.sbt` file and add the [`MoleculeBoilerplate` file](https://github.com/scalamolecule/molecule-demo/blob/master/project/MoleculeBoilerplate.scala)
in your `project` folder so that it can do all the heavy lifting of converting your schema definition into DSL code.


### SBT packaging your custom DSL code...

Running `sbt compile` in your project root will then do 5 things:

1. Generate molecule boilerplate dsl source code files (in the `src_managed` directory in target)
2. Generate a schema file with the necessary code to transact the Datomic schema  
3. Compile the generated code
4. Package both the source code and compiled classes into two `jar`s and place them in the `lib` directory of your module
5. Remove the generated source code and compiled classes

Molecule create the `jars` so that you can use the boilerplate code without having to recompile any generated boilerplate code 
each time you recompile your project:


### 

![](/img/boilerplate/lib.png)

 
We can then recreate the database with the new schema

```scala
implicit val conn = recreateDbFrom(molecule.examples.seattle.schema.SeattleSchema)
```

... and start using our DSL

```scala
import molecule.examples.seattle.dsl.seattle._

// Populate
Community.name.url.orgtype$.category$.Neighborhood.name.District.name.region$ insert seattleData

// Query
Community.name.get(3) === List(
  "KOMO Communities - Ballard",
  "Ballard Blog",
  "Ballard Historical Society")
```

[More on the boilerplate dsl files...](/manual/schema/boilerplate)

[More on the Datomic schema...](/manual/schema/datomic-schema)