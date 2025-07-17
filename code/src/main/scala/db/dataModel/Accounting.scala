package db.dataModel

import molecule.DomainStructure

object Accounting extends DomainStructure {

  trait Invoice {
    val no    = oneInt
    val date  = oneLocalDate
    val lines = many[InvoiceLine].owner
    val total = oneInt
  }

  trait InvoiceLine {
    val qty       = oneInt
    val product   = oneString
    val unitPrice = oneInt
    val lineTotal = oneInt
  }
}