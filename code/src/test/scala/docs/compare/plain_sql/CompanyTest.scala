package docs.compare.plain_sql

import db.compare.dsl.Company.*
import db.compare.dsl.Company.metadb.Company_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.compliance.domains.dsl.Types.Entity
import molecule.db.h2.sync.*
import utest.*


object CompanyTest extends H2Tests {

  override lazy val tests = Tests {

    "multiple mutations in single transaction" - h2(Company_h2()) {
      transact(
        Project.name("Project X").budget(100000).save,
        Employee.name("Alice").salary(80000).project(p1).save,
        Employee.name("Bob").salary(90000).project(p1).save
      )

      unitOfWork {
        val p1 = Project.name("Project X").budget(100000).save.transact.id

        Employee.name("Alice").salary(80000).project(p1).save.transact
        Employee.name("Bob").salary(90000).project(p1).save.transact
      }
    }

    "company" - h2(Company_h2()) {

      val List(d1, d2, d3) = Department.name.insert("Development", "Design", "Marketing").transact.ids

      val List(e1, e2, e3, e4, e5, e6, e7, e8) =
        Employee.name.department.insert(
          ("Alice", d1),
          ("Bob", d1),
          ("Carol", d1),
          ("Diana", d1),
          ("Eve", d2),
          ("Frank", d2),
          ("Grace", d3),
          ("Boss", d3),
        ).transact.ids

      val List(p1, p2, p3) = Project.name.budget.insert(
        ("BigProject", 2000000),
        ("MediumProject", 1200000),
        ("SmallProject", 400000),
      ).transact.ids

      Assignment.employee.project.role.insert(
        // Big project: 4 employees  (budget > 1M)
        (e1, p1, "Dev"), // Alice on BigProject
        (e2, p1, "Dev"), // Bob
        (e3, p1, "Lead"), // Carol
        (e4, p1, "Dev"), // Diana

        // Medium project: 2 employees, both on MediumProject (budget > 1M)
        (e1, p2, "Lead"), // Alice on MediumProject
        (e2, p2, "Dev"), // Bob
        (e5, p2, "Designer"), // Eve
        (e6, p2, "Designer"), // Frank

        // Small project: 1 employee (budget < 1M)
        (e7, p3, "Marketer"),
        // Boss is not assigned to a project
      ).transact

      // Departments and number of employees
      Department.name.Employees.id(countDistinct).d1
        .query.get ==> List(("Development", 4), ("Design", 2), ("Marketing", 2))

      // Departments and number of employees working on projects
      Department.name.Employees.id(countDistinct).d1.Assignments.project_
        .query.get ==> List(("Development", 4), ("Design", 2), ("Marketing", 1)) // Boss not working on a specific project

      // Departments and number of developers (no boss) working on projects
      Department.name.Employees.id(countDistinct).d1.Assignments.role_("Dev")
        .query.get ==> List(("Development", 3))

      // Departments and number of employees working on projects with a budget over one million
      Department.name.Employees.id(countDistinct).d1.Projects.budget_.>(1000000)
        .query.get ==> List(("Development", 4), ("Design", 2))

      // Departments and number of employees working on projects with more than 2 developers and a budget over one million
      // Departments with more than 2 employees assigned to projects with a budget exceeding 1M
      Department.name.Employees.id(countDistinct).>(2).d1.Projects.budget_.>(1000000)
        .query.get ==> List(("Development", 4))

      // Departments and number of developers working on projects with more than 2 developers and a budget over one million
      // Using attribute `role` of `Assignment` join table
      Department.name.Employees.id(countDistinct).>(2).d1.Assignments.role_("Dev").Project.budget_.>(1000000)
        .query.get ==> List(("Development", 3))

      // Same in multiple lines. Might be easier to read
      Department.name
        .Employees.id(countDistinct).>(2).d1
        .Assignments.role_("Dev")
        .Project.budget_.>(1000000)
        .query.get ==> List(("Development", 3))

      // SQL equivalent!
      rawQuery(
        """SELECT
          |  Department.name,
          |  COUNT(DISTINCT Employee.id) Employee_id_count
          |FROM Department
          |  INNER JOIN Employee   ON Department.id = Employee.department
          |  INNER JOIN Assignment ON Employee.id = Assignment.employee
          |  INNER JOIN Project    ON Assignment.project = Project.id
          |WHERE
          |  Department.name IS NOT NULL AND
          |  Assignment.role = 'Dev' AND
          |  Project.budget  > 1000000
          |GROUP BY Department.name
          |HAVING COUNT(DISTINCT Employee.id) > 2
          |ORDER BY Employee_id_count DESC;
          |""".stripMargin
      ) ==> List(List("Development", 3))


      // Without bidirectional relationships we did this before which was backwards:
      Assignment
        .Employee.id(countDistinct).>(2).d1.Department.name._Employee._Assignment
        .Project.budget_.>(1000000)
        .query.get ==> List((4, "Development"))




      // Many-to-many join
      Employee.name.a1.Projects.**(Project.name).query.get ==> List(
        ("Alice", List("BigProject", "MediumProject")),
        ("Bob", List("BigProject", "MediumProject")),
        ("Carol", List("BigProject")),
        ("Diana", List("BigProject")),
        ("Eve", List("MediumProject")),
        ("Frank", List("MediumProject")),
        ("Grace", List("SmallProject")),
      )
      // And the other way around
      Project.name.a1.Employees.**(Employee.name).query.get ==> List(
        ("BigProject", List("Alice", "Bob", "Carol", "Diana")),
        ("MediumProject", List("Alice", "Bob", "Eve", "Frank")),
        ("SmallProject", List("Grace")),
      )
      Project.name.a1.Employees.**(Employee.name.Department.name_.startsWith("De")).query.get ==> List(
        ("BigProject", List("Alice", "Bob", "Carol", "Diana")),
        ("MediumProject", List("Alice", "Bob", "Eve", "Frank")),
      )

      // Same as going through the `Assignment` join table
      Employee.name.a1.Assignments.*(Assignment.Project.name).query.get ==> List(
        ("Alice", List("BigProject", "MediumProject")),
        ("Bob", List("BigProject", "MediumProject")),
        ("Carol", List("BigProject")),
        ("Diana", List("BigProject")),
        ("Eve", List("MediumProject")),
        ("Frank", List("MediumProject")),
        ("Grace", List("SmallProject")),
      )
      Project.name.a1.Assignments.*(Assignment.Employee.name).query.get ==> List(
        ("BigProject", List("Alice", "Bob", "Carol", "Diana")),
        ("MediumProject", List("Alice", "Bob", "Eve", "Frank")),
        ("SmallProject", List("Grace")),
      )



      // Combining one-to-many and many-to-many joins
      Department.name.a1.Employees.*( // one-many, one Department to many Employees
        Employee.name.a1.Projects.**( // many-many, many Employees to many Projects
          Project.name.a1.budget
        )
      ).query.i.get ==> List(
        ("Design", List(
          ("Eve", List(
            ("MediumProject", 1200000))),
          ("Frank", List(
            ("MediumProject", 1200000))))),
        ("Development", List(
          ("Alice", List(
            ("BigProject", 2000000),
            ("MediumProject", 1200000))),
          ("Bob", List(
            ("BigProject", 2000000),
            ("MediumProject", 1200000))),
          ("Carol", List(
            ("BigProject", 2000000))),
          ("Diana", List(
            ("BigProject", 2000000))))),
        ("Marketing", List(
          ("Grace", List(
            ("SmallProject", 400000)))))
      )

      Employee.name.a1.Projects.**(Project.name.budget).insert(
        ("John", List(("OtherProject", 100000)))
      ).transact


      Project.name.a1.Assignments.*(Assignment.role).query.get ==> List(
        ("BigProject", List("Dev", "Lead")),
        ("MediumProject", List("Designer", "Dev", "Lead")),
        ("SmallProject", List("Marketer"))
      )

      Employee.name.a1.Assignments.*(Assignment.role).query.get ==> List(
        ("Alice", List("Dev", "Lead")),
        ("Bob", List("Dev")),
        ("Carol", List("Lead")),
        ("Diana", List("Dev")),
        ("Eve", List("Designer")),
        ("Frank", List("Designer")),
        ("Grace", List("Marketer"))
      )

      Employee.name.a1.Assignments.*(Assignment.role_("Dev").Project.name).query.get ==> List(
        ("Alice", List("BigProject")),
        ("Bob", List("BigProject", "MediumProject")),
        ("Diana", List("BigProject"))
      )

      Project.name.Assignments.*(Assignment.role.Employee.name.a1).query.get ==> List(
        ("BigProject", List(
          ("Dev", "Alice"),
          ("Dev", "Bob"),
          ("Lead", "Carol"),
          ("Dev", "Diana"),
        )),
        ("MediumProject", List(
          ("Lead", "Alice"),
          ("Dev", "Bob"),
          ("Designer", "Eve"),
          ("Designer", "Frank"),
        )),
        ("SmallProject", List(
          ("Marketer", "Grace")
        )),
      )

      Assignment.role_("Designer").Employee.name.query.get ==> List("Eve", "Frank")
    }
  }
}