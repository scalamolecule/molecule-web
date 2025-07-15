package docs.compare.sql.slick

import java.sql.DriverManager
import db.compare.sql.slick.dsl.People.*
import db.compare.sql.slick.dsl.People.metadb.People_MetaDb_h2
import docs.H2Tests
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.h2.sync.*
import utest.*
import scala.util.Random
import scala.util.Using.Manager
import molecule.db.common.marshalling.JdbcProxy

object PeopleTutorial extends H2Tests {


  override lazy val tests = Tests {

    // Implicit connection to a fresh empty database
    implicit val conn: JdbcConn_JVM = {
      val url     = "jdbc:h2:mem:test" + Random.nextInt().abs
      val proxy   = JdbcProxy(url, People_MetaDb_h2())
      val sqlConn = DriverManager.getConnection(proxy.url)
      JdbcHandler_JVM.recreateDb(proxy, sqlConn)
    }

    Address.street.city.insert(
      ("station 14", "Lausanne"),
      ("Grand Central 1", "New York City"),
    ).transact

    Person.name.age.address.insert(
      ("C. Vogt", 999, 1L),
      ("J. Vogt", 1001, 1L),
      ("J. Doe", 18, 2L),
    ).transact


    "Select all table values" - {
      Person.id.name.age.query.get ==> List(
        (1, "C. Vogt", 999),
        (2, "J. Vogt", 1001),
        (3, "J. Doe", 18),
      )
    }

    "Select certain columns" - {
      Person.name.age.query.get ==> List(
        ("C. Vogt", 999),
        ("J. Doe", 18),
        ("J. Vogt", 1001),
      )
      Person.id.name.age.query.get.map {
        case (id, name, age) => (age, s"$name ($id)")
      } ==> List(
        (999, "C. Vogt (1)"),
        (1001, "J. Vogt (2)"),
        (18, "J. Doe (3)"),
      )
    }

    "filter" - {
      Person.age.>=(18).name("C. Vogt").query.get ==> List(
        (999, "C. Vogt"),
      )
    }

    "sortBy" - {
      Person.age.a1.name.query.get ==> List(
        (18, "J. Doe"),
        (999, "C. Vogt"),
        (1001, "J. Vogt"),
      )
    }

    "aggregations" - {
      Person.age(max).query.get ==> List(
        1001
      )
      Person.age(max(2)).query.get ==> List(
        Set(1001, 999)
      )
    }

    "groupBy" - {
      Person.address.age(avg).query.get ==> List(
        (1, 1000),
        (2, 18),
      )
    }

    "groupBy + filter" - {
      Person.address.age(avg).query.get.filter(_._2 > 50) ==> List(
        (1, 1000),
      )
    }

    "join" - {
      Person.name.Address.city.query.get ==> List(
        ("C. Vogt", "Lausanne"),
        ("J. Doe", "New York City"),
        ("J. Vogt", "Lausanne"),
      )
    }

    "left join" - {
      // Person without address
      Person.name("Ben").save.transact
      Person.name.Address.?(Address.city).query.get ==> List(
        ("Ben", None),
        ("C. Vogt", Some("Lausanne")),
        ("J. Doe", Some("New York City")),
        ("J. Vogt", Some("Lausanne")),
      )
    }

    "subquery" - {
      Person.name.Address.city_("New York City").query.get ==> List(
        "J. Doe"
      )
    }

    "insert" - {
      Person.name.age.address.insert(
        ("M Odersky", 12345, 1L),
      ).transact
      Person.name.age.address.query.get ==> List(
        ("C. Vogt", 999, 1L),
        ("J. Doe", 18, 2L),
        ("J. Vogt", 1001, 1L),
        ("M Odersky", 12345, 1L),
      )
    }

    "save" - {
      Person.name("M Odersky").age(12345).address(1).save.transact
      Person.name.age.address.query.get ==> List(
        ("C. Vogt", 999, 1L),
        ("J. Doe", 18, 2L),
        ("J. Vogt", 1001, 1L),
        ("M Odersky", 12345, 1L),
      )
    }

    "update" - {
      Person.name("M Odersky").age(12345).address(1).save.transact
      Person.name_("M Odersky").name("M. Odersky").age(54321).update.transact
      Person.name.age.address.query.get ==> List(
        ("C. Vogt", 999, 1L),
        ("J. Doe", 18, 2L),
        ("J. Vogt", 1001, 1L),
        ("M. Odersky", 54321, 1L),
      )
    }

    "delete" - {
      Person.name("M. Odersky").age(54321).address(1).save.transact
      Person.name.age.address.query.get ==> List(
        ("C. Vogt", 999, 1L),
        ("J. Doe", 18, 2L),
        ("J. Vogt", 1001, 1L),
        ("M. Odersky", 54321, 1L),
      )
      Person.name_("M. Odersky").delete.transact
      Person.name.age.address.query.get ==> List(
        ("C. Vogt", 999, 1L),
        ("J. Doe", 18, 2L),
        ("J. Vogt", 1001, 1L),
      )
    }

    "case" - {
      Person.address(1, 2).query.get.map {
        case 1 => "A"
        case 2 => "B"
      } ==> List("A", "B")
    }
  }
}
