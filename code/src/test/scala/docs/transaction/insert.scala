package docs.transaction

import db.dataModel.dsl.Accounting.*
import db.dataModel.dsl.Accounting.metadb.Accounting_h2
import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object insert extends H2Tests {

  override lazy val tests = Tests {

    "varargs" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Bob", 42),
        ("Liz", 38),
      ).transact

      Person.name.age.query.get ==> List(
        ("Bob", 42),
        ("Liz", 38),
      )
    }


    "list" - h2(Person_h2()) {
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


    "ref" - h2(Person_h2()) {
      Person.name.age.Home.street.insert(
        ("Bob", 42, "Main st. 17"),
        ("Liz", 38, "5th Ave 1"),
      ).transact

      Person.name.age.Home.street.query.get ==> List(
        ("Bob", 42, "Main st. 17"),
        ("Liz", 38, "5th Ave 1"),
      )
    }


    "ref attr" - h2(Person_h2()) {
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


    "nested" - h2(Accounting_h2()) {

      Invoice.no.Lines.*(
        InvoiceLine.product.amount
      ).insert(
        // Invoice 1
        (1, List(
          // Invoice lines for invoice 1
          ("Bread", 50),
          ("Socks", 30),
        )),

        // Invoice 2
        (2, List(
          ("Bread", 40),
          ("Knife", 50),
        ))
      ).transact

      Invoice.no.a1.Lines.*(
        InvoiceLine.product.a1.amount
      ).query.get ==> List(
        (1, List(
          ("Bread", 50),
          ("Socks", 30),
        )),
        (2, List(
          ("Bread", 40),
          ("Knife", 50),
        ))
      )
    }


    "nested opt" - h2(Accounting_h2()) {

      Invoice.no.Lines.*(
        InvoiceLine.product.a1.amount
      ).insert(
        (1, List(
          ("Bread", 50),
          ("Socks", 30),
        )),
        (2, List()) // Invoice 2 without invoice lines
      ).transact

      Invoice.no.a1.Lines.*(
        InvoiceLine.product.a1.amount
      ).query.get ==> List(
        (1, List(
          ("Bread", 50),
          ("Socks", 30),
        ))
      )

      Invoice.no.a1.Lines.*?(
        InvoiceLine.product.a1.amount
      ).query.get ==> List(
        (1, List(
          ("Bread", 50),
          ("Socks", 30),
        )),
        (2, List()) // Invoice 2 without invoice lines
      )
    }
  }
}
