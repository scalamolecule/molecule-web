---
date: 2015-01-02T22:06:44+01:00
title: "Files"
weight: 84
menu:
  main:
    parent: schema
---

# Molecule files

Your Molecule schema definition file should be in a folder named "schema" anywhere within your source code:

![](/img/boilerplate/definition-file.jpg)

### Telling sbt

In the project build file we then tell sbt where such "schema" folders reside:

```scala
settings = commonSettings ++ Seq(
  definitionDirectories(
    "examples/src/main/scala/molecule/examples/dayOfDatomic",
    "examples/src/main/scala/molecule/examples/mbrainz",
    "examples/src/main/scala/molecule/examples/seattle"
  )
)
```

We can add several locations with the `definitionDirectories` method which will tell sbt where to look for our schema definition files.


### Files generated

Running `sbt compile` in your project root will then generate some source code files in the `src_managed` directory (inside the target directory):


![](/img/boilerplate/generated-files.jpg)

### Boilerplate dsl files

Molecule creates a `dsl` folder having a subfolder - or "definition directory" - for each schema definition file (could be more than one).

Within each definition directory we'll find a file generated for each namespace defined (`Community.scala` etc).

[More on the boilerplate dsl files...](/manual/schema/boilerplate)

### Schema file

Molecule places the generated Datomic schema file `SeattleSchema` in the same package as `SeattleDefinition` so that they'll turn up beside each other in our IDE package view:

![](/img/boilerplate/project-files.png)

[More on the Datomic schema...](/manual/schema/datomic)