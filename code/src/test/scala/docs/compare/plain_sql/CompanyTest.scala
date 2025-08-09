package docs.compare.plain_sql

import db.compare.dsl.Company.*
import db.compare.dsl.Company.metadb.Company_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object CompanyTest extends H2Tests {

  override lazy val tests = Tests {

    "company" - h2(Company_h2()) {

      Department.name.insert("Development", "Design", "Marketing").transact

      Employee.name.department.insert(
        ("Alice", 1),
        ("Bob", 1),
        ("Carol", 1),
        ("Diana", 1),
        ("Eve", 2),
        ("Frank", 2),
        ("Grace", 3),
      ).transact

      Project.name.budget.insert(
        ("BigProj", 2000000),
        ("SmallProj", 400000),
        ("MediumProj", 1200000),
        ("LowBudget", 800000),
      ).transact

      EmployeeProject.employee.project.role.insert(
        (1, 1, "Dev"), // Alice on BigProj
        (2, 1, "Dev"), // Bob
        (3, 1, "Dev"), // Carol
        (4, 1, "Dev"), // Diana
        (1, 3, "Lead"), // Alice on MediumProj
        (2, 3, "Dev"), // Bob

        // Design: Only 2 employees, both on MediumProj (>1M)
        (5, 3, "Designer"), // Eve
        (6, 3, "Designer"), // Frank

        //Marketing: 1 employee on SmallProj (budget < 1M)
        (7, 2, "Marketer"),
      ).transact


      // SQL
      rawQuery(
        """SELECT d.name AS department
          |  FROM Department d
          |  JOIN Employee e             ON e.department = d.id
          |  JOIN EmployeeProject ep     ON e.id = ep.employee
          |  JOIN Project p              ON ep.project = p.id
          |  WHERE p.budget > 1000000
          |  GROUP BY d.id, d.name
          |  HAVING COUNT(DISTINCT e.id) > 2
          |  ORDER BY COUNT(DISTINCT e.id) DESC
          |""".stripMargin
      ) ==> List(List("Development"))

      rawQuery(
        """SELECT d.name AS department, COUNT(DISTINCT e.id) AS num_employees
          |  FROM Department d
          |  JOIN Employee e             ON e.department = d.id
          |  JOIN EmployeeProject ep     ON e.id = ep.employee
          |  JOIN Project p              ON ep.project = p.id
          |  WHERE p.budget > 1000000
          |  GROUP BY d.id, d.name
          |  HAVING num_employees > 2
          |  ORDER BY num_employees DESC
          |""".stripMargin
      ) ==> List(List("Development", 4))

      // Molecule
      EmployeeProject
        .Employee.id(countDistinct).>(2).d1.Department.name._Employee._EmployeeProject
        .Project.budget_.>(1000000)
        .query.i.get ==> List((4, "Development"))

      // This should work!
//      Department.name.Employees.id(countDistinct).>(2).d1.Projects.budget_.>(1000000)
//        .query.get.head ==> (4, "Development")
//
//      Department.name
//        .Employees.id_(countDistinct).>(2).d1
//        .Projects.budget_.>(1000000)
//        .query.get.head ==> "Development"
//
//      Department.name
//        .Employees.id_(countDistinct).>(2).d1
//        .Projects.budget_.>(1000000)
//        .query.get.head ==> "Development"
//
//
//      // -> card-many Project_ to access EmployeeProject join table properties
//      Employee.name.Projects_.role // flat join property
//      Employee.name.Projects_.*(EmployeeProject.role) // nested join property
//      Employee.name.Projects_.role.Project.budget // flat join property + target attribute
//      Employee.name.Projects_.*(EmployeeProject.role.Project.budget) // nested join property + flat target attribute from there
//
//      // -> card-many Project to access Project - like card-many now:
//      Employee.name.Projects.budget
//      Employee.name.Projects.*(Project.budget)
//
//
//
//      // Inserting ids in a join table - we can't do that now! :-O
//      EmployeeProject.employee.project.insert(
//        (1, 1),
//        (1, 2), // Possibly with a role too..
//      )
//
//      // We can only insert fixed new values on both sides
//      Employee.name.Projects.*(Project.name).insert(List(
//        ("Alice", "BigProj"),
//        ("Bob", "BigProj"),
//        ("Carol", "BigProj"),
//        ("Diana", "BigProj"),
//        ("Eve", "BigProj"),
//        ("Frank", "BigProj"),
//        ("Grace", "BigProj"),
//        ("Alice", "MediumProj"),
//        ("Bob", "MediumProj"),
//        ("Carol", "MediumProj"),
//      ))
      // But that creates redundant projects!!
      // We want to be able to add the joins _after_ both sides have been inserted.



//      Department.name.Employees.*(Employee.id(countDistinct).>(2).d1.Projects.budget_.>(1000000))
//
//      Department.name.EmployeeProject.*(Employee.id(countDistinct).>(2).d1.Projects.budget_.>(1000000))
//
//      Department.name.Employees.*(Employee.name)

    }
  }
}