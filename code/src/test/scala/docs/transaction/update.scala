package docs.transaction

import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync._
import utest._


object update extends H2Tests {

  override lazy val tests = Tests {

    "id core" - h2(Person_h2()) {
      Person.name("Bob").age(42).Home.street("Main st. 1").save.inspect
      Person.name("Bob").age(42).save.transact

      Person(1).age(43).update.transact
    }

    "id simple" - h2(Person_h2()) {
      val bobId = Person.name("Bob").age(42).save.transact.id
      Person.age.query.get.head ==> 42

      // Bob is now 43
      Person(bobId).age(43).update.i.transact
      Person.age.query.get.head ==> 43
    }

    "id" - h2(Person_h2()) {
      Person.name("Bob").age(42).save.transact

      // Current data: Bob is 42
      Person.age.query.get.head ==> 42

      // Retrieve id to be updated
      val bobId = Person.id.name_("Bob").query.get.head

      // Update Bob's age to 43 using his id
      Person(bobId).age(43).update.transact

      // Bob is now 43
      Person.age.query.get.head ==> 43


      """SELECT
        |  Person.name,
        |  Person.age,
        |  Address.street
        |FROM Person
        |  INNER JOIN Address
        |    ON Person.address = Address.id
        |WHERE
        |  Person.name IS NOT NULL AND
        |  Person.age  IS NOT NULL;""".stripMargin
    }


    "ids" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Bob", 42),
        ("Joe", 42),
      ).transact

      Person.name.age.query.get ==> List(
        ("Bob", 42),
        ("Joe", 42),
      )

      val List(bob, ben) = Person.id.name_.query.get

      // Update Bob and Joe
      Person(bob, ben).age(43).update.transact

