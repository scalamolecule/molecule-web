package db.relationships

import db.dataModel.Accounting.InvoiceLine
import molecule.DomainStructure

object one2many extends DomainStructure {

  // one-side initiates one-to-many relationship
  trait Invoice {
    val no    = oneInt
//    val lines = many[InvoiceLine].invoice
  }

  // many-side
  trait InvoiceLine {
    val invoice = one[Invoice]
    val amount  = oneInt
  }
}

/*

For any one-to-many/many-to-one relationship Molecule could enforce the domain structure definition to include both sides of the relationship.
That would allow Molecule to generate boilerplate code to enable querying data in both directions as both flat and nested retrieval:

object one2many extends DomainStructure {

  // one-side initiates one-to-many relationship
  trait Invoice {
    val no    = oneInt
    val lines = many[InvoiceLine].invoice // defines what attribute in InvoicLine that will join back to Invoice
  }

  // many-side
  trait InvoiceLine {
    val invoice = _one[Invoice] // Maybe starting with underscore to indicate that this is the "reverse" ref attribute? And not "the defining" ref attribute of a relationship.
    val amount  = oneInt
  }

  // Invoice lines typically inserted with each invoice
  Invoice.no.Lines.*(InvoiceLines.amount).insert(
    (1, List(10, 20, 30)),
    (2, List(20, 70)),
  ).transact

  // Nested retrieval, invoice-to-lines
  // "Invoice number with list of line amounts"
  Invoice.no.Lines.*(InvoiceLine.amount).query.get ==> List(
    (1, List(10, 20, 30)),
    (2, List(20, 70)),
  ).transact

  // Flat retrieval, invoice-to-line
  // "Invoice number to line amount pairs
  Invoice.no.Lines.amount.query.get ==> List(
    (1, 10),
    (1, 20),
    (1, 30),
    (2, 20),
    (2, 70),
  ).transact

  // Flat retrieval, line-to-invoice
  // Line amount to invoice number pairs
  InvoiceLine.amount.Invoice.no.query.get ==> List(
    (10, 1),
    (20, 1),
    (20, 2),
    (30, 1),
    (70, 2),
  )

  // Nested retrieval, line-to-invoices
  // "Line amount with list of invoice numbers" - to find invoices by line amounts
  InvoiceLine.amount.Lines.*(InvoiceLine.amount).query.get ==> List(
    (10, List(1)),
    (20, List(1, 2)),
    (30, List(1)),
    (70, List(2)),
  ).transact
}

Wouldn't this make semantic sense and be useful?

Can you make sql queries for each of these queries so that we can see and evaluate the generated sql?


As a bonus feature, we can also add an `owner` keyword to the one-side of the relationship:

val lines = many[InvoiceLine].invoice.owner

and thereby have the application logic delete all invoice lines if an invoice is deleted.

*/

//object one2many extends DomainStructure {
//
//  // one-side initiates one-to-many relationship
//
//  // In this case it is even stronger with the added `owner` keyword
//  // forcing invoice lines to be deleted if an invoice is deleted.
//  trait Invoice {
//    val no    = oneInt
//    val date  = oneLocalDate
//    val lines = many[InvoiceLine].invoice.owner
//    val total = oneInt
//  }
//
//  // many-side
//  trait InvoiceLine {
//    val invoice   = one[Invoice]
//    val qty       = oneInt
//    val product   = oneString
//    val unitPrice = oneInt
//    val lineTotal = oneInt
//  }
//}