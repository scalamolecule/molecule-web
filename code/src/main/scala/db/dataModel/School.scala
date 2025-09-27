package db.dataModel

import molecule.DomainStructure

object School extends DomainStructure {

  trait Teacher {
    val name = oneString
  }

  trait Course {
    val subject = oneString
  }

  trait Student {
    val name = oneString
    val age  = oneInt
  }

  trait Attendance extends Join {
    val teacher   = manyToOne[Teacher]
    val course    = manyToOne[Course]
    val student   = manyToOne[Student]
    val startYear = oneInt
  }
}