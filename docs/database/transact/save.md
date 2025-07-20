---
prev: /database/setup/db-setup
---

# Save

Save an entity by applying data to attributes of a molecule and call `save.transact` on it:

```scala
Person.name("Bob").age(42).save.transact

Person.name.age.query.get.head ==> ("Bob", 42)
```

## Optional attr

Use optional attributes for dynamic optional data

```scala
val optName = Some("Bob") // or None
val optAge  = Some(42) // or None
Person.name_?(optName).age_?(optAge).save.transact

Person.name.age.query.get.head ==> ("Bob", 42)
```



## Collection attr

Collection attributes can be saved with a `Set`, `Seq` and `Map` or any subtype of those:

::: code-tabs#collection
@tab Set
```scala
// Main collection type
Person.hobbies(Set("stamps", "trains")).save.transact

// Collection subtypes
Person.hobbies(ListSet("stamps", "trains")).save.transact
Person.hobbies(TreeSet("stamps", "trains")).save.transact
// etc
```

@tab Seq
```scala
// Main collection type
Person.scores(Seq(1, 2, 3)).save.transact

// Collection subtypes
Person.scores(List(1, 2, 3)).save.transact
Person.scores(Vector(1, 2, 3)).save.transact
```

@tab Map
```scala
// Main collection type
Person.langNames(Map("en" -> "Bob")).save.transact

// Collection subtypes
Person.langNames(TreeMap("en" -> "Bob")).save.transact
Person.langNames(VectorMap("en" -> "Bob")).save.transact
```
:::


## Ref

Additional referenced data can be added, and Molecule will transparently create the relationship:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17").save.transact

Person.name.age
  .Home.street.query.get.head ==> 
  ("Bob", 42, "Main st. 17")
```

Save multiple relationships:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17")
  .Country.name("USA")
  .save.transact

Person.name.age
  .Home.street
  .Country.name.query.get.head ==>
  ("Bob", 42, "Main st. 17", "USA")
```

Or use [backrefs](/database/query/relationships#backref):

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 17")._Person
  .Education.shortName("Harvard")
  .save.transact

Person.name.age
  .Home.street._Person
  .Education.shortName.query.get.head ==>
  ("Bob", 42, "Main st. 17", "Harvard")
```


## Ref attr
Country is likely a separate entity that we don't want to create for each new address. In that case we can instead obtain the country id and add it to the ref attribute `home` to establish the relationship from Address to Country:

```scala
val usaId = Country.id.name_("USA").query.get.head

Person.name("Bob").age(42)
  .Home.street("Main st. 17")
  .country(usaId) // save country id with ref attr
  .save.transact

Person.name.age
  .Home.street
  .Country.name.query.get.head ==>
  ("Bob", 42, "Main st. 17", "USA")
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Save action compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/action/save)