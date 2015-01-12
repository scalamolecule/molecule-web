---
date: 2014-05-14T02:13:50Z
title: "Setup"
weight: 30
menu:
  main:
    parent: home
---

# Setup Molecule


### Download code

1. `git clone https://github.com/scalamolecule/molecule.git`
2. `sbt compile`
3. Import into your IDE
4. Run tests and poke around...


### Dependency in your project

Molecule 0.2.0 for Scala 2.11.4 is available at
[Sonatype](https://oss.sonatype.org/content/repositories/releases/com/scalamolecule/molecule_2.11/)
 so that you can add a dependency in your sbt file to `"com.marcgrue" % "molecule_2.11.4" % "0.2.0"`.

Since Molecule generates boilerplate code from your definitions it also needs to have the `DslBoilerplate.scala` file in your project folder. Please have a look at how the sbt build file
 of the Molecule project itself puts things together and simply copy that to your own project:

2. Setup your sbt build file [as in Molecule][moleculesbt]: 
    - Add library dependency `"com.marcgrue" % "molecule_2.11.4" % "0.2.0"`
    - List directories where you have your [definition file(s)][setup]
3. Define your domain schema in a [schema definition file][setup]
4. `sbt compile`
5. Import into your IDE
6. [Setup your database][setup]
7. [Populate your database][populate] with data
8. [Make molecule queries][tutorial]