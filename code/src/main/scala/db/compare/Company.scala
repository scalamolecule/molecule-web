package db.compare

import molecule.DomainStructure


object Company extends DomainStructure {

  trait Department {
    val name = oneString
  }

  trait Employee {
    val name       = oneString
    val department = one[Department]
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
    val employees = many[Employee]
  }

  trait EmployeeProject {
    val employee = one[Employee]
    val project  = one[Project]
    val role     = oneString
  }
}