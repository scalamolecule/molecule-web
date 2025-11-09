
# Delete

One or more entities can be deleted with a molecule identifying the entities. 

When an entity is deleted, the SQL table row is deleted.

Entities can be identified either by applying ids or by the shape of the molecule with its relationships and tacit filters as we'll see below.


## Id

Delete an entity by applying an id to an entity and call `delete.transact` on it:

```scala
Person(bobId).delete.transact
```
Bob and all attributes describing him are now deleted. The Bob row is deleted.

## Ids

Delete multiple entities by applying multiple ids either as varargs or in a list:

```scala
Person(bobId, lizId).delete.transact

// Or
val someIds = List(bobId, lizId)
Person(someIds).delete.transact
```


## Filters
Another way to delete multiple entities is to filter by tacit attributes. One or more filter attributes can be used to narrow the selection of entities to be deleted.

> Delete filters are similar to delete filters

Let's use the following sample data set to illustrate the various delete filter possibilities:

```scala
Person.name_?.age.insert(
  (Some("Liz"), 27),
  (Some("Bob"), 35),
  (Some("Bob"), 42),
  (None, 72),
).transact
```

### Not null

The most wide filter is to delete all entities having any value set for a tacit filter attribute `attr_`.

```scala
// Delete persons with a `name`:
Person.name_.delete.transact

Person.name_?.age.query.get ==> List(
  // (Some("Liz"), 27), // deleted
  // (Some("Bob"), 35), // deleted
  (None, 72),
)
```

### Null

Likewise, we can delete entities with a missing value (null) by applying nothing to a tacit attribute `attr_()`.

```scala
// Delete persons _without_ a `name`:
Person.name_().delete.transact

Person.name_?.age.query.get ==> List(
  (Some("Liz"), 27),
  (Some("Bob"), 35),
  // (None, 72), // deleted
)
```

### Equality

A more narrow filter is to apply a value to a tacit attribute, like deleting all people with "Bob" as their first name:


```scala
// Delete persons named "Bob":
Person.name_("Bob").delete.transact

Person.name.age.query.get ==> List(
  ("Liz", 27),
  // ("Bob", 35), // deleted
  // ("Bob", 42), // deleted
)
```

### OR

Filter by applying multiple values:

```scala
Person.age_(27, 42).delete.transact
// Or
// Person.age_(List(27, 42)).delete.transact

Person.name.age.query.get ==> List(
  // ("Liz", 27), // deleted
  ("Bob", 35),
  // ("Bob", 42), // deleted
)
```

### Negation

Delete an entity having a tacit filter attribute asserted that is **_not_** some value:

```scala
// Delete persons not 42 years old:
Person.age_.not(42).delete.transact

Person.name.age.query.get ==> List(
  // ("Liz", 27), // deleted
  // ("Bob", 35), // deleted
  ("Bob", 42),
)
```

### NOR

Delete multiple entities having a tacit filter attribute asserted that is **_not_** some value:

```scala
// Delete persons neither 27 NOR 42 years old
Person.age_.not(27, 42).delete.transact
// or
// Person.age_.not(List(27, 42)).delete.transact

Person.name.age.query.get ==> List(
  ("Liz", 27),
  // ("Bob", 35), // deleted
  ("Bob", 42),
)
```


### Comparison

Delete entities having a tacit filter attribute asserted that compares to some value:

```scala
// Delete persons younger than 35
Person.age_.<(35).delete.transact

Person.name.age.query.get ==> List(
  // ("Liz", 27), // deleted
  ("Bob", 35),
  ("Bob", 42),
)
```

Likewise with other comparators:

```scala
// Delete persons with age <= 35
Person.age_.<=(35).delete.transact

// Delete persons with age > 35
Person.age_.>(35).delete.transact

// Delete persons with age >= 35
Person.age_.>=(35).delete.transact
```

### More...

Similarly, all [remaining filters](/database/query/filters.html#string) on strings, numbers and collections can be used as delete filters.


## Multiple filters

Combine filters to make even more specific selections of entities to be deleted:

```scala
// Delete persons with age between 30 and 45
Person.age_.>(30).age_.<(45).delete.transact

Person.name.age.query.get ==> List(
  ("Liz", 27),
  // ("Bob", 35), // deleted
  // ("Bob", 42), // deleted
)
```

## Relationships

Delete entities and their relationships with filters across related entities:

```scala
Person.name.Home.?(Address.street).insert(
  ("Bob", Some("DoobieSetup2 st. 1")),
  ("Liz", Some("5th Ave. 1")),
  ("Tod", None),
).transact

// Delete persons having a street address
Person.Home.street_.delete.transact

Person.name.Home.?(Address.street).query.get ==> List(
  // ("Bob", Some("DoobieSetup2 st. 1")), // deleted
  // ("Liz", Some("5th Ave. 1")), // deleted
  ("Tod", None),
)
```
All the previous filters can be applied across relationships to build complex deletion selections.

## Owner refs

Make sure that owned referenced entities of a relationship are deleted when the owning entity is deleted. 

The classical example is an Invoice owning its Invoice Lines. This can be modelled in the domain structure by adding the `owner` option after the relationship definition:

```scala
trait Invoice {
  ...
  val lines = manyToOne[InvoiceLine].owner // Invoice owns InvoiceLine's
}
```

Let's see it in action:

```scala
// Insert 2 invoices, each with 2 invoice lines
Invoice.no.Lines.*(
  InvoiceLine.qty.product.lineTotal
).insert(
  // Invoice 1
  (1, List(
    // Invoice lines for invoice 1
    (2, "Socks", 30),
    (5, "Bread", 50),
  )),

  // Invoice 2
  (2, List(
    (1, "Knife", 50),
    (4, "Bread", 40),
  ))
).transact

// Delete invoice 1 and its invoice lines!
Invoice.no_(1).delete.transact

// Invoice 1 and its invoice lines deleted
Invoice.no.Lines.*?(
  InvoiceLine.qty.a1.product.lineTotal
).query.get ==> List(
  (2, List(
    (1, "Knife", 50),
    (4, "Bread", 40),
  ))
)

// Confirming that invoice lines of invoice have been deleted
InvoiceLine.qty.product.lineTotal.query.get ==> List(
  (1, "Knife", 50),
  (4, "Bread", 40),
)
```

Beware that ownership applies recursively! So, if we had defined a further ownership from InvoiceLine to another entity, this would have been deleted too when an Invoice is deleted.


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Delete action compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/action/delete)