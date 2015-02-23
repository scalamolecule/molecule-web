---
date: 2015-01-02T22:06:44+01:00
title: "Builder"
weight: 10
menu:
  main:
    parent: query
---

# Attribute builder pattern

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```scala
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.

### All Attributes looked for

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them. 

_Note that this is different from selecting rows from a sql table where you can also get null values back!_ 

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated with them then this query will _not_ return those entities:

```scala
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


### Underscore omits values

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values. If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age values, then we can add an underscore `_` after the `age` Attribute so that it becomes `age_`:

```scala
val names = Person.name.age_.get
```
This will return names of person entities having both a name and age Attribute set. Note how the age values are no longer returned from the type signatures:

```scala
val persons: List[(String, Int)] = Person.name.age.get
val names  : List[String]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures we look for.



### Tuples or HLists returned

Molecule returns all result sets as either tuples of values (with `get`) or Shapeless HLists (with `hl`).

```scala
val persons: List[(String, Int)]         = Person.name.age.get
val persons: List[String :: Int :: HNil] = Person.name.age.hl
```

### Molecule max size
The size of molecules are limited to Scala's limit size of 22 for tuples. Since molecules only look for attributes having a value it seems unlikely that we need to surpass this upper limit except in very special cases.

Once the entities matching our qriteria are found we can use those entities to find additional optional attribute values.