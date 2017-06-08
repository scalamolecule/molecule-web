---
date: 2015-01-02T22:06:44+01:00
title: "Getting started"
weight: 10
menu:
  main:
    parent: docs
    identifier: getting-started
    
up:   /docs/
prev: /docs/documentation/introduction
next: /docs/getting-started/setup
down: /docs/schema
---

# Getting started with Molecule

Some suggestions:

### Watch videos

Watch the [Molecule videos](/resources/videos/2017-04-25_marc_grue/) - specially part 3 giving a tour of Molecule features.


### Try demo

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

[Setup Molecule](/docs/getting-started/setup/) in your own project and try modelling your own domain with Molecule.


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