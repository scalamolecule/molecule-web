package db.relationships

import db.dataModel.Accounting.InvoiceLine
import molecule.DomainStructure

object one2many extends DomainStructure {

  // one-side initiates one-to-many relationship
  trait Invoice {
    val no = oneInt
  }

  // many-side
  trait InvoiceLine {
    val invoice = manyToOne[Invoice].oneToMany("Lines").owner
    val amount  = oneInt
  }
}
