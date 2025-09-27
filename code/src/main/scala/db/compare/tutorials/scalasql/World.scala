package db.compare.tutorials.scalasql

import molecule.DomainStructure

object World extends DomainStructure {

  trait Country {
    val code           = oneString
    val name           = oneString
    val continent      = oneString
    val region         = oneString
    val surfaceArea    = oneInt
    val indepYear      = oneInt
    val population     = oneLong
    val lifeExpectancy = oneDouble
    val gnp            = oneBigDecimal
    val gnpOld         = oneBigDecimal
    val localName      = oneString
    val governmentForm = oneString
    val headOfState    = oneString
    val capital        = manyToOne[City]
    val code2          = oneString
  }

  trait City {
    val country     = manyToOne[Country]
    val countryCode = oneString
    val name        = oneString
    val district    = oneString
    val population  = oneLong
  }

  trait CountryLanguage {
    val country     = manyToOne[Country]
    val countryCode = oneString
    val language    = oneString
    val isOfficial  = oneBoolean
    val percentage  = oneDouble
  }
}