package db.dataModel

import molecule.DomainStructure

object Person extends DomainStructure {

  trait Person {
    val name      = oneString
    val firstName = oneString
    val lastName  = oneString
    val age       = oneInt
    val member    = oneBoolean
    val likes     = oneString
    val home      = manyToOne[Address]
    val education = manyToOne[University]

    // Collection types
    val hobbies   = setString
    val scores    = seqInt
    val langNames = mapString
  }

  trait Address {
    val street  = oneString
    val zip     = oneInt
    val country = manyToOne[Country]
    val stats   = manyToOne[Stats]
  }

  trait Country {
    val name = oneString
  }

  trait University {
    val shortName = oneString
    val zip       = oneInt
    val state     = manyToOne[State]
  }

  trait State {
    val abbr = oneString
  }

  trait Stats {
    val crimeRate = oneString
  }
}