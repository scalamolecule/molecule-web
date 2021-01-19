---
title: "Building Blocks"
weight: 20
menu:
  main:
    parent: intro
---

# Building blocks

Molecule lets you model and query your domain data structures directly with the words of your domain.





## Queries

Let's say we want to find Persons in the Datomic database. Then we can build a molecule to get this data for us:

```scala
val persons: List[(String, Int)] = m(Person.name.age).get
```
This fetches a List of tuples of Strings/Int's that are the types of the `name` and `age` Attributes that we asked for. We can continue adding more and more Attributes as with the builder pattern to define what data we are interested in.


### Attributes in Namespaces

_Attributes_ are atomic pieces of information that are prefixed by a _Namespace_, in the example above, a `Person` Namespace. Namespaces are not like SQL tables but just a common meaningful prefix to Attributes that have something in common.

```scala
val persons = Person.name.age.get
```



### Expressions

We can apply conditional values, ranges etc to our molecules to express more subtle data structures:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```
which will find "names of twitter/facebook_page communities in neighborhoods of southern districts".


## Operations

For single entities we can apply data to the attributes of a molecule and then save it:

```scala
// Save Lisa entity and retrieve new entity id
val lisaId = Person.name("Lisa").age("27").save.eid
```
Or we can add data for multiple entities with an `insert`:

```scala
// Save Lisa entity and retrieve new entity id
Person.name.age insert List(
  ("Lisa", 27), // Inserting Lisa entity
  ("John", 32)  // Inserting John entity  
)
```
Using an entity id we can update attribute values of an entity:
```scala
// Update age attribute value of Lisa entity
Person(lisaId).age("28").update
```
In Datomic, an update is actually a retraction of the old data and an assertion of the new data. In this example, Lisa's age 27 is retracted and her new age 28 asserted. With this information model, Datomic allow us to go back in time and see when Lisa's age was changed to 28.

Or we can retract an entity entirely by calling `retract` on an entity id
```scala
// Retract ("delete") Lisa entity
lisaId.retract
```
Since the entity is retracted and not deleted, Datomic allow us to go back in time before the retraction to see that Lisa existed.



