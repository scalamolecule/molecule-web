---
date: 2015-01-02T22:06:44+01:00
title: "Retract"
weight: 50
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/update
next: /docs/transactions
down: /docs/transactions
---

# Retract data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/manipulation/Retract.scala)


### Retract facts

To retract individual attributre values apply empty parenthesises to 
the attribute we want to retract and then update the molecule:


```scala
Community(belltownId).name().category().update
```
Here we retracted the `name` and `category` attribute values of the Belltown Community entity:


### Retract entity

To delete a whole entity with all its attribute values we can call `retract` on a `Long` entity id 

```scala
fredId.retract
```
Here all attributes having the entity id `fredId` are retracted.

Alternatively we can use the `retract` method (available via `import molecule._`)

```scala
retract(fredId)
```
This `retract` method can also retract multiple entities

```scala
val eids: Iterable[Long] = // some entity ids 

// Retract all supplied entity ids
retract(eids)
```

### Retract entity with sub components

If a ref attribute is defined with the option `subComponent` or `subComponents` then those related entites 
are "owned" by the main entity as when an `Order` own its `LineItem`s. 

```scala
object ProductsOrderDefinition {

  trait Order {
    val id    = oneInt
    val items = many[LineItem].subComponents // Order owns its line items
  }

  trait LineItem {
    val product = oneString
    val price   = oneDouble
    val qty     = oneInt
  }
}
```

If we retract such `Order`, then all
of its related `LineItem`s are also retracted:

```scala
orderId.retract // All related `LineItem`s are also retracted!

// or
retract(orderId)
```
Subcomponents are recursively retracted! So if `LineItem` would have had subcomponents then those would have been retracted too when the order 
was retracted - and so on down the hierarchy of subcomponents.



### Next

[Transactions...](/docs/transactions)