      // Bob and Joe are now 43
      Person.name.age.query.get ==> List(
        ("Bob", 43),
        ("Joe", 43),
      )
    }


    "ids List" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Bob", 42),
        ("Joe", 42),
      ).transact

      Person.name.age.query.get ==> List(
        ("Bob", 42),
        ("Joe", 42),
      )

      val ids = Person.id.name_.query.get
      Person(ids).age(43).update.transact

      Person.name.age.query.get ==> List(
        ("Bob", 43),
        ("Joe", 43),
      )
    }


    "not null" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for anyone with a firstName
      Person.age(43).firstName_.update.transact

      // All but Wilson now 43 years old
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 43), // updated
        (Some("Bob"), "Monroe", 43), // updated
        (Some("Bob"), "Sponge", 43), // updated
        (None, "Wilson", 72), // Not updated (firstName not asserted)
      )
    }

    "null" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for anyone _without_ a firstName
      Person.age(43).firstName_().update.transact

      // All but Wilson now 43 years old
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 43), // updated (firstName is null)
      )
    }


    "equality" - h2(Person_h2()) {
      Person.firstName.lastName.age.insert(
        ("Liz", "Carson", 27),
        ("Bob", "Monroe", 42),
        ("Bob", "Sponge", 35),
      ).transact

      // For all with firstName "Bob", set age to 43
      Person.firstName_("Bob").age(43).update.transact

      // All "Bob" ages set to 43
      Person.firstName.lastName.a1.age.query.get ==> List(
        ("Liz", "Carson", 27),
        ("Bob", "Monroe", 43), // updated
        ("Bob", "Sponge", 43), // updated
      )
    }


    "equality same attr" - h2(Person_h2()) {
      Person.firstName.lastName.age.insert(
        ("Liz", "Carson", 27),
        ("Bob", "Monroe", 42),
        ("Bob", "Sponge", 35),
      ).transact

      // For all aged 42, set age to 43
      Person.age_(42).age(43).update.transact

      // All aged 42 are now 43
      Person.firstName.lastName.a1.age.query.get ==> List(
        ("Liz", "Carson", 27),
        ("Bob", "Monroe", 43), // updated
        ("Bob", "Sponge", 35),
      )
    }

    "equality multiple values for card-one attr" - h2(Person_h2()) {
      intercept[ModelError](
        Person.firstName_("Bob").age(43, 44).update.transact
      ).msg ==> "Can only update one value for attribute `Person.age`. Found: 43, 44"
    }


    "OR logic" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for lastNames "Monroe" or "Wilson"
      Person.age(43).lastName_("Monroe", "Wilson").update.transact

      // A List of values can be used too
      // Person.age(43).lastName_(List("Monroe", "Wilson")).update.transact

      // All with last name Monroe OR Wilson now 43 years old
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 43), // updated
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 43), // updated
      )
    }


    "OR logic dynamic" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for lastNames "Monroe" or "Wilson"
      Person.age(43).lastName_(List("Monroe", "Wilson")).update.transact

      // All but Wilson now 43 years old
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 43), // updated
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 43), // updated
      )
    }


    "Negation" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for all having a first name that is not "Bob"
      Person.age(43).firstName_.not("Bob").update.transact

      // Liz now 43
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 43), // updated
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72), // not update since firstName not asserted
      )
    }

    "NOR logic" - h2(Person_h2()) {
      Person.firstName_?.lastName.age.insert(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 35),
        (None, "Wilson", 72),
      ).transact

      // Update age to 43 for last name NEITHER "Carson" NOR "Monroe"
      Person.age(43).lastName_.not("Carson", "Monroe").update.transact

      // Sponge and Wilson now 43
      Person.firstName_?.lastName.a1.age.query.get ==> List(
        (Some("Liz"), "Carson", 27),
        (Some("Bob"), "Monroe", 42),
        (Some("Bob"), "Sponge", 43), // updated
        (None, "Wilson", 43), // updated
      )
    }


    "comparison <" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update likes of persons with age < 35
      Person.age_.<(35).likes("Beef").update.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Beef")), // updated
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None), // not update since not asserted
      )
    }

    "comparison <=" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update likes of persons with age <= 35
      Person.age_.<=(35).likes("Beef").update.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Beef")), // updated
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Beef")), // updated
        ("Wilson", 72, None), // not update since not asserted
      )
    }

    "comparison >" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update likes of persons with age > 35
      Person.age_.>(35).likes("Beef").update.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Beef")), // updated
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None), // not update since not asserted
      )
    }

    "comparison >=" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update likes of persons with age >= 35
      Person.age_.>=(35).likes("Beef").update.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Beef")), // updated
        ("Sponge", 35, Some("Beef")), // updated
        ("Wilson", 72, None), // not update since not asserted
      )
    }


    "Multiple filters" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update likes of persons with age between 30 and 45
      Person.age_.>(30).age_.<(45).likes("Beef").update.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Beef")), // updated
        ("Sponge", 35, Some("Beef")), // updated
        ("Wilson", 72, None),
      )
    }


    "upsert" - h2(Person_h2()) {
      Person.lastName.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Upsert likes of persons with age > 35
      Person.age_.>(35).likes("Beef").upsert.transact

      Person.lastName.a1.age.likes_?.query.get ==> List(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Beef")), // updated
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, Some("Beef")), // inserted!
      )
    }


    "update all" - h2(Person_h2()) {
      Person.name.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update age and likes of all with a name
      Person.name_.age(43).likes("Beef").update.transact

      // Wilson entity not updated since likes not asserted
      Person.name.a1.age.likes_?.query.get ==> List(
        ("Carson", 43, Some("Beef")),
        ("Monroe", 43, Some("Beef")),
        ("Sponge", 43, Some("Beef")),
        ("Wilson", 72, None), // Neither age nor likes updated
      )
    }

    "upsert all" - h2(Person_h2()) {
      Person.name.age.likes_?.insert(
        ("Carson", 27, Some("Pasta")),
        ("Monroe", 42, Some("Pizza")),
        ("Sponge", 35, Some("Apples")),
        ("Wilson", 72, None),
      ).transact

      // Update age and likes of all with a name
      Person.name_.age(43).likes("Beef").upsert.transact

      // Values updated/inserted for all entities
      Person.name.a1.age.likes_?.query.get ==> List(
        ("Carson", 43, Some("Beef")),
        ("Monroe", 43, Some("Beef")),
        ("Sponge", 43, Some("Beef")),
        ("Wilson", 43, Some("Beef")), // age updated, likes inserted
      )
    }


    "delete attr" - h2(Person_h2()) {
      Person.name("Bob").age(42).save.transact

      // Retrieve id to be updated
      val bobId = Person.id.name_("Bob").query.get.head

      Person.name.age_?.query.get.head ==> ("Bob", Some(42))

      // Delete Bob's age
      Person(bobId).age().update.transact

      // Bob now has no age
      Person.name.age_?.query.get.head ==> ("Bob", None)
    }


    "Collection replace" - h2(Person_h2()) {
      Person.name("Bob")
        .hobbies(Set("stamps", "trains"))
        .scores(Seq(1, 2, 3))
        .langNames(Map("en" -> "Hello", "es" -> "Hola"))
        .save.transact

      Person.hobbies.scores.langNames.query.get.head ==> (
        Set("stamps", "trains"),
        Seq(1, 2, 3),
        Map("en" -> "Hello", "es" -> "Hola"),
      )

      // Replace collections
      Person.name_
        .hobbies(Set("sport", "opera"))
        .scores(Seq(4, 5, 6))
        .langNames(Map("de" -> "Hallo"))
        .update.transact

      // All collections replaced
      Person.hobbies.scores.langNames.query.get.head ==> (
        Set("sport", "opera"),
        Seq(4, 5, 6),
        Map("de" -> "Hallo"),
      )
    }


    "Collection add" - h2(Person_h2()) {
      Person.name("Bob")
        .hobbies(Set("stamps", "trains"))
        .scores(Seq(1, 2, 3))
        .langNames(Map("en" -> "Hello", "es" -> "Hola"))
        .save.transact

      // Add elements to collections
      Person.name_
        .hobbies.add("sports")
        .scores.add(4)
        .langNames.add("de" -> "Hallo")
        .update.transact

      // Elements added to collections
      Person.hobbies.scores.langNames.query.get.head ==> (
        Set("stamps", "trains", "sports"),
        Seq(1, 2, 3, 4),
        Map("en" -> "Hello", "es" -> "Hola", "de" -> "Hallo"),
      )
    }


    "Collection remove" - h2(Person_h2()) {
      Person.name("Bob")
        .hobbies(Set("stamps", "trains"))
        .scores(Seq(1, 2, 3))
        .langNames(Map("en" -> "Hello", "es" -> "Hola"))
        .save.transact

      // Add elements to collections
      Person.name_
        .hobbies.remove("trains")
        .scores.remove(3)
        .langNames.remove("es") // remove by key
        .update.transact

      // Elements removed from collections
      Person.hobbies.scores.langNames.query.get.head ==> (
        Set("stamps"),
        Seq(1, 2),
        Map("en" -> "Hello"),
      )
    }


    "Base update" - h2(Person_h2()) {
      val List(a1, a2) = Address.street.insert("Main st. 17", "5th Ave. 1").transact.ids
      Person.name.likes_?.home_?.insert(
        ("Bob", Some("Pasta"), Some(a1)),
        ("Eva", Some("Sushi"), None),
        ("Liz", None, Some(a2)),
        ("Tod", None, None),
      ).transact

      // Update likes of all persons having a street address
      Person.likes("Beef").Home.street_.update.transact

      // Existing likes of Bob updated since he also has a street address
      Person.name.likes_?.Home.?(Address.street).query.get ==> List(
        ("Bob", Some("Beef"), Some("Main st. 17")), // likes updated
        ("Eva", Some("Sushi"), None),
        ("Liz", None, Some("5th Ave. 1")), // no update without likes value
        ("Tod", None, None),
      )
    }

    "No relationships in upserts" - h2(Person_h2()) {
      intercept[ModelError] {
        Person.likes("Beef").Home.street_.upsert.transact
      }.getMessage() ==> "Upsert of related data not allowed. Please use update instead."
    }


    "Ref update" - h2(Person_h2()) {
      val List(a1, a2) = Address.street.insert("Main st. 17", "5th Ave. 1").transact.ids
      Person.name.likes_?.home_?.insert(
        ("Bob", Some("Pasta"), Some(a1)),
        ("Eva", Some("Sushi"), None),
        ("Liz", None, Some(a2)),
        ("Tod", None, None),
      ).transact

      // Update street addresses of people with preferences
      Person.likes_.Home.street("Bellevue 8").update.transact

      // Only Bob's address updated since he has a preference and street address
      Person.name.likes_?.Home.?(Address.street).query.get ==> List(
        ("Bob", Some("Pasta"), Some("Bellevue 8")), // street updated
        ("Eva", Some("Sushi"), None),
        ("Liz", None, Some("5th Ave. 1")),
        ("Tod", None, None),
      )
    }


    "Ref attr" - h2(Person_h2()) {
      val List(usaId, ukId) = Country.name.insert("USA", "UK").transact.ids

      val bobId = Person.name("Bob")
        .Home.street("Main st. 17").country(usaId).save.transact.id

      Person.name.Home.street.Country.name.query.get ==> List(
        ("Bob", "Main st. 17", "USA"),
      )

      Person(bobId)
        .Home.street("Kings road 1").country(ukId).update.transact

      Person.name.Home.street.Country.name.query.get ==> List(
        ("Bob", "Kings road 1", "UK"),
      )
    }

    "Issue #3" - h2(Person_h2()) {
      // Insert "Bob", no 'age' set yet
      Person.name("Bob").save.transact

      // Get bob id
      val bobId = Person.id.name_("Bob").query.get.head

      // Can't update/upsert optional values
      intercept[ModelError] {
        Person(bobId).age_?(Some(42)).update.transact
      }.getMessage() ==> "Can't update optional values (Person.age_?)"

      intercept[ModelError] {
        Person(bobId).age_?(Some(42)).upsert.transact
      }.getMessage() ==> "Can't upsert optional values (Person.age_?)"

      // Update of null value has no effect
      Person(bobId).age(42).update.transact
      assert(Person.name("Bob").age_?.query.get == List(("Bob", None)))

      // Upsert of null value inserts the value
      Person(bobId).age(42).upsert.transact // note `upsert` instead of `update`
      assert(Person.name("Bob").age_?.query.get == List(("Bob", Some(42))))
    }
  }
}
