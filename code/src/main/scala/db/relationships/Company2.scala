package db.relationships

import molecule.DomainStructure


object Company2 extends DomainStructure {

  trait Department {
    val name = oneString
    val employees = many[Employee]
  }

  trait Employee {
    val name       = oneString
    val department = one[Department]
    val projects   = many[EmployeeProject]
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
    val employees = many[EmployeeProject]
  }

  trait EmployeeProject {
    val employee = one[Employee]
    val project  = one[Project]
  }
}