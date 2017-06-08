---
date: 2015-01-02T22:06:44+01:00
title: "Insert"
weight: 20
menu:
  main:
    parent: crud
up:   /docs/crud
prev: /docs/crud/save
next: /docs/crud/get
down: /docs/transactions
---

# Insert

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/manipulation/Insert.scala)

Multiple rows of data can be inserted by making a molecule that matches the values of each row:

```scala
Person.name.likes.age insert List(
  ("Fred", "pizza", 38),
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)
```

### Type-safety

Type-safety is guaranteed since each tuple of data is enforced by the compiler to conform to the molecule type.

If the data set is not accepted type-wise, then either the molecule needs to be adjusted to match the type
of data rows. Or, the data set might be irregular and have some variable size of tuples or varying types within tuples that
need to be sorted out. 

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
When the Fred entity is created, a Baker St Address entity is also created and a relationship from Fred to that Address 
entity is created. The same for Pete, and so on...  


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

If we have some previously saved entities we can also insert their ids. Here we save some Address entity ids 
with the ref attribute `home`:

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


## Insert-molecule as template

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



## Composite inserts

[Composite](/docs/relationships/composites/) data sets can be inserted with a special `insert` method 
that takes 3 parameter groups:

```scala
insert(<sub-molecules>)(<data>)(<optional-tx-meta-data>)
```

We could for instance insert 2 Article entities each with 2 tags:

```scala
insert(
  // 2 sub-molecules
  Article.name.author, Tag.name.weight
)(
  // 2 rows of data (Articles) 
  // The 2 sub-tuples of each row matches the 2 sub-molecules
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)() // empty optional tx meta data parameter group
```

### Transaction meta data

If we have custom transaction meta data that we want to insert with the insert transaction we could
for instance record who put in the information and what use case was involved to do so:

```scala
insert(
  // 2 sub-molecules
  Article.name.author, Tag.name.weight
)(
  // 2 rows of data (Articles) 
  // The 2 sub-tuples of each row matches the 2 sub-molecules
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)(
  MetaData.submitter_("Brenda Johnson").usecase_("AddArticles")
)
```
Transaction meta data is inserted as facts with the transaction entity id as their entity id. That makes it possible
 to afterwards know that it was Brenda that added the Articles.

We can add as much custom transaction meta data to inserts as we want.

Transaction meta data can be queried as easy as our normal data. Here we find highly weighed serious articles _that Brenda submitted:_


```scala
m(Article.name.author ~ Tag.name_("serious").weight.>=(4)
  .tx_(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```
Molecule's special generic `tx` attribute lets you fetch/apply transaction meta data. See more in [Transaction meta data](/docs/transactions/tx-meta-data/)



### Next

[Read / get...](/docs/crud/get)