---
date: 2015-01-02T22:06:44+01:00
title: "Retract"
weight: 70
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud/update
next: /manual/transactions
down: /manual/transactions
---

# Retract data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/Retract.scala)


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

Alternatively we can use the `retract` method (available via `import molecule.imports._`)

```scala
retract(fredId)
```
This `retract` method can also retract multiple entities

```scala
val eids: List[Long] = // some entity ids 

// Retract all supplied entity ids
retract(eids)
```

### Retract component entity

If a ref attribute is defined with the option `isComponent` then it "owns" its related entities - or "subcomponents", as when an `Order` own its `LineItem`s.

```scala
object ProductsOrderDefinition {

  trait Order {
    val id    = oneInt
    val items = many[LineItem].isComponent // Order owns its line items
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
Component entities are recursively retracted! So if `LineItem` would have had subcomponents then those would have been retracted too when the order
was retracted - and so on down the hierarchy of subcomponents.



### Next

[Transactions...](/manual/transactions)