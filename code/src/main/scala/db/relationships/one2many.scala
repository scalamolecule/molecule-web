package db.relationships

import db.dataModel.Accounting.InvoiceLine
import molecule.DomainStructure

@deprecated
object one2many extends DomainStructure {

  trait Invoice {
    val no = oneInt
  }

  trait InvoiceLine {
    val invoice = manyToOne[Invoice].oneToMany("Lines").owner
    val product = oneString
    val amount  = oneInt
  }
}
