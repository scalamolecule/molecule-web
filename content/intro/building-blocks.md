---
title: "Building blocks"
weight: 30
menu:
  main:
    parent: intro
---


# Molecule building blocks


Here's a quick overview of various building blocks in the Molecule eco-system.




## Molecule

A _molecule_ is a model of a data structure containing one or more _attributes_.

Here we describe a data structure of `name`, `age` of persons and what `street` they live on:
```scala
Person.name.age.Address.street.get.map(_.head ==> ("John", 24, "5th Avenue"))
```
Calling `get` on a molecule returns typed data that matches the molecular data structure.


## Attribute

An _attribute_ is the core element of Molecule. Molecules are composed of attributes.

We could also call it a "property" a "field" etc. It's an intrinsic atomic piece of information about something.

Attributes have

- _Type_ - `String`, `Int` etc.
- _Cardinality_ - "one" for a single thing, "many" for multiple
- _Options_ - is the value indexed, searcheable, unique etc.
- _Descriptions_ - meta info about the attribute - that can be queried



## Datom

A _Datom_ brings context to an Attribute.

A Datom describes a _fact_, for instance that "John likes pizza". A timestamp adds information about _when_ John stated that he likes pizza. A fifth piece of information about the operation performed tells if the fact is asserted (true - John likes) or retracted (false - John no longer likes). 

So, a Datom consists of 5 components of information:
```
johnId    likes    pizza      12:35:54        true
   |        |        |           |             |
Entity  Attribute  Value  Transaction/time  Operation
```
A molecule can retrieve this information:
```scala
// Q: Who likes what and when did they state it?
Person.e.likes.txInstant.get.map(_.head ==> 
  // A: John said he likes pizza at 12:35:54
  (johnId, "pizza", Date("12:35:54"))
)
```

## Entity

When multiple different Datoms (attributes) share the same entity id, _they together describe this entity_. Or we say that _"the Entity has 3 attributes"_. 

In our example John is an entity that has a name "John", likes "pizza" and is 24 years old - a pizza-liking 24-year-old John: 

{{< bootstrap-table "table table-bordered" >}}
Entity id  | Attribue      | Value    
:---:      | :---          | :---  
**101**    | :Person/name  | "John"
**101**    | :Person/likes | "pizza"
**101**    | :Person/age   | 24
{{< /bootstrap-table >}}

As you see, the concept of an entity is very flexible since it can be defined by endless combinations of attributes and values that will give it unique characteristics. 

This is far more powerful than thinking in terms of "defining a Person class"! When we instead let the combinations of atomic attributes define entities of molecular data structures, our semantic capabilities and expressiveness increase exponentially. 


## Namespace

Attributes are loosely organized in _Namespaces_ to semantically group qualities of a subset of our domain:
<br><br>

![](/img/page/intro/DatomicElements1.png)
<br><br>

As we saw, an Entity can have _any_ Attribute from _any_ Namespace associated to it:
<br><br>

![](/img/page/intro/DatomicElements2.png)
<br><br>

An entity is therefore _not_ like a row in a table but rather a "cross-cutting" thing that we can freely associate any attribute value to.


## Time

Since the transaction time is part of all Datoms, we can ask time-related questions:

```scala
// Who liked what on the 5th of november?
Person.name.likes.txInstant_(nov5date).get.map(_.head ==> ("John", "pizza"))
```

Datomic even offers various powerful ways to work with the [time](/documentation/time) dimension of our data:

- `getAsOf` some point in time 
- `getSince` some point in time
- `getHistory` of an entity or attribute
- `getWith(tx-stmts)` to test a future what-if scenario


## Expression

We can mix relationships, conditional values, logic etc in our molecules to express complex and precise data structures:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```
which will find "names of twitter/facebook_page communities in neighborhoods of southern districts".


### Next

[Compare](/intro/compare) Molecule with another query language, [set up a Molecule project](/setup/) or learn more about [molecule code...](/code) 