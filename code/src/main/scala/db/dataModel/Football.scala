package db.dataModel

import molecule.DomainStructure

object Football extends DomainStructure {

  // https://www.whoscored.com/Statistics

  trait Team {
    val name         = oneString
    val goalsToBonus = oneInt
    val bonus        = oneInt
//    val players      = many[Player]
  }

  trait Player {
    val name    = oneString
    val goals   = oneInt
    val assists = oneInt
    val team    = manyToOne[Team]
  }
}