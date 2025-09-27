

# Insert

Insert multiple entities by applying tuples of data to `insert` and finally call `transact`:

```scala
Person.name.age.insert(
  ("Bob", 42),
  ("Liz", 38),
).transact

Person.name.age.query.get ==> List(
  ("Bob", 42),
  ("Liz", 38),
)
```
Notice how the tuple type (String, Int) matches the molecule attributes (name.age). The compiler guarantees that the inserted data matches the molecule. 

Data can also be applied in a `Seq` of tuples (instead of varargs as above):
```scala
val listOfData = List(
  ("Bob", Some(42)),
  ("Liz", None),
)
Person.name.age_?.insert(listOfData).transact
```

Optional attributes and collection types can be used as with [`save`](save#optional-attr).




## Relationships

Additional related data can be added, and Molecule will transparently create the relationship for each row:

```scala
Person.name.age.Home.street.insert(
  ("Bob", 42, "Main st. 17"),
  ("Liz", 38, "5th Ave 1"),
).transact

Person.name.age.Home.street.query.get ==> List(
  ("Bob", 42, "Main st. 17"),
  ("Liz", 38, "5th Ave 1"),
)
```

## Foreign key

Foreign key attributes like `country` can be inserted to add a relationship for each row without creating a new related entity each time:

```scala
val usaId = Country.id.name_("USA").query.get.head

Person.name.age.Home.street
  .country // foreign key
  .insert(
    ("Bob", 42, "Main st. 17", usaId),
    ("Liz", 38, "5th Ave 1", usaId),
  ).transact

Person.name.age.Home.street.Country.name.query.get ==> List(
  ("Bob", 42, "Main st. 17", "USA"),
  ("Liz", 38, "5th Ave 1", "USA"),
)
```


## Nested data

Nest lists of tuples to save hierarchical one-to-many relationships like `Invoice`s each with multiple `InvoiceLine`s:

```scala
Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).insert(
  // Invoice 1
  (1, List(
    // Invoice lines for invoice 1
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),

  // Invoice 2
  (2, List(
    (1, "Knife", 40, 50),
    (4, "Bread", 10, 40),
  ))
).transact

Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).query.get ==> List(
  (1, List(
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),
  (2, List(
    (1, "Knife", 40, 50),
    (4, "Bread", 10, 40),
  ))
)
```
Molecule can insert nested hierarchies up to 7 levels deep.


## Optional nested

A nested list of data can be empty and no relationship or related data is created:

```scala
Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).insert(
  (1, List(
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),
  (2, List()) // Invoice 2 without invoice lines
).transact

Invoice.no.Lines.*(
  InvoiceLine.qty.product.unitPrice.lineTotal
).query.get ==> List(
  (1, List(
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  ))
)
```
But the main entity `Invoice` 2 is still created, just without `InvoiceLine`s. We can see this with an optional nested query allowing for empty nested data with `*?`:

```scala
Invoice.no.Lines.*?(
  InvoiceLine.qty.product.unitPrice.lineTotal
).query.get ==> List(
  (1, List(
    (2, "Socks", 15, 30),
    (5, "Bread", 10, 50),
  )),
  (2, List()) // Invoice 2 without invoice lines
)
```

## Nested data examples
For more nested data examples, see the [nested data](nested-data) section.

##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Insert action compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/action/insert)