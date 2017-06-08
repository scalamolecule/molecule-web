---
date: 2016-07-24T22:06:44+01:00
title: "Cardinality many"
weight: 20
menu:
  main:
    parent: relationships
up:   /docs/relationships
prev: /docs/relationships/card-one
next: /docs/relationships/composites
down: /docs/crud
---

# Card-many relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)

Cardinality-many relationships in Molecule are modelled with the `many[<RefNamespace>]` syntax:

```scala
object OrderDefinition {

  trait Order {
    val id    = oneString
    val items = many[LineItem].subComponents
  }

  trait LineItem {
    val qty     = oneInt
    val product = oneString
    val price   = oneDouble
  }
}
```
An `Order` can have multiple `LineItem`s so we define a cardinality-many ref attribute `items` that points to the `LineItem` namespace.

Note how we also make LineItems `subComponents` of the `Order`. That means that `LineItem`s are _owned_ by an `Order` and will get automatically 
retracted if the `Order` is retracted.

Now we can get an Order and its Line Items:

```scala
Order.id.Items.qty.product.price.get === List(
  ("order1", 3, "Milk", 12.00),
  ("order1", 2, "Coffee", 46.00),
  ("order2", 4, "Bread", 5.00)
)
```
The Order data is repeated for each line Item which is kind of redundant. We can avoid that with a "nested" Molecule instead:


## Nested results

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Nested.scala)

We can nest the result with the Molecule syntax `*` indicating "with many":

```scala
m(Order.id.Items * LineItem.qty.product.price).get === List(
  ("order1", List(
    (3, "Milk", 12.00), 
    (2, "Coffee", 46.00))),
  ("order2", List(
    (4, "Bread", 5.00)))
)
```
Now each Order has its own list of typed Line Item data and there is no Order redundancy.

This becomes more and more handy the deeper the hierarchy of data is. Molecule can nest data structures up to 10 levels deep!


### Entity API

[Tests...](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/ProductsAndOrders.scala)

We can get a similar - but un-typed - nested hierarchy of data with the Entity API by calling `touch` on an order id: 

```scala
// Touch entity facts hierarchy recursively
orderId.touch === Map(
  ":db/id" -> 101L,
  ":order/id" -> "order1",
  ":order/items" -> List(
    Map(
      ":db/id" -> 102L, 
      ":lineItem/qty" -> 3, 
      ":lineItem/product" -> "Milk",
      ":lineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":lineItem/qty" -> 2, 
      ":lineItem/product" -> "Coffee",
      ":lineItem/price" -> 46.0)))
```



### Next

[Composites...](/docs/relationships/composites)