package docs.transaction

import db.dataModel.dsl.Person._
import db.dataModel.dsl.Accounting.metadb.Accounting_MetaDb_h2
import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_MetaDb_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object insert extends H2Tests {

  override lazy val tests = Tests {

    "varargs" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name.age.insert(
        ("Bob", 42),
        ("Liz", 38),
      ).transact

      Person.name.age.query.get ==> List(
        ("Bob", 42),
        ("Liz", 38),
      )
    }


    "list" - h2(Person_MetaDb_h2()) { implicit conn =>
      val listOfData = List(
        ("Bob", Some(42)),
        ("Liz", None),
      )
      Person.name.age_?.insert(listOfData).transact

      Person.name.age_?.query.get ==> List(
        ("Bob", Some(42)),
        ("Liz", None),
      )
    }


    "ref" - h2(Person_MetaDb_h2()) { implicit conn =>
      Person.name.age.Home.street.insert(
        ("Bob", 42, "Main st. 17"),
        ("Liz", 38, "5th Ave 1"),
      ).transact

      Person.name.age.Home.street.query.get ==> List(
        ("Bob", 42, "Main st. 17"),
        ("Liz", 38, "5th Ave 1"),
      )
    }


    "ref attr" - h2(Person_MetaDb_h2()) { implicit conn =>
      Country.name("USA").save.transact
      val usaId = Country.id.name_("USA").query.get.head

      Person.name.age.Home.street.country.insert(
        ("Bob", 42, "Main st. 17", usaId),
        ("Liz", 38, "5th Ave 1", usaId),
      ).transact

      Person.name.age.Home.street.Country.name.query.get ==> List(
        ("Bob", 42, "Main st. 17", "USA"),
        ("Liz", 38, "5th Ave 1", "USA"),
      )
    }


    "nested" - h2(Accounting_MetaDb_h2()) { implicit conn =>
      import db.dataModel.dsl.Accounting._

      Invoice.no.Lines.*(
        InvoiceLine.qty.product.unitPrice.lineTotal
      ).insert(
        // Invoice 1
        (1, List(
          // Invoice lines for invoice 1
          (2, "Socks", 15, 30),
          (5, "Bread", 10, 50),
        )),

        // Invoice 2
        (2, List(
          (1, "Knife", 40, 50),
          (4, "Bread", 10, 40),
        ))
      ).transact

      Invoice.no.a1.Lines.*(
        InvoiceLine.qty.a1.product.unitPrice.lineTotal
      ).query.get ==> List(
        (1, List(
          (2, "Socks", 15, 30),
          (5, "Bread", 10, 50),
        )),
        (2, List(
          (1, "Knife", 40, 50),
          (4, "Bread", 10, 40),
        ))
      )
    }


    "nested opt" - h2(Accounting_MetaDb_h2()) { implicit conn =>
      import db.dataModel.dsl.Accounting._

      Invoice.no.Lines.*(
        InvoiceLine.qty.product.unitPrice.lineTotal
      ).insert(
        (1, List(
          (2, "Socks", 15, 30),
          (5, "Bread", 10, 50),
        )),
        (2, List()) // Invoice 2 without invoice lines
      ).transact

      Invoice.no.a1.Lines.*(
        InvoiceLine.qty.a1.product.unitPrice.lineTotal
      ).query.get ==> List(
        (1, List(
          (2, "Socks", 15, 30),
          (5, "Bread", 10, 50),
        ))
      )

      Invoice.no.a1.Lines.*?(
        InvoiceLine.qty.a1.product.unitPrice.lineTotal
      ).query.get ==> List(
        (1, List(
          (2, "Socks", 15, 30),
          (5, "Bread", 10, 50),
        )),
        (2, List()) // Invoice 2 without invoice lines
      )
    }
  }
}
