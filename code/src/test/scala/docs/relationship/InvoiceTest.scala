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
      Invoice.no.Lines.*(InvoiceLine.amount).insert(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      ).transact

      // Nested retrieval, invoice-to-lines
      // "Invoice number with list of line amounts"
      Invoice.no.Lines.*(InvoiceLine.amount).query.i.get ==> List(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      )

      // Flat retrieval, invoice-to-line
      // "Invoice number to line amount pairs

      //         OneToMany
      Invoice.no.Lines.amount.query.get ==> List(
        (1, 10),
        (1, 20),
        (1, 30),
        (2, 20),
        (2, 70),
      )

      rawQuery(
        """
          |SELECT DISTINCT
          |  InvoiceLine.amount,
          |  Invoice.no
          |FROM Invoice
          |  INNER JOIN Invoice_lines_InvoiceLine ON
          |    InvoiceLine.id = Invoice_lines_InvoiceLine.InvoiceLine_id
          |  INNER JOIN InvoiceLine ON
          |    Invoice_lines_InvoiceLine.Invoice_id = Invoice.id
          |WHERE
          |  Invoice.no         IS NOT NULL AND
          |  InvoiceLine.amount IS NOT NULL;
          |""".stripMargin, true
      )

      //                 ManyToOne
//      InvoiceLine.amount.Invoice.?(Invoice.no).query.i.get ==> List()

      // Flat retrieval, line-to-invoice
      // Line amount to invoice number pairs
      InvoiceLine.amount.Invoice.no.query.i.get ==> List(
        (10, 1),
        (20, 1),
        (20, 2),
        (30, 1),
        (70, 2),
      )

//      // Nested retrieval, line-to-invoices
//      // "Line amount with list of invoice numbers"
//      InvoiceLine.amount.Invoice.*(Invoice.no).query.get ==> List(
//        (10, List(1)),
//        (20, List(1, 2)),
//        (30, List(1)),
//        (70, List(2)),
//      )
      //
      //        // This would semantically wrong...
      //        InvoiceLine.amount.Invoice.*(Invoice.no).insert(
      //          (10, List(1)),
      //          (20, List(1, 2)),
      //          (30, List(1)),
      //          (70, List(2)),
      //        ).transact
    }
  }
}