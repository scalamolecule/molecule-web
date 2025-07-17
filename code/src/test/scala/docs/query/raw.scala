package docs.query

import db.dataModel.dsl.Person._
import db.dataModel.dsl.Person.metadb.Person_MetaDb_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object raw extends H2Tests {

  override lazy val tests = Tests {

    "refs" - h2(Person_MetaDb_h2()) { implicit conn =>

      // Ref ==============================================
      Person.name.age.insert(
        ("Bob", 42),
        ("Liz", 38),
      ).transact


      Person.name.age.query.inspect

      Person.name.age.query.i.get ==> List(
        ("Bob", 42),
        ("Liz", 38),
      )

      val rawResult: List[List[Any]] = rawQuery(
        """SELECT DISTINCT
          |  Person.name,
          |  Person.age
          |FROM Person
          |WHERE
          |  Person.name IS NOT NULL AND
          |  Person.age  IS NOT NULL;""".stripMargin, true
      )

      rawResult ==> List(
        List("Bob", 42),
        List("Liz", 38)
      )


    }
  }
}

