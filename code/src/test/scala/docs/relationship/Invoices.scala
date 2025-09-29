package docs.relationship

import db.dataModel.dsl.Accounting.*
import db.dataModel.dsl.Accounting.metadb.Accounting_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object Invoices extends H2Tests {

  override lazy val tests = Tests {

    "invoice" - h2(Accounting_h2()) {

      //  // Invoice lines typically inserted with each invoice
      Customer.name.Invoices.*(Invoice.no.Lines.*(InvoiceLine.product.amount)).insert(
        ("Bob's cafe", List(
          (1, List(
            ("Coffee", 30),
            ("Milk", 10),
            ("Tea", 20),
          )),
          (2, List(
            ("Chocolate", 70),
            ("Tea", 20),
          )),
        ))
      ).transact.ids

      // Nested retrieval, invoice-to-lines
      Invoice.no.Lines.*(InvoiceLine.amount).query.i.get ==> List(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      )
      Invoice.no.Lines.*(InvoiceLine.product.amount).query.get ==> List(
        (1, List(
          ("Coffee", 30),
          ("Milk", 10),
          ("Tea", 20),
        )),
        (2, List(
          ("Chocolate", 70),
          ("Tea", 20),
        )),
      )
      Invoice.no.Lines.*(InvoiceLine.product).query.get ==> List(
        (1, List("Coffee", "Milk", "Tea")),
        (2, List("Chocolate", "Tea")),
      )

      Customer.name.Invoices.*(
        Invoice.no.Lines.*(
          InvoiceLine.product.amount)).query.get ==> List(
        ("Bob's cafe", List(
          (1, List(
            ("Coffee", 30),
            ("Milk", 10),
            ("Tea", 20),
          )),
          (2, List(
            ("Chocolate", 70),
            ("Tea", 20),
          )),
        ))
      )


      // Flat retrieval, invoice-to-line
      Invoice.no.a1.Lines.amount.a2.query.get ==> List(
        (1, 10),
        (1, 20),
        (1, 30),
        (2, 20),
        (2, 70),
      )
      Invoice.no.a1.Lines.product.a2.query.get ==> List(
        (1, "Coffee"),
        (1, "Milk"),
        (1, "Tea"),
        (2, "Chocolate"),
        (2, "Tea"),
      )


      // Line amount to invoice number pairs
      InvoiceLine.amount.Invoice.no.query.get ==> List(
        (10, 1),
        (20, 1),
        (20, 2),
        (30, 1),
        (70, 2),
      )
      InvoiceLine.product.a1.Invoice.no.a2.query.get ==> List(
        ("Chocolate", 2),
        ("Coffee", 1),
        ("Milk", 1),
        ("Tea", 1),
        ("Tea", 2),
      )


      Invoice.no(3).save.transact

      Invoice.no.Lines.*?(InvoiceLine.product).query.get ==> List(
        (1, List("Coffee", "Milk", "Tea")),
        (2, List("Chocolate", "Tea")),
        (3, List()),
      )

//      val ids = Invoice.id.no_.query.get
//      Invoice(ids.head).delete.transact
//
//      Invoice.no.Lines.*(InvoiceLine.amount).query.get ==> List(
//        (2, List(20, 70)),
//      )
    }
  }
}
