---
date: 2014-05-14T02:13:50Z
title: "Setup"
weight: 30
menu:
  main:
    parent: home
---

# Setup Molecule


### Download the Molecule library with all tests

1. `git clone https://github.com/scalamolecule/molecule.git`
2. `cd molecule`
3. `sbt compile`
4. Open in your IDE
5. Run tests and poke around...


### Or try the demo project...

1. `git clone https://github.com/scalamolecule/molecule-demo.git`
2. `cd molecule-demo`
3. `sbt compile`
4. Open in your IDE
5. Run tests and poke around...


### Dependency in your project

Molecule 0.6.3 for Scala 2.11.8 is available at [Sonatype](https://oss.sonatype.org/content/repositories/releases/org/scalamolecule/molecule_2.11/) so that you can add a dependency in your sbt file:

`"org.scalamolecule" % "molecule_2.11" % "0.6.3"`

Since Molecule generates boilerplate code from your definitions it also needs to have the `MoleculeBoilerplate.scala` file in your project folder. Add a copy of this file to your own project before compiling with sbt:

2. Setup your sbt build file [as in Molecule][moleculesbt]: 
    - Add library dependency<br>
    `"org.scalamolecule" % "molecule_2.11" % "0.6.3"`
    - List directories where you have your [definition file(s)][dbsetup]
3. Define your domain schema in a [schema definition file][schema]
4. `sbt compile`
5. Open in your IDE
6. [Setup your database][dbsetup]
7. [Populate your database][populate] with data
8. [Make molecule queries][tutorial]


[dbsetup]: http://scalamolecule.org/manual/setup
[schema]: http://scalamolecule.org/manual/schema-definition
[populate]: http://scalamolecule.org/manual/populate-database
[tutorial]: http://scalamolecule.org/tutorials/seattle