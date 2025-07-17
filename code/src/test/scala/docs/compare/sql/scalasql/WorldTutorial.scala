package docs.compare.sql.scalasql

import java.sql.DriverManager
import db.compare.sql.scalasql.dsl.World.*
import db.compare.sql.scalasql.dsl.World.metadb.World_MetaDb_h2
import docs.H2Tests
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import utest.*
import scala.io.Source
import scala.util.Random
import scala.util.Using.Manager

object WorldTutorial extends H2Tests {

  override lazy val tests = Tests {

    // Implicit connection to a fresh empty database
    implicit val conn: JdbcConn_JVM = {
      val url     = "jdbc:h2:mem:test" + Random.nextInt().abs
      val proxy   = JdbcProxy(url, World_MetaDb_h2())
      val sqlConn = DriverManager.getConnection(proxy.url)
      JdbcHandler_JVM.recreateDb(proxy, sqlConn)
    }

    // Insert data with JDBC from SQL in file
    val buf = Source.fromFile("code/src/test/scala/docs/compare/sql/scalasql/world-data.sql")
    conn.sqlConn.createStatement().executeUpdate(buf.mkString)
    buf.close()

    // Add foreign keys pointing to Country
    Country.code.id.query.get.foreach {
      case (countryCode, id) => transact(
        City.countryCode_(countryCode).country(id).upsert,
        CountryLanguage.countryCode_(countryCode).country(id).upsert
      )
    }


    "select" - {
      City.id.name.countryCode.district.population.query.get.take(3) ==> Seq(
        (1, "Kabul", "AFG", "Kabol", 1780000),
        (2, "Qandahar", "AFG", "Qandahar", 237500),
        (3, "Herat", "AFG", "Herat", 186800),
      )
    }

    "select2" - {
      City.countryCode.name.population.query.get.take(3) ==> Seq(
        ("ABW", "Oranjestad", 29034),
        ("AFG", "Herat", 186800),
        ("AFG", "Kabul", 1780000),
      )
    }


    "filter" - {
      "singleName" - {
        City.id.name("Singapore").countryCode.district.population
          .query.get.head ==>
          (3208, "Singapore", "SGP", "", 4017733)
      }


      "by Id" - {
        City.id(3208).name.countryCode.district.population
          .query.get.head ==>
          (3208, "Singapore", "SGP", "", 4017733)
      }


      "multiple" - {
        City.id.name.countryCode("CHN").district.population.>(5000000)
          .query.get.take(2) ==> Seq(
          (1890, "Shanghai", "CHN", "Shanghai", 9696300),
          (1891, "Peking", "CHN", "Peking", 7472000)
        )
      }
    }


    "lifting" - {
      "implicit" - {
        def find(cityId: Int) = City.id(cityId).name.countryCode.district.population.query.get

        find(3208) ==> Seq((3208, "Singapore", "SGP", "", 4017733))
        find(3209) ==> Seq((3209, "Bratislava", "SVK", "Bratislava", 448292))
      }

      "values" - {
        City.name_("Singapore", "Kuala Lumpur", "Jakarta").countryCode
          .query.get ==> Seq("IDN", "MYS", "SGP")
      }
    }


    "mapping" - {
      "tuple2" - {
        Country.name.continent.query.get.take(5) ==> Seq(
          ("Afghanistan", "Asia"),
          ("Albania", "Europe"),
          ("Algeria", "Africa"),
          ("American Samoa", "Oceania"),
          ("Andorra", "Europe"),
        )
      }


      "heterogenousTuple" - {
        City.id.name("Singapore").countryCode.district.population.query.get.map {
          case c@(_, name, _, _, population) =>
            (c, name.toUpperCase, population / 1000000)
        }.head ==>
          (
            (3208, "Singapore", "SGP", "", 4017733),
            "SINGAPORE",
            4 // population in millions
          )
      }
    }


    "aggregate" - {
      "sum" - {
        City.countryCode_("CHN").population(sum).query.get.head ==> 175953614
      }

      "sumBy" - {
        City.population(sum).query.get.head ==> 1429559884
      }

      "size" - {
        Country.id(count).population_.>(1000000).query.get.head ==> 154
      }

      "aggregate" - {
        Country.population(min).population(avg).population(max)
          .query.get.head ==> (0, 25434098.11715481, 1277558000)
      }
    }


    "sortDropTake" - {
      City.name.population.d1.query.offset(5).limit(5).get._1 ==> Seq(
        ("Karachi", 9269265),
        ("Istanbul", 8787958),
        ("Ciudad de México", 8591309),
        ("Moscow", 8389200),
        ("New York", 8008278)
      )
    }


    "casting" - {
      Country.name_("Singapore").lifeExpectancy.query.get.head.toInt ==> 80
    }


    "nullable" - {
      Country.capital_().id(count).query.get.head ==> 7
    }


    "optional" - {
      Country.capital_?(None).id(count).query.get.head ==> (None, 7)
    }


    "joins" - {
      "inner" - {
        City.name.Country.name_("Liechtenstein")
          .query.get ==> Seq("Schaan", "Vaduz")
      }


      "right" - {
        City.?(City.country_()).Country.name
          .query.get ==> Seq(
          (None, "Antarctica"),
          (None, "Bouvet Island"),
          (None, "British Indian Ocean Territory"),
          (None, "French Southern territories"),
          (None, "Heard Island and McDonald Islands"),
          (None, "South Georgia and the South Sandwich Islands"),
          (None, "United States Minor Outlying Islands")
        )
      }


      "left" - {
        Country.name.startsWith("Un").a1.Capital.?(City.name).query.get ==> Seq(
          ("United Arab Emirates", Some("Abu Dhabi")),
          ("United Kingdom", Some("London")),
          ("United States", Some("Washington")),
          ("United States Minor Outlying Islands", None), // no relationship
        )
      }
    }


    "subqueries" - {
      val top2 = Country.id.population.d1.query.limit(2).get.map(_._1)
      CountryLanguage.language.a1.Country.id_(top2).name.query.limit(5).get ==> Seq(
        ("Asami", "India"),
        ("Bengali", "India"),
        ("Chinese", "China"),
        ("Dong", "China"),
        ("Gujarati", "India"),
      )

      rawQuery(
        """SELECT countrylanguage1.language AS res_0, subquery0.name AS res_1
          |FROM (SELECT
          |    country0.code AS code,
          |    country0.name AS name,
          |    country0.population AS population
          |  FROM country country0
          |  ORDER BY population DESC
          |  LIMIT 2) subquery0
          |JOIN countrylanguage countrylanguage1
          |ON (subquery0.code = countrylanguage1.countrycode)
          |ORDER BY res_0
          |LIMIT 5
          |""".stripMargin
      ) ==> Seq(
        Seq("Asami", "India"),
        Seq("Bengali", "India"),
        Seq("Chinese", "China"),
        Seq("Dong", "China"),
        Seq("Gujarati", "India"),
      )
    }


    "union/except/intersect" - {
      // (need to sort by Country name too since several countries have 0 inhabitants)
      Country.name.a2.population.a1.query.limit(2).get.map(_._1) ==> Seq("Antarctica", "Bouvet Island")
      Country.name.population.d1.query.limit(2).get.map(_._1) ==> Seq("China", "India")

      rawQuery(
        """SELECT subquery0.res AS res
          |FROM (SELECT country0.name AS res
          |  FROM country country0
          |  ORDER BY country0.population ASC, res
          |  LIMIT 2) subquery0
          |UNION
          |SELECT subquery0.res AS res
          |FROM (SELECT country0.name AS res
          |  FROM country country0
          |  ORDER BY country0.population DESC, res
          |  LIMIT 2) subquery0
          |LIMIT 5
          |""".stripMargin
      ).flatten ==> Seq("Antarctica", "Bouvet Island", "China", "India")
    }


    "realistic queries" - {
      "top languages" - {
        rawQuery(
          """SELECT countrylanguage.language AS res_0, COUNT(1) AS res_1
            |FROM city
            |JOIN countrylanguage ON (city.countrycode = countrylanguage.countrycode)
            |GROUP BY countrylanguage.language
            |ORDER BY res_1 DESC
            |LIMIT 10
            |""".stripMargin
        ) ==> Seq(
          Seq("Chinese", 1083),
          Seq("German", 885),
          Seq("Spanish", 881),
          Seq("Italian", 857),
          Seq("English", 823),
          Seq("Japanese", 774),
          Seq("Portuguese", 629),
          Seq("Korean", 608),
          Seq("Polish", 557),
          Seq("French", 467)
        )
      }


      "populous cities" - {
        // 1 query for countries + 3 subqueries for cities
        val topCity = (country: Long) =>
          City.name.population.d1.country_(country).query.limit(1).get

        Country.id.name.population.d1.query.limit(3).get.map {
          case (id, country, population) =>
            val (city, popCity) = topCity(id).head
            (country, population, city, popCity)
        } ==> Seq(
          ("China", 1277558000, "Shanghai", 9696300),
          ("India", 1013662000, "Mumbai (Bombay)", 10500000),
          ("United States", 278357000, "New York", 8008278)
        )

        rawQuery(
          """SELECT
            |  subquery0.name AS res_0,
            |  subquery0.population AS res_1,
            |  city1.name AS res_2,
            |  city1.population AS res_3
            |FROM (SELECT
            |    country0.code AS code,
            |    country0.name AS name,
            |    country0.population AS population
            |  FROM country country0
            |  ORDER BY population DESC
            |  LIMIT 3) subquery0
            |JOIN city city1 ON (subquery0.code = city1.countrycode)
            |WHERE (city1.id = (SELECT
            |    city2.id AS res
            |    FROM city city2
            |    WHERE (city2.countrycode = subquery0.code)
            |    ORDER BY city2.population DESC
            |    LIMIT 1))
            |""".stripMargin
        ) ==> Seq(
          Seq("China", 1277558000, "Shanghai", 9696300),
          Seq("India", 1013662000, "Mumbai (Bombay)", 10500000),
          Seq("United States", 278357000, "New York", 8008278)
        )
      }
    }


    "inserts" - {
      "single row" - {
        City.name("Sentosa").countryCode("SGP").district("South").population(1337).save.transact

        City.name.countryCode_("SGP").district.population.d1.query.get ==> Seq(
          ("Singapore", "", 4017733),
          ("Sentosa", "South", 1337),
        )
      }


      "batch" - {
        City.name.countryCode.district.population.insert(
          ("Sentosa", "SGP", "South", 1337), // ID provided by database AUTO_INCREMENT
          ("Loyang", "SGP", "East", 31337),
          ("Jurong", "SGP", "West", 313373)
        ).transact

        City.id.name.countryCode_("SGP").district.population.query.get ==> Seq(
          (3208, "Singapore", "", 4017733),
          (4080, "Sentosa", "South", 1337),
          (4081, "Loyang", "East", 31337),
          (4082, "Jurong", "West", 313373),
        )
      }


      "arbitrary" - {
        City.name.countryCode.district.population.insert(
          City.name("Singapore").countryCode.query.get.map {
            case (name, countryCode) => ("New-" + name, countryCode, "", 0L)
          }
        ).transact

        City.id.name.countryCode_("SGP").district.population.query.get ==> Seq(
          (3208, "Singapore", "", 4017733),
          (4080, "New-Singapore", "", 0)
        )
      }
    }


    "Update" - {
      "static values" - {
        City.countryCode_("SGP").district("UNKNOWN").population(0).update.transact

        City.name.countryCode_("SGP").district.population.query.get ==> Seq(
          ("Singapore", "UNKNOWN", 0)
        )
      }


      "computed values" - {
        City.countryCode_("SGP").population.+(1000000).update.transact

        City.name.countryCode_("SGP").population.query.get ==> Seq(
          ("Singapore", 5017733)
        )
      }


      "all" - {
        // Filter all cities having a population value and then set it to 0 for all!
        // Molecule puts trust in your hands that you know what you are doing.
        City.population_.population(0).update.transact

        City.name.countryCode_("LIE").population.query.get ==> Seq(
          ("Schaan", 0),
          ("Vaduz", 0),
        )
      }
    }


    "delete" - {
      City.name(count).countryCode_("SGP").query.get.head ==> 1
      City.countryCode_("SGP").delete.transact
      City.name(count).countryCode_("SGP").query.get.head ==> 0
    }


    "transactions" - {
      City.countryCode("SGP").query.get ==> Seq("SGP")
      try {
        unitOfWork {
          City.countryCode_("SGP").delete.transact
          City.countryCode("SGP").query.get ==> Seq()
          throw new Exception()
        }
      } catch {
        case _: Exception => ()
      }
      City.countryCode("SGP").query.get ==> Seq("SGP")
    }


    "savepoint explicit" - {
      City.countryCode("SGP").query.get ==> Seq("SGP")
      unitOfWork {
        savepoint { sp =>
          City.countryCode_("SGP").delete.transact
          City.countryCode("SGP").query.get ==> Seq()
          sp.rollback()
        }
      }
      City.countryCode("SGP").query.get ==> Seq("SGP")
    }

    "savepoint throw" - {
      City.countryCode("SGP").query.get ==> Seq("SGP")
      try {
        unitOfWork {
          savepoint { _ =>
            City.countryCode_("SGP").delete.transact
            City.countryCode("SGP").query.get ==> Seq()
            throw new Exception()
          }
        }
      } catch {
        case _: Exception => ()
      }
      City.countryCode("SGP").query.get ==> Seq("SGP")
    }
  }
}
