package docs.query

import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object attributes extends H2Tests {

  override lazy val tests = Tests {

    "refs" - h2(Person_h2()) {

      // Ref ==============================================
      Person.name("Bob").age(42).save.transact
      Person.name("Liz").save.transact


      Person.name.age.query.get ==> List(
        ("Bob", 42),
      )

      Person.name.age_.query.get ==> List(
        "Bob"
      )

      Person.name.age_?.query.get ==> List(
        ("Bob", Some(42)),
        ("Liz", None),
      )
    }
  }
}
