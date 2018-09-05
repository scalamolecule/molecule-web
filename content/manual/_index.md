---
date: 2015-01-02T22:06:44+01:00
title: "Manual"
weight: 5
menu:
  main:
    identifier: manual

up: 
prev: 
next: /manual/documentation/introduction
down: /manual/getting-started
---

# Molecule manual

Welcome to the manual for Molecule, the type-safe Scala meta DSL to access the [Datomic](http://www.datomic.com) database.

It's highly recommended to watch the [Molecule videos](/resources/videos/2017-04-25_marc_grue/). 
This will give you an overview of Molecule and an understanding of the underlying data model of [Datomic](http://www.datomic.com).

## Quick start demo

The fastest way to build some molecules yourself is to clone the [molecule-demo](https://github.com/scalamolecule/molecule-demo)
github repo and play around with the code in your IDE:

```
git clone https://github.com/scalamolecule/molecule-demo.git
cd molecule-demo
sbt compile

// open in your IDE
// build more molecules in the App test
// Add some of your own attributes to the schema

sbt compile

// build some molecules with your own attributes!
```

### Setup Molecule in your own project

[Setup Molecule](/manual/getting-started/setup/) in your own project and try modelling your own domain with Molecule.


### Study example/core tests

When you have acquired some experience with Molecule you can clone the Molecule repo and dive into the extensive example/core tests.
This is where the rubber hits the road and the widest range of capabilities are tested. You can then apply some of the techniques to
your own code

```
git clone https://github.com/scalamolecule/molecule.git
cd molecule
sbt compile // this can take a few minutes since there are thousands of molecules in the project :-)

// open in your IDE and explore
```

### Next

A brief [Introduction](/manual/documentation/introduction/) to Molecule...

or

Straight on to [Getting started](/manual/getting-started/)...


### Improvements

[Issues and suggestions](https://github.com/scalamolecule/molecule-docs/issues/new) are welcome!