package db.compare.tutorials.slick

import molecule.DomainStructure

object People extends DomainStructure {

  trait Person {
    val name    = oneString
    val age     = oneInt
    val address = one[Address]
  }

  trait Address {
    val street = oneString
    val city   = oneString
  }
}