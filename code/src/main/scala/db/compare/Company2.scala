package db.compare

import molecule.DomainStructure


object Company2 extends DomainStructure {

  trait Employee {
    val name    = oneString
    val salary  = oneInt
    val project = manyToOne[Project]
  }

  trait Project {
    val name   = oneString
    val budget = oneInt
  }
}