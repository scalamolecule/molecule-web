---
date: 2015-01-02T22:06:44+01:00
title: "Composites"
weight: 30
menu:
  main:
    parent: relationships
up:   /manual/relationships
prev: /manual/relationships/card-many
next: /manual/relationships/bidirectional
down: /manual/crud
---

# Composites

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Composite.scala)

As we saw earlier, [Entities](/manual/entities/) are simply groups of facts that share an entity id:

![](/img/entity/entity5.jpg)

The last fact is kind of a black sheep though since the `:site/cat` attribute is not in the `Person` namespace.

### Avoid non-intrinsic pollution

Since entities can have attributes from **any** namespace we have a challenge of how to model this in our
schema definiton. It would be quick and easy to just make a relationship from a `Person` namespace to
the `Site` namespace:


```scala
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
    val addr  = one[Addr]
    val site  = one[Site]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
  trait Site {
    val cat    = oneString
  }
}
```
Then we could easily build a molecule to get the `Site` category:

```scala
Person.name.likes.age.Site.cat.get.head === ("Fred", "pizza", 38, "customer")
```


Modelling-wise this is just not the best idea since we could easily end up making lots of redundant relationships from 
various namespaces to `Site`:

```scala
object YourDomainDefinition {
  trait Person {
    val site  = one[Site]
  }
  trait Company {
    val site  = one[Site]
  }
  trait Project {
    val site  = one[Site]
  }
  // etc...
  
  trait Site {
    val cat    = oneString
  }
}
```
Relationships to `Site` are simply not _intrinsic_ to or a natural core part of neither `Person`, `Company` or `Project`. 
Littering non-intrinsic relationships to `Site` - and possibly other cross-cutting namespaces like `Tags`, `Likes` etc - all 
over the place quickly clutters and pollutes our domain model.

Instead we want to create a more "loose association" to `Site`. This is what Datomic allow us to do by letting an entity id
tie _any_ attributes together as we see in the list of facts at the top of this page. 


## Composite modelling

In Molecule we can model "associative relationships" - or "composites" with the `~` method:

```scala
m(Person.name.likes.age ~ Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)
```
We make a composite molecule from two "sub-molecules" `Person.name.likes.age` and `Site.cat`. 

The composite result set is a list of tuples with
a sub-tuple for each sub-molecule. 

Since in this case the last sub-molecule only has one attribute value "customer" a single value for that is returned. If it
had 2 attributes we would get a sub-tuple for that too:

```scala
m(Person.name.likes.age ~ Site.cat.status).get === List(
  (("Fred", "pizza", 38), ("customer", "good"))
)
```
And so on..

```scala
m(Person.name.likes.age ~ Site.cat.status ~ Loc.tags ~ Emotion.like).get === List(
  (("Fred", "pizza", 38), ("customer", "good"), Set("inner city", "hipster"), true)
)
```

We can compose up to 22 sub-molecules (!) which should give us plenty of room to model even the most complex 
composite aspects of our domain.


### ...with expressions

_"Which positive elder hipster customers like what?"_

```scala
m(Person.name.likes.age_.>(35) 
 ~ Site.cat_("customer")
 ~ Loc.tags_("hipster") 
 ~ Emotion.like_(true)).get === List(
  ("Fred", "pizza")
)
```
The combinations are quite endless - while you can keep your domain model/schema clean and intrinsic!



## Arity 22+ molecules

Since composites are composed of up to 22 sub-molecules we could potentially insert and retrieve 
mega composite molecules with up to 22 x 22 = 484 attributes, although the compiler probably wouldn't 
be too happy about that. 

### Compile time performance trick...

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/perf/CompilationPerf.scala)

Compile time for molecules longer than about 12-15 attributes tend to increase sharply, so simply 
splitting up long molecules in 2 or more sub-molecules composed into 1 composite molecule easily keeps compilation fast.

It's also worth to remember that each mandatory attribute in a molecule is like a where clause. So having more than
 12-15 where clauses seems also like a less frequent need.



### Next

[Bidirectional references...](/manual/relationships/bidirectional)