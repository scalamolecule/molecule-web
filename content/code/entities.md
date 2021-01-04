---
title: "Entities"
weight: 50
menu:
  main:
    parent: code
    identifier: entities
up:   /manual/attributes
prev: /manual/attributes/parameterized
next: /manual/relationships
down: /manual/relationships
---

# Entities

An entity in Datomic is a group of Datoms/facts that share an entity id:
 
![](/img/page/entity/entity1.png)


Attributes with any seemingly unrelated namespaces can group as entities by simply sharing the entity id:

![](/img/page/entity/entity2.png)

This demonstrates that Datomic/Molecule Namespaces are not like Tables in SQL. The above entity for instance has attributes asserted from 2 different namespaces that could be completely unrelated/have no reference to each other. Attributes from any number of namespaces could be asserted sharing the same entity id.

## Entity API

At runtime we can see the facts of an entity by calling `touch` on the entity id (of type `Long`):

```
101L.touch === Map(
  ":db/id" -> 101L,
  ":Person/name"  -> "Fred", 
  ":Person/likes" -> "pizza", 
  ":Person/age"   -> 38, 
  ":Person/addr"  -> 102L,        // reference to an address entity with entity id 102 
  ":Site/cat"     -> "customer"
)
```



### Optional attribute values

We can look for an optionally present attribute value. Here we ask the entity id `fredId` if it has a `:Site/cat` attribute value (of type `String`) and we get a typed optional value back:
```
val siteCat_? : Option[String] = fredId[String](":Site/cat")
```


### Traversing

The `touch` method can recursively retrieve referenced entities. We could for instance traverse an `Order` with `LineItems`:


```
orderId.touch === Map(
  ":db/id" -> orderId,
  ":Order/lineItems" -> List(
    Map(
      ":db/id" -> 102L, 
      ":LineItem/qty" -> 3, 
      ":LineItem/product" -> "Milk",
      ":LineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":LineItem/qty" -> 2, 
      ":LineItem/product" -> "Coffee",
      ":LineItem/price" -> 46.0)))
```

The entity attributes graph might be deep and wide so we can apply a max level to `touch(<maxLevel>)`:

```
fredId.touchMax(2) === Map(
  ":db/id" -> fredId,
  ":Person/name" -> "Fred"
  ":Person/friends" -> List(
    Map(
      ":db/id" -> lisaId,
      ":Person/name" -> "Lisa"
      ":Person/friends" -> List(
        Map(
          ":db/id" -> monaId,
          ":Person/name" -> "Mona"
          ":Person/friends" -> Set(ids...) // Mona's friends (3 levels deep) only as ids - not attribute maps
        ),
        Map(...) // + more friends of Lisa (2 levels deep)
      )
    ),
    Map(...) // + more friends of Fred (1 level deep)
  )
)
```



### Next

The entity API is not type-safe so in [Relationships](/manual/relationships/) we will look at various ways of traversing referenced data in type-safe ways with Molecule.