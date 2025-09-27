package docs.relationship

import db.relationships.dsl.one2many.*
import db.relationships.dsl.one2many.metadb.one2many_h2
import docs.H2Tests
import molecule.core.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object InvoiceTest extends H2Tests {

  override lazy val tests = Tests {

    "invoice" - h2(one2many_h2()) {

      //  // Invoice lines typically inserted with each invoice
      val ids = Invoice.no.Lines.*(InvoiceLine.amount).insert(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      ).transact.ids

      // Nested retrieval, invoice-to-lines
      // "Invoice number with list of line amounts"
      Invoice.no.Lines.*(InvoiceLine.amount).query.i.get ==> List(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      )

      // Flat retrieval, invoice-to-line
      Invoice.no.Lines.amount.query.get ==> List(
        (1, 10),
        (1, 20),
        (1, 30),
        (2, 20),
        (2, 70),
      )


      // Line amount to invoice number pairs
      InvoiceLine.amount.Invoice.no.query.i.get ==> List(
        (10, 1),
        (20, 1),
        (20, 2),
        (30, 1),
        (70, 2),
      )

      Invoice(ids.head).delete.transact

      Invoice.no.Lines.*(InvoiceLine.amount).query.i.get ==> List(
        (2, List(20, 70)),
      )
    }
  }
}
