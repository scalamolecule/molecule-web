package db.dataModel

import molecule.DomainStructure

object ChatRoom extends DomainStructure {

  trait Post {
    val user    = oneString
    val comment = oneString
    val note    = oneString
  }
}