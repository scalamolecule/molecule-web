package docs.query

import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import db.dataModel.dsl.Gaming.metadb.Gaming_h2
import docs.H2Tests
import molecule.db.h2.sync.*
import utest.*


object filters extends H2Tests {

  override lazy val tests = Tests {

    "Shared ops" - h2(Person_h2()) {
      Person.name.likes_?.insert(
        ("Ann", Some("sushi")),
        ("Ben", Some("pizza")),
        ("Gus", Some("pasta")),
        ("Jon", Some("pizza")),
        ("Liz", None),
      ).transact

      Person.name.a1.likes_?.query.i.get ==> List(
        ("Ann", Some("sushi")),
        ("Ben", Some("pizza")),
        ("Gus", Some("pasta")),
        ("Jon", Some("pizza")),
        ("Liz", None),
      )
      Person.name.a1.likes.query.i.get ==> List(
        ("Ann", "sushi"),
        ("Ben", "pizza"),
        ("Gus", "pasta"),
        ("Jon", "pizza"),
      )


      // Null
      Person.name.likes_().query.i.get ==> List("Liz")


      // Equality
      Person.name.a1.likes("pizza").query.i.get ==> List(
        ("Ben", "pizza"),
        ("Jon", "pizza"),
      )
      // tacit
      Person.name.a1.likes_("pizza").query.i.get ==> List(
        "Ben",
        "Jon",
      )
      // optional
      Person.name.a1.likes_?(Some("pizza")).query.i.get ==> List(
        ("Ben", Some("pizza")),
        ("Jon", Some("pizza")),
      )
      Person.name.a1.likes_?(Option.empty[String]).query.i.get ==> List(
        ("Liz", None),
      )


      // OR logic
      Person.name.a1.likes("pizza", "sushi").query.i.get ==> List(
        ("Ann", "sushi"),
        ("Ben", "pizza"),
        ("Jon", "pizza"),
      )
      Person.name.a1.likes(List("pizza", "sushi")).query.i.get ==> List(
        ("Ann", "sushi"),
        ("Ben", "pizza"),
        ("Jon", "pizza"),
      )
      // empty
      Person.name.likes(List.empty[String]).query.i.get ==> List()


      // Negation
      Person.name.a1.likes.not("pizza").query.i.get ==> List(
        ("Ann", "sushi"),
        ("Gus", "pasta"),
      )

      // NOR logic
      Person.name.a1.likes.not("pizza", "sushi").query.i.get ==> List(
        ("Gus", "pasta"),
      )
    }


    "Comparison" - h2(Person_h2()) {
      Person.name.age.likes.insert(
        ("Ann", 17, "sushi"),
        ("Ben", 18, "pizza"),
        ("Gus", 19, "pasta"),
        ("Jon", 20, "pizza"),
      ).transact

      Person.name.a1.age_.<(18).query.i.get ==> List("Ann")
      Person.name.a1.age_.<=(18).query.i.get ==> List("Ann", "Ben")
      Person.name.a1.age_.>(18).query.i.get ==> List("Gus", "Jon")
      Person.name.a1.age_.>=(18).query.i.get ==> List("Ben", "Gus", "Jon")

      Person.likes.<("pizza").query.i.get ==> List("pasta")
      Person.likes.<=("pizza").query.i.get ==> List("pasta", "pizza")
      Person.likes.>("pizza").query.i.get ==> List("sushi")
      Person.likes.>=("pizza").query.i.get ==> List("pizza", "sushi")
    }


    "String" - h2(Person_h2()) {
      Person.name.likes.insert(
        ("Ann", "sushi"),
        ("Ben", "pizza"),
        ("Gus", "pasta"),
        ("Jon", "pizza"),
      ).transact

      Person.likes.startsWith("p").query.i.get ==> List("pasta", "pizza")
      Person.likes.endsWith("a").query.i.get ==> List("pasta", "pizza")
      Person.likes.contains("s").query.i.get ==> List("pasta", "sushi")
      Person.likes.matches("^[r-z].*").query.i.get ==> List("sushi")
    }


    "Number" - h2(Person_h2()) {
      Person.age.insert(-4 to 4).transact

      Person.age.%(3, 0).query.i.get ==> List(-3, 0, 3)
      Person.age.%(3, 1).query.i.get ==> List(-4, -1, 1, 4)
      Person.age.%(3, 2).query.i.get ==> List(-2, 2)

      // Odd
      Person.age.%(2, 1).query.i.get ==> List(-3, -1, 1, 3)
      // Even
      Person.age.%(2, 0).query.i.get ==> List(-4, -2, 0, 2, 4)

      Person.age.odd.query.i.get ==> List(-3, -1, 1, 3)
      Person.age.even.query.i.get ==> List(-4, -2, 0, 2, 4)
    }

    "Number2" - h2(Gaming_h2()) {
      import db.dataModel.dsl.Gaming.*
      Gamer.rank.insert(-4 to 4).transact

      Gamer.rank.%(3, 0).query.i.get ==> List(-3, 0, 3)
      Gamer.rank.%(3, 1).query.i.get ==> List(-4, -1, 1, 4)
      Gamer.rank.%(3, 2).query.i.get ==> List(-2, 2)

      // Odd
      Gamer.rank.%(2, 1).query.i.get ==> List(-3, -1, 1, 3)
      // Even
      Gamer.rank.%(2, 0).query.i.get ==> List(-4, -2, 0, 2, 4)

      Gamer.rank.odd.query.i.get ==> List(-3, -1, 1, 3)
      Gamer.rank.even.query.i.get ==> List(-4, -2, 0, 2, 4)
    }


    "Set" - h2(Person_h2()) {
      Person.name.hobbies.insert(
        ("Bob", Set("golf", "stamps")),
        ("Liz", Set("golf")),
      ).transact

      Person.name.hobbies_.has("golf").query.i.get ==> List("Bob", "Liz")
      Person.name.hobbies_.has("stamps").query.i.get ==> List("Bob")
      Person.name.hobbies_.has("hiking").query.i.get ==> List()

      // "Has any of"
      Person.name.hobbies_.has("stamps", "hiking").query.i.get ==> List("Bob")
      // same as
      Person.name.hobbies_.has(Seq("stamps", "hiking")).query.i.get ==> List("Bob")
    }


    "Seq" - h2(Person_h2()) {
      Person.name.scores.insert(
        ("Bob", List(7, 6, 7, 4)),
        ("Liz", List(9, 3, 6)),
      ).transact

      Person.name.scores_.has(6).query.i.get ==> List("Bob", "Liz")
      Person.name.scores_.has(7).query.i.get ==> List("Bob")
      Person.name.scores_.has(8).query.i.get ==> List()

      // "Has any of"
      Person.name.scores_.has(1, 2, 3).query.i.get ==> List("Liz")
      // same as
      Person.name.scores_.has(Seq(1, 2, 3)).query.i.get ==> List("Liz")
    }


    "Map" - h2(Person_h2()) {
      Person.name.a1.age.likes.insert(
        ("Ann", 17, "sushi"),
        ("Ben", 18, "pizza"),
        ("Gus", 19, "pasta"),
        ("Jon", 20, "pizza"),
      ).transact

      Person.name.a1.age.likes.query.i.i.get ==> List(
        ("Ann", 17, "sushi"),
        ("Ben", 18, "pizza"),
        ("Gus", 19, "pasta"),
        ("Jon", 20, "pizza"),
      )

      Person.name.a1.age.<(18).query.i.get ==> List(
        ("Ann", 17),
      )
      Person.name.age.<=(18).query.i.get ==> List(
        ("Ann", 17),
        ("Ben", 18),
      )
      Person.name.age.>(18).query.i.get ==> List(
        ("Gus", 19),
        ("Jon", 20),
      )
      Person.name.age.>=(18).query.i.get ==> List(
        ("Ben", 18),
        ("Gus", 19),
        ("Jon", 20),
      )
    }
  }
}
