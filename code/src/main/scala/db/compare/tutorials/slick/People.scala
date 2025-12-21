package db.compare.tutorials.slick

import molecule.DomainStructure

trait People extends DomainStructure {

  trait Person {
    val name    = oneString
    val age     = oneInt
    val address = manyToOne[Address]
  }

  trait Address {
    val street = oneString
    val city   = oneString
  }
}