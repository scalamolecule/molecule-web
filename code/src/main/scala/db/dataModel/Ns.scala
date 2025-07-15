package db.dataModel

import molecule.DomainStructure

object Ns extends DomainStructure {

  trait Ns {
    val s = oneString
    val int  = oneInt
  }
}