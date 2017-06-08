---
date: 2015-01-02T22:06:44+01:00
title: "Save"
weight: 10
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud
next: /docs/crud/insert
down: /docs/transactions
---

# Save

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/manipulation/Save.scala)

In Molecule we can populate a molecule with data and save it:

```scala
Person.name("Fred").likes("pizza").age(38).save
```

This will assert 3 facts in Datomic that all share the id of the new entity id `fredId` that is automatically created by Datomic:

```
fredId    :person/name    "Fred"
fredId    :person/likes   "pizza"
fredId    :person/age     38
```

### Type-safety

Type-safety is guaranteed since each attribute only accepts values of its defined type.

### Related data

We can even save related date in the same operation
```scala
Person.name("Fred").likes("pizza").age(38).Home.street("Baker St. 7").city("Boston").save
```
In this case, 6 facts will be asserted for the entity of Fred. A `:person/home` ref attribute will resolve to the
value of a new Address entity with id `addrId` and thereby establish the relationship from Fred to his Address:

```
fredId    :person/name    "Fred"
fredId    :person/likes   "pizza"
fredId    :person/age     38
fredId    :person/home    addrId
addrId    :addr/street    "Baker St. 7"
addrId    :addr/city      "Boston"
```
And we could go on with further relationships...

### Cardinality many values

Cardinality many attributes like for instance `hobbies` hold `Set`s of values. But we can apply values in
various ways:
```scala
// Vararg
Person.hobbies("golf", "chess").save

// Set
val set = Set("golf", "chess")
Person.hobbies(set).save

// Seq/List
val seq = Seq("golf", "chess")
Person.hobbies(seq).save

// Iterable
val iterable = Iterable("golf", "chess")
Person.hobbies(iterable).save
```


## Optional values

An optional value (`optionalLikes`) from a form submission for instance can be applied to an optional attribute (`likes$`):

```scala
Person.name(aName).likes$(optionalLikes).age(anAge).save
```
When this molecule is saved, only 2 facts will be asserted:

```
fredId    :person/name    "Fred"
fredId    :person/age     38
```

This is different from SQL where we would save a NULL value in a `likes` column.

Molecule lets us fetch data sets with optional facts asserted for an attribute as optional values:

```scala
Person.name.likes$.age.get === List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
```

If we specifically want to find Persons that have no `likes` asserted we can say
```scala
Person.name.likes_(nil).age.get === List(
  ("Fred", 38)
  // Pete not returned since he likes something
)
```
.. or 
```scala
Person.name.likes$(None).age.get === List(
  ("Fred", None, 38)
  // Pete not returned since he likes something
)
```



### Next

[Create / Insert...](/docs/crud/insert)