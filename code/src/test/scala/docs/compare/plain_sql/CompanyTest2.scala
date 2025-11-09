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

  }
}