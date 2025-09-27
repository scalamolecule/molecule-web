package db.dataModel

import molecule.DomainStructure

object Pets extends DomainStructure {
//  val friends       = many[Person]
//  val companionPets = many[Pet]


  trait Person {
    val name       = oneString
//    val pets       = many[Pet].owner
//    val companions = many[Pet].companion
  }

  trait Pet {
    val name      = oneString
    val owner     = manyToOne[Person]
    val companion = manyToOne[Person].oneToMany("Companions")
  }


//  // query Pet name and its owner name
//  Pet.name.Owner.name
//  // (Rex, John)
//  // (Vuf, John)
//
//  // query Pet name and its owner name
//  Person.name.Pets.name
//  // (John, Rex)
//  // (John, Vuf)
//
//  // Or nested
//  Person.name.Pets.*(Pet.name)
//  // (John, List(Rex, Vuf))

}