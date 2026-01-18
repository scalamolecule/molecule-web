package docs.compare.plain_sql

import db.compare.dsl.Company2.*
import db.compare.dsl.Company2.metadb.Company2_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object CompanyTest2 extends H2Tests {

  override lazy val tests = Tests {


    "unitOfWork" - h2(Company2_h2()) {
      unitOfWork {
        val p1 = Project.name("Project X").budget(100000).save.transact.id
        Employee.name.salary.project.insert(
          ("Alice", 80000, p1),
          ("Bob", 90000, p1),
        ).transact
      }
    }

    "self-aggregate" - h2(Company2_h2()) {
      Employee.name.salary.insert(
        ("Bob", 50000),
        ("Eva", 60000),
        ("Liz", 70000),
      ).transact

      Employee.salary(avg).query.get ==> List(60000)

      Employee.name.salary.>(Employee.salary(avg)).query.get ==> List(
        ("Liz", 70000, 60000),
      )
      Employee.name.salary.>(Employee.salary_(avg)).query.i.get ==> List(
        ("Liz", 70000),
      )

      Employee.name.salary.join(Employee.salary(avg)).query.i.get ==> List(
        ("Bob", 50000, 60000),
        ("Eva", 60000, 60000),
        ("Liz", 70000, 60000),
      )
    }
  }
}