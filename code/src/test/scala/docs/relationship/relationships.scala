package docs.relationship

import db.compare.tutorials.scalasql.dsl.World.metadb.World_h2
import db.dataModel.dsl.Person.metadb.Person_h2
import db.dataModel.dsl.Products.metadb.Products_h2
import db.dataModel.dsl.School.metadb.School_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object relationships extends H2Tests {

  override lazy val tests = Tests {

    "compare plain sql" - h2(Person_h2()) {
      import db.dataModel.dsl.Person.*

      Person.name("Bob").age(42)
        .Home.street("Main st. 17").zip(10240).Country.name("USA")
        ._Address.Stats.crimeRate("Low")
        ._Address._Person
        .Education.shortName("Harvard").State.abbr("MA")
        .save.transact

      Person.name("Liz").age(38).save.transact


      // Ref ==============================================

      Person.name.age.Home.street.zip.query.get.head ==>
        ("Bob", 42, "Main st. 17", 10240)

      Person.name.age.home.query.get.head ==>
        ("Bob", 42, 1L)

      Person.name.age.Home.id.street.zip.query.get.head ==>
        ("Bob", 42, 1L, "Main st. 17", 10240)

      Person.name.age.
        Home.street.zip.
        Country.name.query.get.head ==>
        ("Bob", 42, "Main st. 17", 10240, "USA")

      Person.name.age.
        Home.street.zip.
        Country.name_("USA").query.get.head ==>
        ("Bob", 42, "Main st. 17", 10240)
    }

    "persons" - h2(Person_h2()) {
      import db.dataModel.dsl.Person.*

      Person.name("Bob").age(42)
        .Home.street("Main st. 17").zip(10240).Country.name("USA")
        ._Address.Stats.crimeRate("Low")
        ._Address._Person
        .Education.shortName("Harvard").State.abbr("MA")
        .save.transact

      Person.name("Liz").age(38).save.transact


      // Ref ==============================================

      Person.name.age.Home.street.zip.query.get.head ==>
        ("Bob", 42, "Main st. 17", 10240)

      Person.name.age.home.query.get.head ==>
        ("Bob", 42, 1L)

      Person.name.age.Home.id.street.zip.query.get.head ==>
        ("Bob", 42, 1L, "Main st. 17", 10240)

      Person.name.age.
        Home.street.zip.
        Country.name.query.get.head ==>
        ("Bob", 42, "Main st. 17", 10240, "USA")


      // Backref ==============================================

      Person.name.age
        .Home.street.zip // branch 1
        ._Person // backref to Person
        .Education.shortName // branch 2: Education is a ref from Person
        .query.get.head ==>
        (
          "Bob", 42,
          "Main st. 17", 10240, // branch 1 data
          "Harvard" // branch 2 data
        )

      Person.name.age
        .Home.street.zip.Country.name // branch 1
        ._Address._Person // 2 backrefs to Person
        .Education.shortName.State.abbr // branch 2
        .query.get.head ==>
        (
          "Bob", 42,
          "Main st. 17", 10240, "USA", // branch 1 data
          "Harvard", "MA" // branch 2 data
        )

      Person.name.age
        .Home.street.zip.Country.name // branch 1
        ._Address // one backref to Address
        .Stats.crimeRate // branch 2
        .query.get.head ==>
        (
          "Bob", 42,
          "Main st. 17", 10240, "USA", // branch 1 data
          "Low" // branch 2 data
        )


      // Opt ref ==============================================

      // One optional related attribute
      Person.name.age.Home.?(Address.street).query.get ==> List(
        ("Bob", 42, Some("Main st. 17")), // Option[<value>]
        ("Liz", 38, None)
      )

      // Multiple optional related attributes
      Person.name.age.Home.?(Address.street.zip).query.get ==> List(
        ("Bob", 42, Some(("Main st. 17", 10240))), // Option[<tuple>]
        ("Liz", 38, None)
      )


      // Opt entity ==============================================

      Address.street("Lonely st. 1").save.transact

      Person.?(Person.name).Home.street.query.get ==> List(
        (None, "Lonely st. 1"),
        (Some("Bob"), "Main st. 17"),
      )

      Person.?(Person.name.age).Home.street.query.get ==> List(
        (None, "Lonely st. 1"),
        (Some(("Bob", 42)), "Main st. 17"),
      )


      // Mix

      // Mandatory ref before is allowed
      Person.name.age
        .Home.street.zip //                           Person --- Home
        ._Person.Education.?(University.shortName) // Person -?- Education
        .query.get ==> List(
        (
          "Bob", 42,
          "Main st. 17", 10240,
          Some("Harvard"),
        )
        // (Liz not included since home address is mandatory)
      )

      // Mandatory ref after not allowed
      intercept[ModelError] {
        Person.name.age
          .Home.?(Address.street.zip) // Person -?- Home
          .Education.shortName //        Person --- Education
          .query.get ==> List(
          (
            "Bob", 42,
            Some(("Main st. 17", 10240)),
            Some("Harvard"),
          ),
          (
            "Liz", 38,
            None,
            None
          )
        )
      }.msg ==> "Only further optional refs allowed after optional ref."

      // Adjacent
      Person.name.age
        .Home.?(Address.street.zip) //        Person -?- Home
        .Education.?(University.shortName) // Person -?- Education
        .query.get ==> List(
        (
          "Bob", 42,
          Some(("Main st. 17", 10240)),
          Some("Harvard"),
        ),
        (
          "Liz", 38,
          None,
          None
        )
      )

      // Inside
      Person.name.age
        .Home.?(Address.street.Country.name).query.i.get ==> List(
        ("Bob", 42, Some(("Main st. 17", "USA"))),
        ("Liz", 38, None)
      )

      // Nesting
      Person.name.age
        .Home.?(Address.street.zip
          .Country.?(Country.name)).query.i.get ==> List(

        ("Bob", 42, Some(
          ("Main st. 17", 10240, Some(
            "USA")))),

        ("Liz", 38, None)
      )
    }


    "right join" - h2(World_h2()) {
      import db.dataModel.dsl.Football.*

      Player.?(Player.name).Team.name.insert(
        (Some("Ben"), "Lions"),
        (None, "Lions"),
      )


    }

    // Nested ==============================================

    "school" - h2(School_h2()) {
      import db.dataModel.dsl.School.*

      val List(t1, t2) = Teacher.name.insert(
        "Maggie",
        "Ronnie",
      ).transact.ids

      val List(c1, c2, c3, c4) = Course.subject.insert(
        "Math",
        "Physics",
        "Biology",
        "English",
      ).transact.ids

      Attendance.teacher.course.insert(
        (t1, c1),
        (t1, c2),
        (t2, c3),
        (t2, c4),
      ).transact

      Teacher.name.a1.Courses.subject.a2.query.get ==> List(
        ("Maggie", "Math"),
        ("Maggie", "Physics"),
        ("Ronnie", "Biology"),
        ("Ronnie", "English"),
      )

      Teacher.name.a1.Courses.**(Course.subject.a1).query.get ==> List(
        ("Maggie", List("Math", "Physics")),
        ("Ronnie", List("Biology", "English")),
      )
    }


    "school2" - h2(School_h2()) {
      import db.dataModel.dsl.School.*

      val List(t1, t2, t3) = Teacher.name.insert(
        "Maggie",
        "Ronnie",
        "Veronica" // no students yet
      ).transact.ids

      val List(c1, c2, c3, c4, c5, c6) = Course.subject.insert(
        "Math",
        "Physics",
        "Biology",
        "English",
        "Chemistry", // no students yet
        "Drama", //     no students yet
      ).transact.ids

      val List(s1, s2, s3, s4, s5, s6) = Student.name.age.insert(
        ("Ann", 13),
        ("Ben", 14),
        ("Cat", 13),
        ("Dan", 14),
        ("Eva", 14),
        ("Fay", 15),
      ).transact.ids

      Attendance.teacher.course.student_?.insert(
        (t1, c1, Some(s1)),
        (t1, c1, Some(s2)),
        (t1, c1, Some(s3)),
        (t1, c2, Some(s4)),
        (t1, c2, Some(s5)),
        (t1, c2, Some(s6)),
        (t2, c3, Some(s1)),
        (t2, c3, Some(s2)),
        (t2, c3, Some(s3)),
        (t2, c4, Some(s4)),
        (t2, c4, Some(s5)),
        (t2, c4, Some(s6)),
        (t3, c5, None),
        (t3, c6, None),
      ).transact

      Teacher.name.a1
        .Courses.**(Course.subject.a1
          .Students.**(Student.name.a1.age)).query.get ==> List(
        ("Maggie", List(
          ("Math", List(
            ("Ann", 13),
            ("Ben", 14),
            ("Cat", 13),
          )),
          ("Physics", List(
            ("Dan", 14),
            ("Eva", 14),
            ("Fay", 15),
          )),
        )),
        ("Ronnie", List(
          ("Biology", List(
            ("Ann", 13),
            ("Ben", 14),
            ("Cat", 13),
          )),
          ("English", List(
            ("Dan", 14),
            ("Eva", 14),
            ("Fay", 15),
          )),
        )),
      )

      // Optional data

      Teacher.name.a1
        .Courses.**?(Course.subject.a1
          .Students.**?(Student.name.a1.age)).query.get ==> List(
        ("Maggie", List(
          ("Math", List(
            ("Ann", 13),
            ("Ben", 14),
            ("Cat", 13),
          )),
          ("Physics", List(
            ("Dan", 14),
            ("Eva", 14),
            ("Fay", 15),
          )),
        )),
        ("Ronnie", List(
          ("Biology", List(
            ("Ann", 13),
            ("Ben", 14),
            ("Cat", 13),
          )),
          ("English", List(
            ("Dan", 14),
            ("Eva", 14),
            ("Fay", 15),
          )),
        )),
        ("Veronica", List(
          ("Chemistry", List()), // no students yet
          ("Drama", List()), //     no students yet
        )),
      )
    }
  }
}