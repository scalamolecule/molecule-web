package db.compare

import molecule.DomainStructure


object Company extends DomainStructure {

  trait Department {
    val name = oneString
  }

  trait Employee {
    val name       = oneString
    val department = manyToOne[Department]
  }

  trait Project {
    val name   = oneString
    val budget = oneInt
  }

  trait Assignment extends Join {
    val employee = manyToOne[Employee]
    val project  = manyToOne[Project]
    val role     = oneString
  }
}