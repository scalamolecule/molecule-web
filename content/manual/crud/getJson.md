---
date: 2015-01-02T22:06:44+01:00
title: "Get Json"
weight: 50
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/get
next: /docs/crud/update
down: /docs/transactions
---

# Get Json formatted Data

We can get data in json format directly from the database by calling `getJson` on a molecule. So instead of converting tuples of data to 
json with some 3rd party library we can call `getJson` and pass the json data string directly to an Angular table for instance.

Internally, Molecule builds the json string in a StringBuffer directly from the raw data coming from Datomic 
(with regards to types being quoted or not). This should make it the fastest way of supplying json data when needed.


### Flat data

Normal "flat" molecules creates json with a row on each line in the output:

```scala
Person.name.age.getJson ===
  """[
    |{"name": "Fred", "age": 38},
    |{"name": "Lisa", "age": 35}
    |]""".stripMargin
```


### Composite data

Composite data has potential field name clashes so each sub part of the composite is rendered 
as a separate json object tied together in an array for each row: 
 
```scala
m(Person.name.age ~ Category.name.importance).getJson ===
  """[
    |[{"name": "Fred", "age": 38}, {"name": "Marketing", "importance": 6}],
    |[{"name": "Lisa", "age": 35}, {"name": "Management", "importance": 7}]
    |]""".stripMargin
``` 
Note how a field `name` appears in each sub object. Since the molecule is defined in client code it is presumed that 
the semantics of eventual duplicate field names are also handled by client code.


### Nested data

Nested date is rendered as a json array with json objects for each nested row: 

```scala
(Invoice.no.customer.InvoiceLines * InvoiceLine.item.qty.amount).getJson ===
  """[
    |{"no": 1, "customer": "Johnson", "invoiceLines": [
    |   {"item": "apples", "qty": 10, "amount": 12.0},
    |   {"item": "oranges", "qty": 7, "amount": 3.5}]},
    |{"no": 2, "customer": "Benson", "invoiceLines": [
    |   {"item": "bananas", "qty": 3, "amount": 3.0},
    |   {"item": "oranges", "qty": 1, "amount": 0.5}]}
    |]""".stripMargin
```

### Render strategies...

Various render strategies could rather easily be added if necessary. In that case, please file an issue with a description of a desired format.


### Next

[Update...](/docs/crud/update)