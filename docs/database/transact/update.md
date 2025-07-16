# Update

One or more entities can be updated with a molecule populated with the new data.

Entities can be identified either by applying ids or by the shape of the molecule with its relationships and tacit filters as we'll see below.

## Id

Update an entity with an id and new data applied to attributes:

```scala
Person(bobId).age(43).update.transact
```

Id's can be retrieved with the special `id` attribute that Molecule manages. For SQL tables it's an auto-incremented `Long`.

So a more full variant of a typical workflow for our example could look this:

```scala
// Current data: Bob is 42
Person.age.query.get.head ==> 42

// Retrieve id to be updated
val bobId = Person.id.name_("Bob").query.get.head

// Update Bob's age to 43 using his id
Person(bobId).age(43).update.transact

// Bob is now 43
Person.age.query.get.head ==> 43
```

## Ids

Multiple entities can be updated with multiple ids:

```scala
// Update Bob and Joe
Person(bobId, benId).age(43).update.transact

// Bob and Joe are now 43
Person.name.age.query.get ==> List(
  ("Bob", 43),
  ("Joe", 43),
)
```

Instead of applying ids as varargs, we can also use a `Seq` of ids

```scala
val ids = Person.id.name_.query.get
Person(ids).age(43).update.transact
```

## Filters

Another way to update multiple entities is to filter by tacit attributes. One or more filter attributes can be used to narrow the selection of entities to be updated.

> Update filters are similar to query filters but have to be tacit only for updates to leave mandatory attributes carrying the new values.

Let's use the following sample data set to illustrate the various update filter possibilities:

```scala
Person.firstName_?.lastName.age.insert(
  (Some("Liz"), "Carson", 27),
  (Some("Bob"), "Monroe", 42),
  (Some("Bob"), "Sponge", 35),
  (None, "Wilson", 72), // (no firstName set)
).transact
```

### Not null

The most wide filter is to update all entities having any value set for a tacit filter attribute `attr_`.

Here we update `age` of all persons having a `firstName` set:

```scala
// Update age to 43 for anyone with a firstName
Person.age(43).firstName_.update.transact

// All ages set to 43
Person.firstName_?.lastName.age.query.get ==> List(
  (Some("Liz"), "Carson", 43), // updated
  (Some("Bob"), "Monroe", 43), // updated
  (Some("Bob"), "Sponge", 43), // updated
  (None, "Wilson", 72), // Not updated (firstName not asserted)
)
```

### Null

Likewise, we can update entities with a missing value (null) by applying nothing to a tacit attribute `attr_()`.

Here we update `age` of all persons having a `firstName` set:

```scala
// Update age to 43 for anyone with a firstName
Person.age(43).firstName_.update.transact

// All ages set to 43
Person.firstName_?.lastName.age.query.get ==> List(
  (Some("Liz"), "Carson", 43), // updated
  (Some("Bob"), "Monroe", 43), // updated
  (Some("Bob"), "Sponge", 43), // updated
  (None, "Wilson", 72), // Not updated (firstName not asserted)
)
```

### Equality

A more narrow filter is to apply a value to a tacit attribute, like updating all people with "Bob" as their first name:

```scala
// For all with firstName "Bob", set age to 43
Person.firstName_("Bob").age(43).update.transact

// All "Bob" ages set to 43
Person.firstName.lastName.age.query.get ==> List(
  ("Liz", "Carson", 27),
  ("Bob", "Monroe", 43), // updated
  ("Bob", "Sponge", 43), // updated
)
```

You can use the same attribute for both filtering and supplying a new value:

```scala
// For all aged 42, set age to 43
Person.age_(42).age(43).update.transact

// All aged 42 are now 43
Person.firstName.lastName.age.query.get ==> List(
  ("Liz", "Carson", 27),
  ("Bob", "Monroe", 43), // updated
  ("Bob", "Sponge", 35),
)
```

It's not allowed (or meaningful) to update multiple values for a cardinality-one attribute:

```scala
intercept[ModelError](
  Person.firstName_("Bob").age(43, 44).update.transact
).msg ==> "Can only update one value for attribute `Person.age`. Found: 43, 44"
```

### OR

Filter by applying multiple values:

```scala
// Update age to 43 for lastNames "Monroe" or "Wilson"
Person.age(43).lastName_("Monroe", "Wilson").update.transact

// All with last name Monroe OR Wilson now 43 years old
Person.firstName_?.lastName.age.query.get ==> List(
  (Some("Liz"), "Carson", 27),
  (Some("Bob"), "Monroe", 43), // updated
  (Some("Bob"), "Sponge", 35),
  (None, "Wilson", 43), // updated
)
```

A dynamic list of filter values can be applied too with the same result:

```scala
val filterValues = List("Monroe", "Wilson")
Person.age(43).lastName_(filterValues).update.transact
```

### Negation

Update an entity having a tacit filter attribute asserted that is **_not_** some value:

```scala
// Update age to 43 for all having a first name that is not "Bob"
Person.age(43).firstName_.not("Bob").update.transact

// Liz now 43
Person.firstName_?.lastName.age.query.get ==> List(
  (Some("Liz"), "Carson", 43), // updated
  (Some("Bob"), "Monroe", 42),
  (Some("Bob"), "Sponge", 35),
  (None, "Wilson", 72), // not update since firstName not asserted
)
```

