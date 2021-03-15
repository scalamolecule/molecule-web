---
title: "Overview"
weight: 10
menu:
  main:
    parent: code
---

# Molecule Code overview

On this page we'll quickly get an intuitive overview of how Molecule queries and transactions look like.

Then the following pages in the side menu will explain in more detail.


## Db connection

An implicit connection to a database is needed to create and execute molecule queries.

Here's an example of importing the molecule api and setting up the Datomic Peer in-memory database:

```scala
import molecule.datomic.api._
import molecule.datomic.peer.facade.Datomic_Peer._

implicit val conn = recreateDbFrom(SomeSchema)
```
[Setup](/setup) describes the various database setups that can be used with molecules.


## Retrieve tuples/objects

Data is retrieved by building molecules of attributes from a namespace. Calling `get` on a molecule will fetch typed data from the database that match the molecule:

```scala
val names           : List[String]                 = Person.name.get
val namesAndAges    : List[(String, Int)]          = Person.name.age.get
val namesAgesMembers: List[(String, Int, Boolean)] = Person.name.age.isMember.get
// etc..
```

Data can also be returned as an object for each row of data that has properties matching the attributes of the molecule:
```scala
// Single row/object
val ben = Person.name_("Ben").age.gender.Address.street.City.name.getObj
ben.age === 23
ben.gender === "male"
ben.Address.street === "Broadway"
ben.Address.City.name === "New York"

// Multiple rows/objects
Person.name.age.Address.street.City.name.getObjList.foreach { person =>
  println(
    s"${person.name} is ${person.age} yeas old and lives on " +
            s"${person.Address.street}, ${person.Address.City.name}"
  )
  // "Ben is 23 years old and lives on Broadway, New York"
  // "Lisa is ..." etc...
}
```





## Sync/Async APIs

All commands in Molecule can be synchronous or asynchronous:

```scala
// sync 
val list: List[(String, Int)] = Person.name.age.get 

// async
val listAsync: Future[List[(String, Int)]] = Person.name.age.getAsync

saveAsync
insertAsync
updateAsync
retractAsync
```




## "CRUD"



In Datomic, data is not deleted but instead "retracted" since all changes are accumulated. That makes it possible to go back and see _what data_ was retracted and is no longer current. That's why we say "CRUD" instead of CRUD.

```scala
// Save populated molecule
Person.name("John").likes("pizza").age(24).save

// Insert multiple tuples of data using a molecule template
Person.name.age.likes insert List(
  ("John", 24, "pizza"),
  ("Lisa", 20, "sushi")
)

// Update one or more attributes of a given entity id
Person(johnId).age(25).likes("thai").update

// Retract ("delete") entity
johnId.retract

// Retract attribute value
Person(johnId).likes().update
```



## Attribute types

### Card-one attributes
`name` here is a card-one attribute with a single value
```scala
Person.name.get.head === "Bob"
```

### Card-many attributes
`interests` here is a card-many attribute with a Set of distinct values
```scala
Person.name.interests.get === List(
  "Bob", Set("Baseball", "Origami"),
  "Liz", Set("Painting", "Traveling", "Tae Kwondo")
)
```

### Map attributes
Keyed card-many attributes, or "Map attributes", are useful for i18n for instance
```scala
Phrases.greeting("en" -> "hello", "de" -> "hallo").save
Phrases.greeting("en" -> "hello").get.head === Map("en" -> "hello")
Phrases.greeting.k("de").get.head          === Map("de" -> "hallo")
```


### Mandatory, Optional, Tacit

Attributes can be

- a _mandatory_ value,
- an _optional_ value (`$` appended), or
- a _tacit_ value ( `_` appended) that is mandatory but not returned:

```scala
// name is mandatory
// age$ is optional
// isMember_ is mandatory but not returned (tacit)
val membersWithOptionalAge: List[(String, Option[Int])] = Person.name.age$.isMember_.get
```


### Attribute data types

