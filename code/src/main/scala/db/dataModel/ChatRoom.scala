package db.dataModel

import molecule.DomainStructure

trait ChatRoom extends DomainStructure {

  trait Post {
    val user    = oneString
    val comment = oneString
    val note    = oneString
  }
}