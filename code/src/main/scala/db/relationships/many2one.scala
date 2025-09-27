package db.relationships

import molecule.DomainStructure

object many2one extends DomainStructure {

  // many-side initiates believing in a God
  trait Worshipper {
    val name = oneString
    val god  = manyToOne[God] // defining side
  }

  // one-side
  trait God {
    val name        = oneString
//    val worshippers = many[Worshipper] // reverse side
  }
}