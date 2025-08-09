package db.relationships

import db.relationships.many2many_no_properties.Employee
import molecule.DomainStructure

object many2many_with_properties extends DomainStructure {

  trait Employee {
    val name     = oneString
    val projects = one[EmployeeProject].employee // no underscore
  }

  trait Project {
    val name      = oneString
    val employees = one[EmployeeProject].project // also no underscore
  }

  // User has to define join table
  trait EmployeeProject {
    val employee = one[Employee]
    val project  = one[Project]

    // property of a relationship
    val role = oneString
  }


//  Employee_projects_Project.employee_id.role.project_id.insert(
//    (bob, "lead", scala),
//    (bob, "engineer", java),
//    (liz, "manager", scala)
//  ).transact
//
//
//  // Join property access
//  Employee.name.Projects_.role_("lead").Project.name.query.get ==> List(
//    ("Bob", "Scala")
//  )
//
//  // nested without join property access
//  Employee.name.Projects.*(Project.name).query.get ==< List(
//    ("Bob", List("Scala", "Java")),
//    ("Liz", List("Scala"))
//  )
//  // or flat
//  Employee.name.Projects.name.query.get ==< List(
//    ("Bob", "Scala"),
//    ("Bob", "Java"),
//    ("Liz", "Scala")
//  )

}