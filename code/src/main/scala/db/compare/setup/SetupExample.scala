package db.compare.setup

import molecule.DomainStructure


// Generate Molecule boilerplate code by running `sbt moleculeGen`
object SetupExample extends DomainStructure {
  trait Project {
    val name   = oneString
    val budget = oneInt
  }
  trait Employee {
    val name    = oneString
    val salary  = oneInt
    val project = manyToOne[Project]
  }
}