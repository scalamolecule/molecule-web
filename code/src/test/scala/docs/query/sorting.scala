package docs.query

import db.dataModel.dsl.Products._
import db.dataModel.dsl.Products.metadb.Products_MetaDb_h2
import docs.H2Tests
import molecule.base.error.ModelError
import molecule.db.h2.sync._
import utest._


object sorting extends H2Tests {

  override lazy val tests = Tests {

    "basics" - h2(Products_MetaDb_h2()) { implicit conn =>

      Product.name.price.stars.insert(
        ("Knife", 20, 3),
        ("Socks", 10, 4),
        ("Bread", 10, 3),
      ).transact

      Product.name.price.d1.stars.d2.query.get ==> List(
        ("Knife", 20, 3),
        ("Socks", 10, 4),
        ("Bread", 10, 3),
      )


      Product.name.a1.query.get ==> List(
        "Bread",
        "Knife",
        "Socks",
      )

      Product.name.d1.query.get ==> List(
        "Socks",
        "Knife",
        "Bread",
      )


      Product.name.price.a1.stars.a2.query.get ==> List(
        ("Bread", 10, 3),
        ("Socks", 10, 4),
        ("Knife", 20, 3),
      )

      Product.name.price.a1.stars.d2.query.get ==> List(
        ("Socks", 10, 4),
        ("Bread", 10, 3),
        ("Knife", 20, 3),
      )

      Product.name.price.d1.stars.a2.query.get ==> List(
        ("Knife", 20, 3),
        ("Bread", 10, 3),
        ("Socks", 10, 4),
      )

      Product.name.price.d1.stars.d2.query.get ==> List(
        ("Knife", 20, 3),
        ("Socks", 10, 4),
        ("Bread", 10, 3),
      )


      Product.name.price.a2.stars.a1.query.get ==> List(
        ("Bread", 10, 3),
        ("Knife", 20, 3),
        ("Socks", 10, 4),
      )

      Product.name.price.d2.stars.a1.query.get ==> List(
        ("Knife", 20, 3),
        ("Bread", 10, 3),
        ("Socks", 10, 4),
      )

      Product.name.price.a2.stars.d1.query.get ==> List(
        ("Socks", 10, 4),
        ("Bread", 10, 3),
        ("Knife", 20, 3),
      )

      Product.name.price.d2.stars.d1.query.get ==> List(
        ("Socks", 10, 4),
        ("Knife", 20, 3),
        ("Bread", 10, 3),
      )


      Product.name.price.insert(
        ("Paper", 10),
      ).transact

      Product.name.price.d1.stars_?.a2.query.get ==> List(
        ("Knife", 20, Some(3)),
        ("Paper", 10, None),
        ("Bread", 10, Some(3)),
        ("Socks", 10, Some(4)),
      )

      Product.name.price.a1.stars_?.d2.query.get ==> List(
        ("Socks", 10, Some(4)),
        ("Bread", 10, Some(3)),
        ("Paper", 10, None),
        ("Knife", 20, Some(3)),
      )


      intercept[ModelError] {
        Product.name.a2.query.get
      }.msg ==>
        "Sort index 1 should be present and additional indexes " +
          "continuously increase (in any order). " +
          "Found non-unique sort index(es): 2"


      intercept[ModelError] {
        Product.name.a1.price.d3.query.get
      }.msg ==>
        "Sort index 1 should be present and additional indexes " +
          "continuously increase (in any order). " +
          "Found non-unique sort index(es): 1, 3"

      intercept[ModelError] {
        Product.name.a1.price.d1.query.get
      }.msg ==>
        "Sort index 1 should be present and additional indexes " +
          "continuously increase (in any order). " +
          "Found non-unique sort index(es): 1, 1"


      Product.price.stars(avg).d1.query.get ==> List(
        (10, 3.5),
        (20, 3.0),
      )
    }


    "nested" - h2(Products_MetaDb_h2()) { implicit conn =>

      Category.ordering.name.Products.*(Product.name.price).insert(
        (1, "Home", List(
          ("Lamp", 25),
          ("Poster", 25),
          ("Couch", 370),
        )),
        (2, "Outdoor", List(
          ("Hose", 50),
          ("Sun screen", 70),
        )),
      ).transact

      Category.ordering.a1.name
        .Products.*(Product.name.a2.price.d1).query.get ==> List(
        (1, "Home", List(
          ("Couch", 370),
          ("Lamp", 25),
          ("Poster", 25),
        )),
        (2, "Outdoor", List(
          ("Sun screen", 70),
          ("Hose", 50),
        )),
      )


      val dynamicQuery = (priceOrder: Int) => Category.ordering.a1.name
        .Products.*(Product.name.a2.price.sort(priceOrder)).query

      // Descending price
      dynamicQuery(-1).get ==> List(
        (1, "Home", List(
          ("Couch", 370), // first
          ("Lamp", 25),
          ("Poster", 25),
        )),
        (2, "Outdoor", List(
          ("Sun screen", 70), // first
          ("Hose", 50),
        )),
      )

      // Ascending price
      dynamicQuery(1).get ==> List(
        (1, "Home", List(
          ("Lamp", 25),
          ("Poster", 25),
          ("Couch", 370), // last
        )),
        (2, "Outdoor", List(
          ("Hose", 50),
          ("Sun screen", 70), // last
        )),
      )
    }
  }
}