Important to notice that Wilson was not updated since no `firstName` value exist to negate.

### NOR

Update multiple entities having a tacit filter attribute asserted that is **_not_** some value:

```scala
// Update age to 43 for last name neither "Carson" NOR "Monroe"
Person.age(43).lastName_.not("Carson", "Monroe").update.transact

// Sponge and Wilson now 43
Person.firstName_?.lastName.age.query.get ==> List(
  (Some("Liz"), "Carson", 27),
  (Some("Bob"), "Monroe", 42),
  (Some("Bob"), "Sponge", 43), // updated
  (None, "Wilson", 43), // updated
)
```

A dynamic list of filter values can be applied too with the same result:

```scala
val filterValues = List("Carson", "Monroe")
Person.age(43).lastName_.not(filterValues).update.transact
```

### Comparison

Update entities having a tacit filter attribute asserted that compares to some value:

```scala
Person.lastName.age.likes_?.insert(
  ("Carson", 27, Some("Pasta")),
  ("Monroe", 42, Some("Pizza")),
  ("Sponge", 35, Some("Apples")),
  ("Wilson", 72, None),
).transact

// Update likes of persons with age < 35
Person.age_.<(35).likes("Beef").update.transact

Person.lastName.age.likes_?.query.get ==> List(
  ("Carson", 27, Some("Beef")), // updated
  ("Monroe", 42, Some("Pizza")),
  ("Sponge", 35, Some("Apples")),
  ("Wilson", 72, None), // not update since not asserted
)
```

Likewise with other comparators:

```scala
// Update likes of persons with age <= 35
Person.age_.<=(35).likes("Beef").update.transact

// Update likes of persons with age > 35
Person.age_.>(35).likes("Beef").update.transact

// Update likes of persons with age >= 35
Person.age_.>=(35).likes("Beef").update.transact
```

### More...

