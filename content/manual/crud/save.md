---
title: "Save"
weight: 10
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud
next: /manual/crud/insert
down: /manual/transactions
---

# Save

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/Save.scala)

In Molecule we can populate a molecule with data and save it:

```
Person.name("Fred").likes("pizza").age(38).save
```

This will assert 3 facts in Datomic that all share the id of the new entity id `fredId` that is automatically created by Datomic:

```
fredId    :Person/name    "Fred"
fredId    :Person/likes   "pizza"
fredId    :Person/age     38
```

### Type-safety

Type-safety is guaranteed since each attribute only accepts values of its defined type.


### Asynchronous save

All transactional operators have an asynchronous equivalent. Saving data asynchronously with 
`saveAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`. 

Here, we map over the result of saving asynchronously:

```
// Map over a Future
Person.name("Fred").likes("pizza").age(42).saveAsync.map { tx => // tx report from successful save transaction
  // (synchronous get)
  Person.name.likes.age.get.head === ("Ben", "pizza", 42)
}
```

Or we could defer the resolution of the `Future`

```
val futureSave: Future[TxReport] = Person.name("Fred").likes("pizza").age(42).saveAsync
for {
  _ <- futureSave
  result <- Person.name.likes.age.getAsync
} yield {
  // Data was saved
  result.head === ("Ben", "pizza", 42)
}
```

For brevity, the following examples use the synchronous `save` operation.



### Related data

We can even save related date in the same operation
```
Person.name("Fred").likes("pizza").age(38).Home.street("Baker St. 7").city("Boston").save
```
In this case, 6 facts will be asserted for the entity of Fred. A `:Person/home` ref attribute will resolve to the
value of a new Address entity with id `addrId` and thereby establish the relationship from Fred to his Address:

```
fredId    :Person/name    "Fred"
fredId    :Person/likes   "pizza"
fredId    :Person/age     38
fredId    :Person/home    addrId
addrId    :Addr/street    "Baker St. 7"
addrId    :Addr/city      "Boston"
```
And we could go on with further relationships...

### Cardinality many values

Cardinality many attributes like for instance `hobbies` hold `Set`s of values. But we can apply values in
various ways:
```
// Vararg
Person.hobbies("golf", "chess").save

// Set
val set = Set("golf", "chess")
Person.hobbies(set).save

// Seq/List
val seq = Seq("golf", "chess")
Person.hobbies(seq).save
```


## Optional values

An optional value (`optionalLikes`) from a form submission for instance can be applied to an optional attribute (`likes$`):

```
Person.name(aName).likes$(optionalLikes).age(anAge).save
```
When this molecule is saved, only 2 facts will be asserted:

```
fredId    :Person/name    "Fred"
fredId    :Person/age     38
```

This is different from SQL where we would save a NULL value in a `likes` column.

Molecule lets us fetch data sets with optional facts asserted for an attribute as optional values:

```
Person.name.likes$.age.get === List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
```

If we specifically want to find Persons that have no `likes` asserted we can say
```
Person.name.likes_(nil).age.get === List(
  ("Fred", 38)
  // Pete not returned since he likes something
)
```
.. or 
```
Person.name.likes$(None).age.get === List(
  ("Fred", None, 38)
  // Pete not returned since he likes something
)
```



### Next

[Create / Insert...](/manual/crud/insert)