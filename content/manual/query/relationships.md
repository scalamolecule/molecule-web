---
date: 2015-01-02T22:06:44+01:00
title: "Relationships"
weight: 60
menu:
  main:
    parent: query
---

# Relationships

Relationships are modelled in Molecule as "references between namespaces".

It's not that namespaces automatically become entities referencing each other but rather that referenced entities will likely pick attributes from a certain namespace. 

### One-to-One

A `Person` could have a cardinality-one reference to `City` having a cardinality-one reference to a `Country`:

```scala
Person.name.City.name.Country.name
```

Strictly speaking we have an entity with an asserted `:person/name` attribute value with a reference to another entity with an asserted `:city/name` attribute value etc. But in practice we simply talk about "namespace A has a relationship/reference to namespace B".

A namespace is not like an SQL Table but rather organizes some attributes by a meaningful name - it's, well, a _namespace_.


### One-to-Many


If we have an `Order` with multiple `OrderLine`s we would instead define a cardinality-many reference to the `OrderLine` namespace in our Schema definition:

```scala
val orderLines = many[OrderLine]
```
This would cause Molecule to generate boilerplate code that would allow us to for instance insert multiple products for an order in one go:

```scala
m(Order.id.LineItems * LineItem.product.price.quantity).insert(
  "order1", List((chocolateId, 48.00, 1), (whiskyId, 38.00, 2))
)
```

