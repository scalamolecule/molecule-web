---
date: 2016-07-24T22:06:44+01:00
title: "Bidirectional refs"
weight: 65
menu:
  main:
    parent: query
---

# Bidirectional references

(See [bidirectional tests](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/bidirectional/))


### Unidirectional reference limitations

Normal Datomic references are unidirectional. If we add a friend reference from Ann to Ben

```scala
Person.name("Ann").Friends.name("Ben").save
```
Then we can naturally query to get friends of Ann

```scala
Person.name_("Ann").Friends.name.get === List("Ben")
```

But what if we want to find friends of Ben? This will give us nothing since our reference only went from Ann to Ben:

```scala
Person.name_("Ben").Friends.name.get === List()
```

Instead we would have to think backwards to get the back reference "who referenced Ben?":
 
```scala
 Person.name.Friends.name_("Ben").get === List("Ann")
```
Since we can't know from which person a friendship reference is made we will always have to query 
separately in both directions. If we were to traverse say 3 levels into a friendship graph we would end
 up with 6 queries - one in each direction for all three levels. It can quickly become a pain.

### Bidirectional refs to the rescue...

By defining a relationship in Molecule as bidirectional:
```scala
val friends = manyBi[Person]
```
we can start treating friendship relationships uniformly in both directions and get the intuitively 
expected results
```scala
 Person.name_("Ann").Friends.name.get === List("Ben")
 Person.name_("Ben").Friends.name.get === List("Ann")
```

And the graph example becomes easy
```scala
 Person.name_("Ann").Friends.Friends.Friends.name.get === List(...)
 
 // Or without recycling to Ann
 Person.name_("Ann").Friends.Friends.name_.not("Ann").Friends.name.not("Ann").get === List(...)
```

## Direct bidirectional refs

Single direct references to another entity can either go to the same namespace or to another namespace:

### A <---> A
The friendship reference we saw above is a classic "self-reference" in that it's a cardinality-many relationship 
between same-kinds: Persons.

A cardinality-one example would be
```scala
val spouse = oneBi[Person]
```
where the relationship goes in both directions but only between two persons. If Ann is spouse to Ben, then Ben 
is also spouse to Ann and we want to be able to query that information uniformly in both directions:

```scala
 Person.name_("Ann").Spouse.name.get === List("Ben")
 Person.name_("Ben").Spouse.name.get === List("Ann")
```


### A <---> B

In a zoo we could (admittedly a bit contrively) say that the caretakers are buddies with the animals they take care of, 
and reversely the caretakers would be "buddies" of the animals. We define such bidirectional relationship with a
reference from each namespace to the other:

```scala
object Person extends Person
trait Person {
  val buddies = manyBi[Animal.buddies.type]
  
  val name = oneString
}

object Animal extends Animal
trait Animal {
  val buddies = manyBi[Person.buddies.type]
  
  val name = oneString
}
```
Each `manyBi` reference definition takes a type parameter that points back to the other definition. This is so that
 Molecule can keep track of the references back and forth.

As with friends we can now query uniformly no matter from which end the reference was entered:

```scala
 Person.name_("Joe").Buddies.name.get === List("Leo", "Gus")
 Animal.name_("Leo").Buddies.name.get === List("Joe")
 Animal.name_("Gus").Buddies.name.get === List("Joe")
```
An interesting aspect is that we can give the reference attributes different names on each end. Say Persons have 1 Pet
and we model that as a bidirectional cardinality-one reference:

```scala
object Person extends Person
trait Person {
  val pet = oneBi[Animal.master.type]
  
  val name = oneString
}

object Animal extends Animal
trait Animal {
  val master = oneBi[Person.pet.type]
}
```
If we then enter a pet ownership 

```scala
Person.name("Liz").Pet.name("Rex").save
```
then we can get access to that information uniformly from both ends even though different attribute names are used:

