package docs.compare.tutorials.scalasql

import java.sql.DriverManager
import scala.io.Source
import scala.util.Random
import scala.util.Using.Manager
import db.compare.tutorials.scalasql.dsl.World.*
import db.compare.tutorials.scalasql.dsl.World.metadb.World_h2
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import docs.relationship.relationships.h2
import molecule.core.error.ModelError
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import utest.*

object Subquery extends H2Tests {

  override lazy val tests = Tests {

    // Implicit connection to a fresh empty database
    given conn: JdbcConn_JVM = {
      val url     = "jdbc:h2:mem:test" + Random.nextInt().abs
      val proxy   = JdbcProxy(url, World_h2())
      val sqlConn = DriverManager.getConnection(proxy.url)
      JdbcHandler_JVM.recreateDb(proxy, sqlConn)
    }

    // Insert data with JDBC from SQL in file
    val buf = Source.fromFile("code/src/test/scala/docs/compare/tutorials/scalasql/world-data.sql")
    conn.sqlConn.createStatement().executeUpdate(buf.mkString)
    buf.close()

    // Add foreign keys pointing to Country
    Country.code.id.query.get.foreach {
      case (countryCode, id) => transact(
        City.countryCode_(countryCode).country(id).upsert,
        CountryLanguage.countryCode_(countryCode).country(id).upsert
      )
    }


    "Aggregate per row" - {
      Country.name.join(
        City
          .id(count).d1 // aggregate count of cities per row
          .country_(Country.id_) // correlate fk country with Country.id
      ).query.i.limit(3).get ==> List(
        ("China", 363),
        ("India", 341),
        ("United States", 274),
      )

      Country.name.join(
        City.id(count).d1.population(max).country_(Country.id_)
      ).query.i.limit(3).get ==> List(
        ("China", (363, 9696300)),
        ("India", (341, 10500000)),
        ("United States", (274, 8008278))
      )
    }


    "Aggregate per row, non-id" - {
      Country.name.join(
        City
          .id(count).d1
          .countryCode_(Country.code_) // correlate value countryCode with Country.code
      ).query.limit(3).get ==> List(
        ("China", 363),
        ("India", 341),
        ("United States", 274),
      )
    }


    "Limit & offset" - {
      // 3 most populous countries each with their 2 biggest cities:
      Country.name.population.d1.join(
        City.name.population.d1.country_(Country.id_).query.limit(2)
      ).query.limit(6).get ==> List(
        ("China", 1277558000, ("Shanghai", 9696300)),
        ("China", 1277558000, ("Peking", 7472000)),
        ("India", 1013662000, ("Mumbai (Bombay)", 10500000)),
        ("India", 1013662000, ("Delhi", 7206704)),
        ("United States", 278357000, ("New York", 8008278)),
        ("United States", 278357000, ("Los Angeles", 3694820))
      )

      Country.name.population.d1.join(
        City.name.population.d1.country_(Country.id_).query.limit(1)
      ).query.limit(3).get ==> List(
        ("China", 1277558000, ("Shanghai", 9696300)),
        ("India", 1013662000, ("Mumbai (Bombay)", 10500000)),
        ("United States", 278357000, ("New York", 8008278)),
      )

      Country.name.population.d1.join(
        City.name.population.a1.country_(Country.id_).query.limit(1)
      ).query.limit(3).get ==> List(
        ("China", 1277558000, ("Huangyan", 89288)),
        ("India", 1013662000, ("Vejalpur", 89053)),
        ("United States", 278357000, ("Charleston", 89063))
      )

      Country.name.population.d1.join(
        City.name.population.d1.country_(Country.id_).query.limit(1).offset(1)
      ).query.limit(3).get ==> List(
        ("China", 1277558000, ("Peking", 7472000)),
        ("India", 1013662000, ("Delhi", 7206704)),
        ("United States", 278357000, ("Los Angeles", 3694820))
      )
    }


    "Advanced" - {

      Country.name.governmentForm_("Republic").join(
        CountryLanguage.language(count).>(8).d1.isOfficial_(false).country_(Country.id_) //.query.limit(2)
      ).query.i.get ==> List(
        ("Congo, The Democratic Republic of the", 10),
        ("Kenya", 10),
        ("Mozambique", 10),
        ("Tanzania", 10),
        ("Uganda", 10),
        ("Angola", 9),
        ("Philippines", 9),
      )


    }

  }
}
