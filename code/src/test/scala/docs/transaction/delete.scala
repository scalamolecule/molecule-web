package docs.transaction

import db.dataModel.dsl.Accounting.*
import db.dataModel.dsl.Accounting.metadb.Accounting_h2
import db.dataModel.dsl.Person.*
import db.dataModel.dsl.Person.metadb.Person_h2
import docs.H2Tests
import molecule.db.h2.sync.*
import utest.*


object delete extends H2Tests {

  override lazy val tests = Tests {

    "id" - h2(Person_h2()) {
      val List(bob, liz) = Person.name.age.insert(
        ("Bob", 42),
        ("Liz", 38),
      ).transact.ids

      // Delete entities with a name
      Person(bob).delete.i.transact

      Person.name.age.query.get ==> List(
        ("Liz", 38),
      )
    }

    "not null" - h2(Person_h2()) {
      Person.name_?.age.insert(
        (Some("Liz"), 27),
        (Some("Bob"), 35),
        (Some("Bob"), 42),
        (None, 72),
      ).transact

      // Delete entities with a name
      Person.name_.delete.transact

      Person.name_?.age.a1.query.get ==> List(
        // (Some("Liz"), 27), // deleted
        // (Some("Bob"), 35), // deleted
        (None, 72),
      )
    }

    "null" - h2(Person_h2()) {
      Person.name_?.age.insert(
        (Some("Liz"), 27),
        (Some("Bob"), 35),
        (None, 72),
      ).transact

      // Delete entities _without_ a name
      Person.name_().delete.transact

      Person.name_?.age.a1.query.get ==> List(
        (Some("Liz"), 27),
        (Some("Bob"), 35),
        // (None, 72), // deleted
      )
    }


    "equality" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.name_("Bob").delete.transact

      Person.name.age.a1.query.get ==> List(
        ("Liz", 27),
        // ("Bob", 35), // deleted
        // ("Bob", 42), // deleted
      )
    }


    "OR logic" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_(27, 42).delete.transact
      // Or
      // Person.age_(List(27, 42)).delete.transact

      Person.name.age.a1.query.get ==> List(
        // ("Liz", 27), // deleted
        ("Bob", 35),
        // ("Bob", 42), // deleted
      )
    }


    "Negation" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.not(42).delete.transact

      Person.name.age.a1.query.get ==> List(
        // ("Liz", 27), // deleted
        // ("Bob", 35), // deleted
        ("Bob", 42),
      )
    }

    "NOR logic" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.not(27, 42).delete.transact
      // or
      //      Person.age_.not(List(27, 42)).delete.transact

      Person.name.age.a1.query.get ==> List(
        ("Liz", 27),
        // ("Bob", 35), // deleted
        ("Bob", 42),
      )
    }


    "comparison <" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.<(35).delete.transact

      Person.name.age.a1.query.get ==> List(
        // ("Liz", 27), // deleted
        ("Bob", 35),
        ("Bob", 42),
      )
    }

    "comparison <=" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.<=(35).delete.transact

      Person.name.age.a1.query.get ==> List(
        // ("Liz", 27), // deleted
        // ("Bob", 35), // deleted
        ("Bob", 42),
      )
    }

    "comparison >" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.>(35).delete.transact

      Person.name.age.a1.query.get ==> List(
        ("Liz", 27),
        ("Bob", 35),
        // ("Bob", 42), // deleted
      )
    }


    "comparison >=" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.>=(35).delete.transact

      Person.name.age.a1.query.get ==> List(
        ("Liz", 27),
        // ("Bob", 35), // deleted
        // ("Bob", 42), // deleted
      )
    }


    "Multiple filters" - h2(Person_h2()) {
      Person.name.age.insert(
        ("Liz", 27),
        ("Bob", 35),
        ("Bob", 42),
      ).transact

      Person.age_.>(30).age_.<(45).delete.transact

      Person.name.age.a1.query.get ==> List(
        ("Liz", 27),
        // ("Bob", 35), // deleted
        // ("Bob", 42), // deleted
      )
    }




    // Ref ------------------------

    "ref" - h2(Accounting_h2()) {

      // Insert 2 invoices, each with 2 invoice lines
      Invoice.no.Lines.*(
        InvoiceLine.qty.product.lineTotal
      ).insert(
        // Invoice 1
        (1, List(
          // Invoice lines for invoice 1
          (2, "Socks", 30),
          (5, "Bread", 50),
        )),

        // Invoice 2
        (2, List(
          (1, "Knife", 50),
          (4, "Bread", 40),
        ))
      ).transact

      // Delete invoice 1 and its invoice lines!
      Invoice.no_(1).delete.transact

      // Invoice 1 and its invoice lines deleted
      Invoice.no.a1.Lines.*?(
        InvoiceLine.qty.a1.product.lineTotal
      ).query.get ==> List(
        (2, List(
          (1, "Knife", 50),
          (4, "Bread", 40),
        ))
      )

      // Confirming that invoice lines of invoice have been deleted
      InvoiceLine.qty.a1.product.lineTotal.query.get ==> List(
        (1, "Knife", 50),
        (4, "Bread", 40),
      )
    }
  }
}