```scala
 Person.name_("Liz").Pet.name.get === List("Rex")
 Animal.name_("Rex").Master.name.get === List("Liz")
```


## Property edges

Taking bidirectionality to the next level involves "property edges", a term taken from graph theory where an edge/relationship
between two vertices/entities has some property values attached to it. Molecule models this by using a bidirectional reference between one entity
and a (property edge) entity, and then between this property edge entity and another entity.

This is actually what we do all the time with references except that they are normally unidirectional! In order to make them bidirectional
Molecule offers a convenient solution:


### A <---> Edge.properties... <---> A

If we want to express "how well" two persons know each other we could model the above friendship example instead with at property edge
having a `weight` property:

```scala
// Entity
object Person extends Person
trait Person {
  // A ==> edge -- a
  val knows = manyBiEdge[Knows.person.type]
  
  val name = oneString
}

// Property edge
object Knows extends Knows
trait Knows {
  // a --- edge ==> a
  val person: AnyRef = target[Person.knows.type]
  
  // Property
  val weight = oneInt
}
```
Now we use the `manyBiEdge` definition that takes a type parameter pointing to the reference in the edge namespace that points back here. In the
edge namespace we define the reference back with the `target` definition.

Now we can add some weighed friendships:

```scala
Person.name.Knows.*(Knows.weight.Person.name).insert("Ann", List((7, "Ben"), (8, "Joe")))
```
And uniformly retrieve that information from any end:

```scala
Person.name_("Ann").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ben"), (8, "Joe"))
Person.name_("Ben").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ann"))
Person.name_("Joe").Knows.*(Knows.weight.Person.name).get.head === List((8, "Ann"))
```

### A <---> Edge.properties... <---> B

Let's add weight to the relationships between caretakers and animals. The edge namespace now has to reference both the Person and Animal 
namespaces:

```scala
// Entity A
object Person extends Person
trait Person {
  // Ref to edge
  // A ==> edge -- b
  val closeTo = manyBiEdge[CloseTo.animal.type]
  
  val name = oneString
}

// Property edge
object CloseTo extends CloseTo
trait CloseTo {
  // Ref to Person
  // a <== edge --- b
  val person: AnyRef = target[Person.closeTo.type]
  
  // Ref to Animal
  // a --- edge ==> b
  val animal: AnyRef = target[Animal.closeTo.type]
  
  // Property
  val weight = oneInt
}

// Entity B
object Animal extends Animal
trait Animal {
  // Ref to edge
  // a -- edge <== B
  val closeTo  = manyBiEdge[CloseTo.person.type]
  
  val name = oneString
}
```
Adding data from one end (we could as well have done it from the Animal end)

```scala
Person.name.CloseTo.*(CloseTo.weight.Animal.name) insert List(("Joe", List((7, "Gus"), (6, "Leo"))))
```
We can now uniformly retrieve the weighed friendship information from any end:

```scala
// Querying from Person
Person.name_("Joe").CloseTo.*(CloseTo.weight.Animal.name).get.head === List((7, "Gus"), (8, "Leo"))

// Querying from Animal
Animal.name_("Gus").CloseTo.*(CloseTo.weight.Person.name).get.head === List((7, "Joe"))
Animal.name_("Leo").CloseTo.*(CloseTo.weight.Person.name).get.head === List((8, "Joe"))
```

### How it works

For each bidirectional reference created, Molecule creates a reverse reference:

```
Ann --> Ben
Ben <-- Ann // reverse ref
```
and for edges a full reverse edge entity with properties is created: 

```
Ann --> annLovesBen (7) -->  Ben
  \                         /
    <-- benLovesAnn (7) <--       // reverse edge
```

Since Molecule is a closed eco-system it can manage this redundancy with 100% control. The advantages 
of uniform queries should easily outweigh the impact of a bit of additional information for the reverse references.


### More exampes...

Please have a look at the implementation of the 
[Gremlin graph](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/). 




















































