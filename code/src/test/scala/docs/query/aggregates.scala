package docs.query

import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import molecule.db.h2.sync.*
import utest.*


object aggregates extends H2Tests {

  override lazy val tests = Tests {

    "aggregates" - h2(Person_h2()) {

      Person.firstName.lastName.age.insert(
        ("Bob", "Johnson", 23),
        ("Liz", "Benson", 24),
        ("Liz", "Murray", 24),
        ("Liz", "Taylor", 25)
      ).transact

      Person.age(min).query.get.head ==> 23
      Person.age(max).query.get.head ==> 25

      Person.lastName(min).query.get.head ==> "Benson"
      Person.lastName(max).query.get.head ==> "Taylor"


      // 2 lowest/highest ages
      Person.age(min(2)).query.get.head ==> Set(23, 24)
      Person.age(max(2)).query.get.head ==> Set(24, 25)

      // 2 firstly/lastly ordered lastNames
      Person.lastName(min(2)).query.get.head ==> Set("Benson", "Johnson")
      Person.lastName(max(2)).query.get.head ==> Set("Murray", "Taylor")


      Person.age(count).query.get ==> List(4)
      Person.firstName.age(count).query.get ==> List(
        ("Bob", 1),
        ("Liz", 3), // 24, 24, 25
      )
      Person.firstName.age(count).>(1).query.get ==> List(
        ("Liz", 3), // 24, 24, 25
      )
      Person.age.age(count).query.get ==> List(
        (23, 1),
        (24, 2),
        (25, 1),
      )


      Person.age(countDistinct).query.get ==> List(3)
      Person.firstName.age(countDistinct).query.get ==> List(
        ("Bob", 1),
        ("Liz", 2), // 24, 25
      )
      Person.firstName.age(countDistinct).>(1).query.get ==> List(
        ("Liz", 2), // 24, 25
      )
      Person.age.age(countDistinct).query.get ==> List(
        (23, 1),
        (24, 1), // 24 and 24 coalesced to one distinct value
        (25, 1),
      )


      // Distinct ages by firstName
      Person.firstName.age(distinct).query.get ==> List(
        ("Bob", Set(23)),
        ("Liz", Set(24, 25))
      )

      // Distinct ages
      Person.age(distinct).query.get ==> List(
        Set(23, 24, 25)
      )


      Person.firstName.age(sum).query.get ==> List(
        ("Bob", 23),
        ("Liz", 24 + 24 + 25),
      )
      Person.age(sum).query.get.head ==> (23 + 24 + 24 + 25)


      Person.firstName.age(avg).query.i.get ==> List(
        ("Bob", 23),
        ("Liz", (24 + 24 + 25) / 3.0), // compare double value
      )

      Person.age(avg).query.get.head ==> (23 + 24 + 24 + 25) / 4.0


      Person.firstName.age(median).query.get ==> List(
        ("Bob", 23),
        ("Liz", 24),
      )
      Person.age(median).query.get.head ==> 24
    }
  }
}
