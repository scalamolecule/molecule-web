---
date: 2015-01-02T22:06:44+01:00
title: "Schema"
weight: 10
menu:
  main:
    parent: manual
    identifier: schema
---

# Molecule schema

Prepare your domain in 3 steps to use Molecule:

### 1. Define Schema

A Molecule Schema file defines what attributes you'll have available to build molecules.

Attributes having something in common are defined as fields in a namespace trait and we list all namespace traits in a Definition trait. 

Shortened example from the [Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html):

```scala
@InOut(3, 8)
object SeattleDefinition {

  trait Community {
    val name         = oneString.fullTextSearch
    val url          = oneString
    val category     = manyString.fullTextSearch
    val orgtype      = oneEnum('community, 'commercial, 'nonprofit, 'personal)
    val `type`       = oneEnum('email_list, 'twitter, 'facebook_page) // + more...
    val neighborhood = one[Neighborhood]
  }

  trait Neighborhood {
    val name     = oneString
    val district = one[District]
  }

  trait District {
    val name   = oneString
    val region = oneEnum('n, 'ne, 'e, 'se, 's, 'sw, 'w, 'nw)
  }
}
```

Here we have a `Community`, `Neighborhood` and `District` namespace each defining some attributes of various types.

Model your own domain structures with similar schemas and save each definition in a file in a "schema" folder anywhere within your project.

[Read more...](/manual/schema/files) 


### 2. Tell sbt

In your sbt build file you add the paths to one or more of your domains. Each of those domain directories should have one or more `schema` directories containing your schema definition files (like the one shown above):

```scala
// Add schema definition directories
.settings(Seq(definitionDirectories(
  "examples/src/main/scala/molecule/examples/dayOfDatomic",
  "examples/src/main/scala/molecule/examples/seattle",
  "examples/src/main/scala/molecule/examples/mbrainz",
  "examples/src/main/scala/molecule/examples/graph"
)))
```

[Read more...](/manual/schema/files) 

Have a look in the [build file](https://github.com/scalamolecule/molecule-demo/blob/master/build.sbt#L30-L33) of the molecule-demo project of how things are organized...


### 3. Compile (generate boilerplate code)

`sbt compile` your project, wait for your domain-customized boilerplate code to be generated - and you're ready to make intuitive molecule queries!

[Read more...](/manual/schema/files) 
