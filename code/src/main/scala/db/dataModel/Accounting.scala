package db.dataModel

import molecule.DomainStructure

object Accounting extends DomainStructure {

  trait Invoice {
    val no    = oneInt
    val date  = oneLocalDate
    val total = oneInt
  }

  trait InvoiceLine {
    val invoice   = manyToOne[Invoice].oneToMany("Lines").owner
    val qty       = oneInt
    val product   = oneString
    val unitPrice = oneInt
    val lineTotal = oneInt
  }
}