package db.compare

import molecule.DomainStructure


object Company extends DomainStructure {

  trait Department {
    val name = oneString
    // .Employees (plural of Employee)
  }

  trait Employee {
    val name       = oneString
    val department = manyToOne[Department]
    // .Projects via Assignment
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
  }

  trait Assignment extends Join {
    val employee = manyToOne[Employee]
    val project  = manyToOne[Project]
    val role     = oneString
  }
}