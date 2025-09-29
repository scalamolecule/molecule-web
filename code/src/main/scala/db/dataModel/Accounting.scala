package db.dataModel

import molecule.DomainStructure

object Accounting extends DomainStructure {

  trait Customer {
    val name = oneString
  }

  trait Invoice {
    val customer = manyToOne[Customer]
    val no       = oneInt
    val date     = oneLocalDate
    val total    = oneInt
  }

  trait InvoiceLine {
    val invoice = manyToOne[Invoice].oneToMany("Lines").owner
    val product = oneString
    val amount  = oneInt
  }
}