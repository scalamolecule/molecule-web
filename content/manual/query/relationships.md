---
date: 2015-01-02T22:06:44+01:00
title: "Relationships"
weight: 60
menu:
  main:
    parent: query
---

# Relationships

(See [relationship tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/Relations.scala)
and [self-join tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/SelfJoin.scala))

Relationships are modelled in Molecule as "references between namespaces".

It's not that namespaces automatically become entities referencing each other but rather that referenced entities will likely pick attributes from a certain namespace. 

### One-to-One

A `Person` could have a cardinality-one reference to `City` having a cardinality-one reference to a `Country`:

```scala
Person.name.City.name.Country.name
```

Strictly speaking we have an entity with an asserted `:person/name` attribute value with a reference to another entity with an asserted `:city/name` attribute value etc. But in practice we simply talk about "namespace A has a relationship/reference to namespace B".

A namespace is not like an SQL Table but rather organizes some attributes by a meaningful name - it's, well, a _namespace_.


### One/Many-to-Many


If we have an `Order` with multiple `OrderLine`s we would instead define a cardinality-many reference to the `OrderLine` namespace in our Schema definition:

```scala
val orderLines = many[OrderLine]
```
This would cause Molecule to generate boilerplate code that would allow us to for instance insert multiple products for an order in one go:

```scala
m(Order.id.LineItems * LineItem.product.price.quantity).insert(
  ("order1", List((chocolateId, 48.00, 1), (whiskyId, 38.00, 2)))
  ("order2", List((bread, 13.00, 4)))  
)
```

We can then fetch the nested data

```scala
m(Order.id.LineItems * LineItem.product.price.quantity).get === List(
  ("order1", List((chocolateId, 48.00, 1), (whiskyId, 38.00, 2)))
  ("order2", List((bread, 13.00, 4)))  
)
```

The nested data structures can be arbitrarily deep (currently max 10 levels deep - can be expanded though if needed):

```scala
m(Order.orderid.LineItems * (LineItem.quantity.price.Comments * Comment.text.descr)).get === List(
  (23, List(
    (1, 48.00, List(
      ("first", "1a"),
      ("product", "1b"))),
    (2, 38.00, List(
      ("second", "2b"),
      ("is", "2b"),
      ("best", "2c")))
  ))
)
// etc...
```