Similarly, all [remaining filters](/docs/query/filters.html#string) on strings, numbers and collections can be used as update filters.


## Multiple filters

Combine filters to make even more specific selections of entities to be deleted:

```scala
// Update likes of persons with age between 30 and 45
Person.age_.>(30).age_.<(45).likes("Beef").update.transact

Person.lastName.age.likes_?.query.get ==> List(
  ("Carson", 27, Some("Pasta")),
  ("Monroe", 42, Some("Beef")), // updated
  ("Sponge", 35, Some("Beef")), // updated
  ("Wilson", 72, None),
)
```


## Upsert

As we saw in the above examples, attributes that haven't already been asserted will not be updated with an `update`. To insert the new data if missing, we can use `upsert` instead:

```scala
Person.lastName.age.likes_?.insert(
  ("Carson", 27, Some("Pasta")),
  ("Monroe", 42, Some("Pizza")),
  ("Sponge", 35, Some("Apples")),
  ("Wilson", 72, None),
).transact

// Upsert likes of persons with age > 35
Person.age_.>(35).likes("Beef").upsert.transact

Person.lastName.age.likes_?.query.get ==> List(
  ("Carson", 27, Some("Pasta")),
  ("Monroe", 42, Some("Beef")), // updated
  ("Sponge", 35, Some("Apples")),
  ("Wilson", 72, Some("Beef")), // inserted!
)
```

Note that _all_ attributes have to have values for an `update` to take place for an entity (row). If one attribute is null, other attributes won't be updated:

```scala
Person.name.age.likes_?.insert(
  ("Carson", 27, Some("Pasta")),
  ("Monroe", 42, Some("Pizza")),
  ("Sponge", 35, Some("Apples")),
  ("Wilson", 72, None),
).transact

// Update age and likes of all with a name
Person.name_.age(43).likes("Beef").update.transact

// Wilson entity not updated since likes not asserted
Person.name.age.likes_?.query.get ==> List(
  ("Carson", 43, Some("Beef")),
  ("Monroe", 43, Some("Beef")),
  ("Sponge", 43, Some("Beef")),
  ("Wilson", 72, None), // Neither age nor likes updated
)
```

Whereas if using `upsert`, missing values will be inserted:

```scala
// Update age and likes of all with a name
Person.name_.age(43).likes("Beef").upsert.transact

// Values updated/inserted for all entities
Person.name.age.likes_?.query.get ==> List(
  ("Carson", 43, Some("Beef")),
  ("Monroe", 43, Some("Beef")),
  ("Sponge", 43, Some("Beef")),
  ("Wilson", 43, Some("Beef")), // age updated, likes inserted
)
```

## Delete Attribute

An attribute value can be deleted by applying nothing to a mandatory attribute (no underscore):

```scala
Person.name.age_?.query.get.head ==> ("Bob", Some(42))

// Delete Bob's age
Person(bobId).age().update.transact

// Bob now has no age
Person.name.age_?.query.get.head ==> ("Bob", None)
```

## Collections

Collection values can be updated by either replacing the whole collection or by adding/removing individual values.

We'll use the following collections data to illustrate:

```scala
Person.name("Bob")
  .hobbies(Set("stamps", "trains"))
  .scores(Seq(1, 2, 3))
  .langNames(Map("en" -> "Hello", "es" -> "Hola"))
  .save.transact
```

### Replace all

`apply` a new collection to replace the old one:

```scala
// Replace collections
Person.name_
  .hobbies(Set("sport", "opera"))
  .scores(Seq(4, 5, 6))
  .langNames(Map("de" -> "Hallo"))
  .update.transact

// All collections replaced
Person.hobbies.scores.langNames.query.get.head ==> (
  Set("sport", "opera"),
  Seq(4, 5, 6),
  Map("de" -> "Hallo"),
)
```

### Add elements

`add` one or more elements to collections. A list of elements can also be added.

```scala
// Add elements to collections
Person.name_
  .hobbies.add("sports")
  .scores.add(4)
  .langNames.add("de" -> "Hallo")
  .update.transact

// Elements added to collections
Person.hobbies.scores.langNames.query.get.head ==> (
  Set("stamps", "trains", "sports"),
  Seq(1, 2, 3, 4),
  Map("en" -> "Hello", "es" -> "Hola", "de" -> "Hallo"),
)
```

### Remove elements

`remove` one or more elements from a collections. A list of elements can also be removed.

```scala
// Add elements to collections
Person.name_
  .hobbies.remove("trains")
  .scores.remove(3)
  .langNames.remove("es") // remove by key
  .update.transact

// Elements removed from collections
Person.hobbies.scores.langNames.query.get.head ==> (
  Set("stamps"),
  Seq(1, 2),
  Map("en" -> "Hello"),
)
```

## Relationships

Updates can be applied to entities that match specific relationship structures.

Given a relationship from Person to Address

```scala
Person.name.likes_?.Home.?(Address.street).insert(
  ("Bob", Some("Pasta"), Some("Main st. 17")),
  ("Eva", Some("Sushi"), None),
  ("Liz", None, Some("5th Ave. 1")),
  ("Tod", None, None),
).transact
```

we can explore updates across relationships:

### Base update

Here we update data of the base namespace `Person` if there is a relationship to `Address`:

```scala
// Update likes of all persons having a street address
Person.likes("Beef").Home.street_.update.transact

// Existing likes of Bob updated since he also has a street address
Person.name.likes_?.Home.?(Address.street).query.get ==> List(
  ("Bob", Some("Beef"), Some("Main st. 17")), // likes updated
  ("Eva", Some("Sushi"), None),
  ("Liz", None, Some("5th Ave. 1")), // no update without likes value
  ("Tod", None, None),
)
```

### Base upsert

Using `upsert` instead of `update` will insert missing values:

```scala
// Upsert likes of all persons having a street address
Person.likes("Beef").Home.street_.upsert.transact

// Likes of Bob and Liz upserted since they both have a street address
Person.name.likes_?.Home.?(Address.street).query.get ==> List(
  ("Bob", Some("Beef"), Some("Main st. 17")), // likes updated
  ("Eva", Some("Sushi"), None),
  ("Liz", Some("Beef"), Some("5th Ave. 1")), // likes inserted
  ("Tod", None, None),
)
```

### Ref update

Data in referenced namespaces can be updated too:

```scala
// Update street addresses of people with preferences
Person.likes_.Home.street("Bellevue 8").update.transact

// Only Bob's address updated since he has a preference and street address
Person.name.likes_?.Home.?(Address.street).query.get ==> List(
  ("Bob", Some("Pasta"), Some("Bellevue 8")), // street updated
  ("Eva", Some("Sushi"), None),
  ("Liz", None, Some("5th Ave. 1")),
  ("Tod", None, None),
)
```

### Ref upsert

Data in referenced namespaces can be updated too:

```scala
// Upsert street addresses of people with preferences
Person.likes_.Home.street("Bellevue 8").upsert.transact

// New relationship and street address inserted for Eva
Person.name.likes_?.Home.?(Address.street).query.get ==> List(
  ("Bob", Some("Pasta"), Some("Bellevue 8")), // street updated
  ("Eva", Some("Sushi"), Some("Bellevue 8")), // relationship + street inserted
  ("Liz", None, Some("5th Ave. 1")),
  ("Tod", None, None),
)
```


## Ref attr

We can update a relationship by updating the ref attribute id.

Here we update the ref attribute `country` on `Address` from the id of USA to the id of UK:

```scala
// Existing Country ids
val List(usaId, ukId) = Country.name.insert("USA", "UK").transact.ids

// Bob in USA
val bobId = Person.name("Bob")
  .Home.street("Main st. 17")
  .country(usaId) // set ref attribute to US id
  .save.transact.id

Person.name.Home.street.Country.name.query.get ==> List(
  ("Bob", "Main st. 17", "USA"),
)

// Bob moves to the UK
Person(bobId)
  .Home.street("Kings road 1")
  .country(ukId) // update ref attribute to UK id
  .update.transact

Person.name.Home.street.Country.name.query.get ==> List(
  ("Bob", "Kings road 1", "UK"),
)
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Update action compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/action/update)