```
Cardinality one             Cardinality many                 Mapped cardinality many
-------------------         -------------------------        --------------------------------
oneString    : String       manyString    : Set[String]      mapString    : Map[String, String]
oneInt       : Int          manyInt       : Set[Int]         mapInt       : Map[String, Int]
oneLong      : Long         manyLong      : Set[Long]        mapLong      : Map[String, Long]
oneFloat     : Float        manyFloat     : Set[Float]       mapFloat     : Map[String, Float]
oneDouble    : Double       manyDouble    : Set[Double]      mapDouble    : Map[String, Double]
oneBigInt    : BigInt       manyBigInt    : Set[BigInt]      mapBigInt    : Map[String, BigInt]
oneBigDecimal: BigDecimal   manyBigDecimal: Set[BigDecimal]  mapBigDecimal: Map[String, BigDecimal]
oneBoolean   : Boolean      manyBoolean   : Set[Boolean]     mapBoolean   : Map[String, Boolean]
oneDate      : Date         manyDate      : Set[Date]        mapDate      : Map[String, Date]
oneUUID      : UUID         manyUUID      : Set[UUID]        mapUUID      : Map[String, UUID]
oneURI       : URI          manyURI       : Set[URI]         mapURI       : Map[String, URI]
oneEnum      : String       manyEnum      : Set[String]
```





## Attribute values

### Expressions

```scala
// equality
Person.age(42)

// negation
Person.age.not(42) // or
Person.age.!(42) 

// comparison
Person.age.>(42)
Person.age.>=(42)
Person.age.<(42)
Person.age.<=(42)

// null
Person.age() // or
Person.age(Nil)

// Word search
Person.comment.contains("nice")
```

### Aggregates

```scala
Person.age(count)
Person.age(countDistinct)
Person.age(distinct)
Person.age(max)
Person.age(min)
Person.age(rand)
Person.age(sample)
Person.age(avg)
Person.age(median)
Person.age(stddev)
Person.age(sum)
Person.age(variance)
```

### Logic

OR
```scala
Person.age(42 or 43) // same as
Person.age(42, 43)   // same as
Person.age(List(42, 43))
```

AND: card-many attribute `category` has both a "restaurants" and a "shopping" value:
```scala
Community.name.category_("restaurants" and "shopping").get === List("Ballard Gossip Girl")
```






## Input molecules

"Input molecules" awaits 1, 2 or 3 input values. Useful for re-use
```scala
val personsOfAge = m(Person.name.age_(?))

personsOfAge(23).get === List("Bob")
personsOfAge(24).get === List("Liz", "Don")
```

2 inputs + logic (and relationships)
```scala
val typeAndRegion = m(Community.name.type_(?).Neighborhood.District.region_(?))

// Social media communities in southern districts
typeAndRegion(("twitter" or "facebook_page") and ("sw" or "s" or "se"))
```





## Relationships

### Card-one
```scala
Person.name.age.Address.street.get === List(
  ("Bob", 23, "5th Avenue") 
) 
```

### Card-many
```scala
// flat
Invoice.no.InvoiceLines.item.get === List(
  (42, "coffee"),
  (42, "sugar")
)

// nested
Invoice.no.InvoiceLines.*(InvoiceLine.item).get === List(
  (42, List("coffee", "sugar"))
)
```

### Self-join

Relationship to the same Namespace type (Person -> Person)
```scala
Person.name.Spouse.name.get.head === ("Bob", "Liz")
```

### Directional

Relationships can be defined to go in both directions so that we can traverse a graph uniformly:
```scala
Person.name.Knows.name.Knows.name.get === List(
  ("Bob", "Liz", "Dan"),
  ("Dan", "Liz", "Bob")
  // etc...
)
```

### Associative

Attributes from different Namespaces that are not explicitly related can be _associated_ by sharing the same entity id. We call molecules with associative relationships "Composite molecules":
```scala
m(Person.name("Bob") + Bar.status("regular")).save
m(Person.name + Bar.status).get.head === ("Bob", "regular")
```






## Tx Bundle

Transaction bundles can atomically transact multiple operations/statements in one transaction:
```scala
transact(
  // retract entity
  e1.getRetractStmts,
  // save new entity
  Ns.int(4).getSaveStmts,
  // insert multiple new entities
  Ns.int.getInsertStmts(List(5, 6)),
  // update entity
  Ns(e2).int(20).getUpdateStmts
)
```







## Tx meta data

Add meta data to the transaction entity itself about the transaction:
```scala
Person.name("John").likes("pizza").Tx(Audit.user("Lisa").uc("survey")).save
```

We can then query for specific tx meta data
```scala
// How was John added?
// John was added by Lisa as part of a survey
Person(johnId).name.Tx(Audit.user.uc).get === List(("John", "Lisa", "survey"))

// When did Lisa survey John?
Person(johnId).name_.txInstant.Tx(Audit.user_("Lisa").uc_("survey")).get.head === dateX
  
// Who were surveyed?  
Person.name.Tx(Audit.uc_("survey")).get === List("John")

// What did people that Lisa surveyed like? 
Person.likes.Tx(Audit.user_("Lisa").uc_("survey")).get === List("pizza")

// etc..
```








