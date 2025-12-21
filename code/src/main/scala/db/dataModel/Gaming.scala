package db.dataModel

import molecule.DomainStructure

trait Gaming extends DomainStructure {

  trait Gamer {
    val category = oneString
    val rank     = oneInt.unique
    val score    = oneInt
    val username = oneString.unique
    val name     = oneString

  }
}