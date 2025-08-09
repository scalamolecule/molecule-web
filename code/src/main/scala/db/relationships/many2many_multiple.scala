package db.relationships

import molecule.DomainStructure

object many2many_multiple extends DomainStructure {

  trait Employee {
    val name      = oneString
    val frontends = many[Project].designers
    val backends  = many[Project]
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
    val designers = many[Employee]
    val engineers = many[Employee]("hej").backends
  }

  trait Person {
    val spouse = one[Person]
  }
}