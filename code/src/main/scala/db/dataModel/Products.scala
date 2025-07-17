package db.dataModel

import molecule.DomainStructure

object Products extends DomainStructure {

  trait Product {
    val name  = oneString
    val price = oneInt
    val stars = oneInt
    val team  = one[Category]
  }

  trait Category {
    val name     = oneString
    val ordering = oneInt
    val products = many[Product]
  }
}