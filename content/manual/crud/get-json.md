---
title: "Get Json"
weight: 50
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud/get
next: /manual/crud/update
down: /manual/transactions
---

# Get Json formatted Data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/json)

We can get data in json format directly from the database by calling `getJson` on a molecule. So instead of converting tuples of data to 
json with some 3rd party library we can call `getJson` and pass the json data string directly to an Angular table for instance.

Internally, Molecule builds the json string in a StringBuffer directly from the raw data coming from Datomic 
(with regards to types being quoted or not). This should make it the fastest way of supplying json data when needed.

To avoid ambiguity all attribute names are prefixed with their namespace name, all in lower case and separated by a dot:

```scala
Person.name.age.getJson ===
  """[
    |{"person.name": "Fred", "person.age": 38},
    |{"person.name": "Lisa", "person.age": 35}
    |]""".stripMargin
```


### Composite data

Each sub part of the composite is rendered as a separate json object tied together in an array for each row: 
 
```scala
m(Person.name.age + Category.name.importance).getJson ===
  """[
    |[{"person.name": "Fred", "person.age": 38}, {"category.name": "Marketing", "category.importance": 6}],
    |[{"person.name": "Lisa", "person.age": 35}, {"category.name": "Management", "category.importance": 7}]
    |]""".stripMargin
``` 
Note the `name` field is prefixed by a different namespace in each json sub-object.


### Nested data

Nested date is rendered as a json array with json objects for each nested row: 

```scala
m(Invoice.no.customer.InvoiceLines * InvoiceLine.item.qty.amount).getJson ===
  """[
    |{"invoice.no": 1, "invoice.customer": "Johnson", "invoice.invoiceLines": [
    |   {"invoiceline.item": "apples", "invoiceline.qty": 10, "invoiceline.amount": 12.0},
    |   {"invoiceline.item": "oranges", "invoiceline.qty": 7, "invoiceline.amount": 3.5}]},
    |{"no": 2, "customer": "Benson", "invoiceLines": [
    |   {"invoiceline.item": "bananas", "invoiceline.qty": 3, "invoiceline.amount": 3.0},
    |   {"invoiceline.item": "oranges", "invoiceline.qty": 1, "invoiceline.amount": 0.5}]}
    |]""".stripMargin
```

### Render strategies...

Various render strategies could rather easily be added if necessary. In that case, please file an issue with a description of a desired format.


### Next

[Update...](/manual/crud/update)