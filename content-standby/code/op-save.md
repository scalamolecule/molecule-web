---
title: Save / Insert
weight: 51
menu:
  main:
    parent: documentation
    identifier: code-op-save
---


# Save

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/Save.scala)

In Molecule we can populate a molecule with data and save it:

```scala
Person.name("Fred").likes("pizza").age(38).save
```

This will assert 3 facts in Datomic that all share the id of the new entity id `fredId` that is automatically created by Datomic:

```scala
fredId    :Person/name    "Fred"
fredId    :Person/likes   "pizza"
fredId    :Person/age     38
```

### Type-safety

Type-safety is guaranteed since each attribute only accepts values of its defined type.


### Asynchronous save

All transactional operators have an asynchronous equivalent. Saving data asynchronously with `saveAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we map over the result of saving asynchronously:

```scala
// Map over a Future
Person.name("Fred").likes("pizza").age(42).saveAsync.map { tx => // tx report from successful save transaction
  // (synchronous get)
  Person.name.likes.age.get.map(_.head ==> ("Ben", "pizza", 42)
}
```

Or we could defer the resolution of the `Future`

```scala
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
```scala
Person.name("Fred").likes("pizza").age(38).Home.street("Baker St. 7").city("Boston").save
```
In this case, 6 facts will be asserted for the entity of Fred. A `:Person/home` ref attribute will resolve to the value of a new Address entity with id `addrId` and thereby establish the relationship from Fred to his Address:

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

Cardinality many attributes like for instance `hobbies` hold `Set`s of values. But we can apply values in various ways:
```scala
// Vararg
Person.hobbies("golf", "chess").save

// Set
val set = Set("golf", "chess")
Person.hobbies(set).save

// Seq/List
val seq = Seq("golf", "chess")
Person.hobbies(seq).save
```


### Optional values

An optional value (`optionalLikes`) from a form submission for instance can be applied to an optional attribute (`likes$`):

```scala
Person.name(aName).likes$(optionalLikes).age(anAge).save
```
When this molecule is saved, only 2 facts will be asserted:

```
fredId    :Person/name    "Fred"
fredId    :Person/age     38
```

This is different from SQL where we would save a NULL value in a `likes` column.

Molecule lets us fetch data sets with optional facts asserted for an attribute as optional values:

```scala
Person.name.likes$.age.get.map(_ ==> List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
```

If we specifically want to find Persons that have no `likes` asserted we can say
```scala
Person.name.likes_(nil).age.get.map(_ ==> List(
  ("Fred", 38)
  // Pete not returned since he likes something
)
```
.. or
```scala
Person.name.likes$(None).age.get.map(_ ==> List(
  ("Fred", None, 38)
  // Pete not returned since he likes something
)
```

## Insert

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/crud/Insert.scala)

Data can be inserted by making a molecule that matches the values of each row.

One row of data can be applied directly with matching arguments

```scala
Person.name.likes.age.insert("Fred", "pizza", 38)
```

Multiple rows of data can be applied as any `Iterable` of tuples of data each matching the molecule attributes:
```scala
Person.name.likes.age insert List(
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)
```

### Type-safety

Type-safety is guaranteed since the type of each tuple of data is enforced by the compiler to conform to the molecule type.

If the data set is not accepted type-wise, then either the molecule needs to be adjusted to match the type of data rows. Or, the data set might be irregular and have some variable size of tuples or varying types within tuples that need to be sorted out.



### Asynchronous insert

All transactional operators have an asynchronous equivalent. Inserting data asynchronously with `insertAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`.

Here, we insert data as argument list/tuples asynchronously:

```scala
// Insert single row of data with individual args
val singleInsertFuture: Future[TxReport] = Person.name.likes.age.insertAsync("Fred", "pizza", 38)

// Insert Iterable of multiple rows of data
val multipleInsertFuture: Future[TxReport] = Person.name.likes.age insertAsync List(
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)

for {
  _ <- singleInsertFuture
  _ <- multipleInsertFuture
  result <- Person.name.likes.age.getAsync
} yield {
  // Both inserts applied
  result === List(
    ("Fred", "pizza", 38),
    ("Lisa", "pizza", 7),
    ("Ben", "pasta", 5)
  )
}
```
For brevity, the following examples use the synchronous `save` operation.


### Optional values

`null` values are not allowed as data-input values whereas Optional values are:

```scala
Person.name.likes$.age insert List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
```
As with `save`, None values are simply not asserted. No `likes` value is asserted for Fred in the example above.


### Related data

Related data can be inserted

```scala
Person.name.likes$.age.Home.street.city insert List(
  ("Fred", None, 38, "Baker St. 7", "Boston"),
  ("Pete", Some("sushi"), 17, "Sunset Boulevard 1042", "Foxville")
)
```
When the Fred entity is created, a Baker St Address entity is also created and a relationship from Fred to that Address entity is created. The same for Pete, and so on...


### Composite data

Data with associative relationships can be inserted with a Composite molecule
```scala
Article.name.author + Tag.name.weight insert List(
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)
```
Note how each sub-molecule type-safely corresponds to each sub-tuple of data.

Up to 22 sub-molecules can be associated in a single Composite which allows for wide data sets to be saved with up to 22 x 22 = 484 attributes per row of data!


### Data variables
Likewise we might often have the whole data set saved in a variable that we can insert too:

```scala
val data = List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
Person.name.likes$.age insert data
```

### Entity ids

If we have some previously saved entities we can also insert their ids. Here we save some Address entity ids with the ref attribute `home`:

```scala
val bakerSt7 = Addr.street("Baker St. 7").city("Boston").save.eid
val sunsetB = Addr.street("Sunset Boulevard 1042").city("Foxville").save.eid

Person.name.likes$.age.home insert List(
  ("Fred", Some("pizza"), 38, bakerSt7),
  ("Lisa", None, 12, bakerSt7),
  ("Ben", Some("pasta"), 7, bakerSt7),
  ("Pete", Some("sushi"), 17, sunsetB)
)
```


### Insert-molecule as template

We can assign an Insert-molecule to a variable in order to re-use it as a temple to insert data with various inputs.

```scala
// Insert-molecule
val insertPerson = Person.name.likes.age.insert

// Insert 3 persons re-using the insert-molecule
insertPerson("Fred", "pizza", 38)
insertPerson("Lisa", "pizza", 12)
insertPerson("Ben", "pasta", 7)
```

We can use insert-molecules with data assigned to variables too:

```scala
val insertPerson = Person.name.likes.age.insert

val personsData = List(
  ("Fred", "pizza", 38),
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)

// Re-use insert-molecules with larger data sets 
insertPerson(personsData)
```