## Tx function

Ensure transactional atomicity with tx functions that run within a single transaction. If any part of the function throws an exception, the whole transaction is aborted.

Here's a money transfer function where we want to be sure that both accounts are updated correctly:
```scala
// Pass in entity ids of from/to accounts and the amount to be transferred
def transfer(from: Long, to: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
  // Validate sufficient funds in from-account
  val curFromBalance = Account(from).balance.get.headOption.getOrElse(0)
  if (curFromBalance < amount)
    throw new TxFnException(
      s"Can't transfer $amount from account $from having a balance of only $curFromBalance.")

  // Calculate new balances
  val newFromBalance = curFromBalance - amount
  val newToBalance = Account(to).balance.get.headOption.getOrElse(0) + amount

  // Update accounts
  Account(from).balance(newFromBalance).getUpdateStmts ++ Account(to).balance(newToBalance).getUpdateStmts
}
```
We then call the transaction function inside a `transact` method:
```scala
transact(transfer(fromAccount, toAccount, okAmount))
```







## Time

Datomic has powerful ways of accessing all the immutable data that accumulates over time in the database:

![](/img/page/time/all.png)

```scala
// Current data
Person.name.age.get

// As of some point in time - how did it look at that time?
Person.name.age.getAsOf(nov5date)

// Since some point in time - what has happened after this time?
Person.name.age.getSince(nov5date)

// History of all name operations - what names were added and retracted?
Person.name.getHistory

// Test what-if scenarios given some test statements - how will it look if we do x?
Person.name.getWith(someTestStmts)
```






## Generic APIs

Molecule provides access to Datomic's various generic interfaces and apis.


### Entity API

`touch` an entity id to get all it's attribute values
```scala
orderId.touch === Map(
  ":db/id" -> orderId,
  ":Order/lineItems" -> List(
    Map(
      ":db/id" -> 102L, 
      ":LineItem/qty" -> 3, 
      ":LineItem/product" -> "Milk",
      ":LineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":LineItem/qty" -> 2, 
      ":LineItem/product" -> "Coffee",
      ":LineItem/price" -> 46.0)))
```

### Datom

Retrieve generic data about entities and attributes:

```scala
// Entity id of Ben with generic Datom attribute `e`
Person.e.name.get.head === (benEntityId, "Ben")

// When was the Ben's age last changed?
Person.age.txInstant.get.head === (24, <April 4>) // (Date)

// etc...
```


### Indexes
#### EAVT

Attributes and values of entity e1
```scala
EAVT(e1).a.v.get === List(
  (":Person/name", "Ben"),
  (":Person/age", 25),
  (":Golf/score", 5.7)
)
``` 

#### AVET

Values, entities and transactions where attribute `:Person/age` is involved
```scala
AVET(":Person/age").e.v.t.get === List(
  (25, e1, t2),
  (23, e2, t5)
  (14, e3, t7),
)

// AVET index filtered with an attribute name and a range of values
AVET.range(":Person/age", Some(14), Some(24)).v.e.t.get === List(
  (14, e4, t7),
  (23, e2, t5)
)
``` 

#### AEVT

Entities, values and transactions where attribute `:Person/name` is involved
```scala
AEVT(":Person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)
``` 

#### VAET

Get entities pointing to entity a1
```scala
VAET(a1).v.a.e.get === List(
  (a1, ":Release/artists", r1),
  (a1, ":Release/artists", r2),
  (a1, ":Release/artists", r3),
)
``` 

### Log

Data from transaction `t1` until `t4` (exclusive)
```scala
Log(Some(t1), Some(t4)).t.e.a.v.op.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 25, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 23, true),

  (t3, e1, ":Person/age", 25, false),
  (t3, e1, ":Person/age", 26, true)
)
``` 

### Schema

Get information about the Datomic schema which is also a way to access your Data Model programatically.
```scala
Schema.part.ns.attr.fulltext$.doc.get === List(
  ("ind", "Person", "name", Some(true), "Person name"), // fulltext search enabled
  ("ind", "Person", "age", None, "Person age"),
  ("cat", "Sport", "name", None, "Sport category name")
)
``` 


### Next

[Attributes...](/code/attributes/)