package db.dataModel

import molecule.DomainStructure

object Gaming extends DomainStructure {

  trait Gamer {
    val category = oneString
    val rank     = oneInt.unique
    val score    = oneInt
    val username = oneString.unique
    val name     = oneString

  }
}