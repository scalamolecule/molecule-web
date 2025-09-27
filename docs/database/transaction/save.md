---
prev: /database/setup/db-setup
---

# Save

Save an entity by applying data to attributes of a molecule and call `save.transact` on it:

```scala
Person.name("Bob").age(42).save.transact
```

Query the saved data:
```scala
Person.name.age.query.get.head ==> ("Bob", 42)
```


## TxReport
A `TxReport` containing the id of the saved row is returned:

::: code-tabs#coord
@tab Sync
```scala
import molecule.db.postgres.sync.*
       
val txReport: TxReport = 
  Person.name("Bob").age(42).save.transact
```

@tab Async
```scala
import molecule.db.postgres.async.*

val txReport: Future[TxReport] =
  Person.name("Bob").age(42).save.transact
```

@tab ZIO
```scala
import molecule.db.postgres.zio.*

val txReport: ZIO[Conn, MoleculeError, TxReport] =
  Person.name("Bob").age(42).save.transact
```

@tab IO
```scala
import molecule.db.postgres.io.*

val txReport: cats.effect.IO[TxReport] =
  Person.name("Bob").age(42).save.transact
```
:::


`TxReport` is a simple container of ids affected by a transaction.

```scala
case class TxReport(ids: List[Long]) {
  // Convenience method when we expect a single id
  def id: Long = ids.head
}
```

So we can get the id of the saved row through the txReport:

```scala
txReport.id ==> 1L
// or directly
Person.name("Bob").age(42).save.transact.id ==> 1L
```

## Optional attribute

Use optional attributes with `_?` suffix for dynamic optional data

```scala
val optName = Some("Bob")
val optAge  = None
Person.name_?(optName).age_?(optAge).save.transact

Person.name.age_?.query.get.head ==> ("Bob", None)
```



## Collection attributes

Collection attributes can be saved with a `Set`, `Seq` and `Map` or any subtype of those:

::: code-tabs#collection
@tab Set
```scala
// Main collection type
Person.hobbies(Set("stamps", "trains")).save.transact

// Set subtypes
Person.hobbies(ListSet("stamps", "trains")).save.transact
Person.hobbies(TreeSet("stamps", "trains")).save.transact
// etc
```

@tab Seq
```scala
// Main collection type
Person.scores(Seq(1, 2, 2)).save.transact

// Seq subtypes
Person.scores(List(1, 2, 2)).save.transact
Person.scores(Vector(1, 2, 2)).save.transact
// etc
```

@tab Map
```scala
// Main collection type
Person.langNames(Map("en" -> "Hello", "es" -> "Hola")).save.transact

// Map subtypes
Person.langNames(TreeMap("en" -> "Hello", "es" -> "Hola")).save.transact
Person.langNames(VectorMap("en" -> "Hello", "es" -> "Hola")).save.transact
// etc
```
:::


## Relationships

Additional related data can be added, and Molecule will transparently create the relationship by inserting the Address and add the address id as a foreign key to the Person row:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17").save.transact

Person.name.age
  .Home.street.query.get.head ==> 
  ("Bob", 42, "Main st. 17")
```

Even though we would likely save Countries separately in a database, for tests it can be valuable to be able to create a chain of relationships:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17")
  .Country.name("USA")
  .save.transact

Person.name.age
  .Home.street
  .Country.name
  .query.get.head ==>
  ("Bob", 42, "Main st. 17", "USA")
```

## Stepping back

We can use `_Person` to "step back" to `Person` and save additional relationships or "branches" from `Person` to other entities:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17") // add relationship
  ._Person // step back to Person 
  .Education.shortName("Harvard") // add another relationship from Person
  .save.transact

// Likewise we can query multiple relationships from Person:
Person.name.age
  .Home.street._Person
  .Education.shortName
  .query.get.head ==>
  ("Bob", 42, "Main st. 17", "Harvard")
```


## Foreign key
As mentioned above, `Country` is likely a separate entity that we don't want to create for each new address. In that case we can instead get the country id and apply it to the lower-case named foreign key attribute `country`:

```scala
val usaId = Country.id.name_("USA").query.get.head

Person.name("Bob").age(42)
  .Home.street("Main st. 17")
  .country(usaId) // save country id `usaId` as a foreign key
  .save.transact

Person.name.age
  .Home.street
  .Country.name
  .query.get.head ==>
  ("Bob", 42, "Main st. 17", "USA")
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Save action compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/action/save)