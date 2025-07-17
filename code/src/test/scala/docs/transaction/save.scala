package docs.transaction

import db.dataModel.dsl.Person._
import db.dataModel.dsl.Person.metadb.Person_MetaDb_h2

import docs.H2Tests
import molecule.db.h2.sync._
import utest._
import scala.collection.immutable.{ListSet, TreeMap, TreeSet, VectorMap}


object save extends H2Tests {

  override lazy val tests = Tests {

    "mandatory" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name("Bob").age(42).save.transact
      Person.name.age.query.get.head ==> ("Bob", 42)
    }

    "raw" - h2(Person_MetaDb_h2()) { implicit conn =>

      rawTransact(
        """INSERT INTO Person (
          |  name,
          |  age
          |) VALUES ('Bob', 42)""".stripMargin
      )

      Person.name.age.query.get.head ==> ("Bob", 42)
    }


    "optional" - h2(Person_MetaDb_h2()) { implicit conn =>
      val optName = Some("Bob") // or None
      val optAge  = Some(42) // or None
      Person.name_?(optName).age_?(optAge).save.transact
      Person.name.age.query.get.head ==> ("Bob", 42)
    }


    "Set" - h2(Person_MetaDb_h2()) { implicit conn =>
      // Main collection type
      Person.hobbies(Set("stamps", "trains")).save.transact

      // Collection subtypes
      Person.hobbies(ListSet("stamps", "trains")).save.transact
      Person.hobbies(TreeSet("stamps", "trains")).save.transact

      Person.hobbies.query.get.head ==> Set("stamps", "trains")
    }

    "Seq" - h2(Person_MetaDb_h2()) { implicit conn =>
      // Main collection type
      Person.scores(Seq(1, 2, 3)).save.transact

      // Collection subtypes
      Person.scores(List(1, 2, 3)).save.transact
      Person.scores(Vector(1, 2, 3)).save.transact

      Person.scores.query.get.head ==> Seq(1, 2, 3)
    }

    "Map" - h2(Person_MetaDb_h2()) { implicit conn =>
      // Main collection type
      Person.langNames(Map("en" -> "Bob")).save.transact

      // Collection subtypes
      Person.langNames(TreeMap("en" -> "Bob")).save.transact
      Person.langNames(VectorMap("en" -> "Bob")).save.transact

      Person.langNames.query.get.head ==> Map("en" -> "Bob")
    }


    "ref" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name("Bob").age(42)
        .Home.street("Main st. 17").save.transact

      Person.name.age
        .Home.street.query.get.head ==>
        ("Bob", 42, "Main st. 17")
    }


    "refs" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name("Bob").age(42)
        .Home.street("Main st. 17")
        .Country.name("USA")
        .save.transact

      Person.name.age
        .Home.street
        .Country.name.query.get.head ==>
        ("Bob", 42, "Main st. 17", "USA")
    }


    "backref" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name("Bob").age(42)
        .Home.street("Main st. 17")._Person
        .Education.shortName("Harvard")
        .save.transact

      Person.name.age
        .Home.street._Person
        .Education.shortName.query.get.head ==>
        ("Bob", 42, "Main st. 17", "Harvard")
    }


    "ref attr" - h2(Person_MetaDb_h2()) { implicit conn =>
      Country.name("USA").save.transact
      val usaId = Country.id.name_("USA").query.get.head

      Person.name("Bob").age(42)
        .Home.street("Main st. 17")
        .country(usaId) // save country id with ref attr
        .save.transact

      Person.name.age
        .Home.street
        .Country.name.query.get.head ==>
        ("Bob", 42, "Main st. 17", "USA")
    }
  }
}
