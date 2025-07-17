package db.dataModel

import molecule.DomainStructure

object School extends DomainStructure {

  trait Teacher {
    val name    = oneString
    val classes = many[Class]
  }

  trait Class {
    val subject  = oneString
    val students = many[Student]
  }

  trait Student {
    val name = oneString
    val age  = oneInt
  }
}