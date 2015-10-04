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

A Molecule Schema defines what attributes you'll have available to build molecules.

Prepare your domain in 3 steps to use Molecule:

### 1. Define Schema

A schema defines what attributes you'll have available to build molecules.

Attributes having something in common are defined as fields in a namespace trait and we list all namespace traits in a Definition trait. 

Shortened example from the [Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html):

```scala
@InOut(3, 8)
object SeattleDefinition {

  trait Community {
    val name         = oneString.fullTextSearch
    val url          = oneString
    val category     = manyString.fullTextSearch
    val orgtype      = oneEnum('community, 'commercial, 'nonprofit)
    val `type`       = oneEnum('email_list, 'twitter, 'facebook_page)
    val neighborhood = one[Neighborhood]
  }

  trait Neighborhood {
    val name     = oneString.fullTextSearch.uniqueIdentity
    val district = one[District]
  }

  trait District {
    val name   = oneString.fullTextSearch.uniqueIdentity
    val region = oneEnum('n, 'ne, 'e, 'se, 's, 'sw, 'w, 'nw)
  }
}
```

Here we have a `Community`, `Neighborhood` and `District` namespace each defining some attributes of various types.

Model your own domain structures with similar schemas and save each definition in a file in a "schema" folder anywhere within your project.

[Read more...](/manual/) 


### 2. Tell sbt

Tell sbt where to find your "schema" folders containing definition files:

```scala
settings = commonSettings ++ Seq(
  definitionDirectories(
    "examples/src/main/scala/molecule/examples/dayOfDatomic",
    "examples/src/main/scala/molecule/examples/seattle",
    "examples/src/main/scala/molecule/examples/mbrainz"
  )
)
```

[Read more...](/manual/) 


### 3. Compile (generate boilerplate code)

`sbt compile` your project, wait for your domain-customized boilerplate code to be generated - and you're ready to make intuitive molecule queries!

[Read more...](/manual/) 
