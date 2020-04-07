---
title: "Insert"
weight: 20
menu:
  main:
    parent: crud
up:   /manual/crud
prev: /manual/crud/save
next: /manual/crud/get
down: /manual/transactions
---

# Insert

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/crud/Insert.scala)

Data can be inserted by making a molecule that matches the values of each row. 

One row of data can be applied directly with matching arguments

```
Person.name.likes.age.insert("Fred", "pizza", 38)
```

Multiple rows of data can be applied as any `Iterable` of tuples of data each matching the molecule attributes:
```
Person.name.likes.age insert List(
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)
```

### Type-safety

Type-safety is guaranteed since the type of each tuple of data is enforced by the compiler to conform to the molecule type.

If the data set is not accepted type-wise, then either the molecule needs to be adjusted to match the type
of data rows. Or, the data set might be irregular and have some variable size of tuples or varying types within tuples that
need to be sorted out. 



### Asynchronous insert

All transactional operators have an asynchronous equivalent. Inserting data asynchronously with 
`insertAsync` uses Datomic's asynchronous API and returns a `Future` with a `TxReport`. 

Here, we insert data as argument list/tuples asynchronously:

```
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

```
Person.name.likes$.age insert List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
```
As with `save`, None values are simply not asserted. No `likes` value is asserted for Fred in the example above.


### Related data

Related data can be inserted

```
Person.name.likes$.age.Home.street.city insert List(
  ("Fred", None, 38, "Baker St. 7", "Boston"),
  ("Pete", Some("sushi"), 17, "Sunset Boulevard 1042", "Foxville")
)
```
When the Fred entity is created, a Baker St Address entity is also created and a relationship from Fred to that Address 
entity is created. The same for Pete, and so on...  


### Composite data

Data with associative relationships can be inserted with a Composite molecule
```
Article.name.author + Tag.name.weight insert List(
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)
```
Note how each sub-molecule type-safely corresponds to each sub-tuple of data.

Up to 22 sub-molecules can be associated in a single Composite which allows for wide data sets to be saved with up
to 22 x 22 = 484 attributes per row of data!


### Data variables
Likewise we might often have the whole data set saved in a variable that we can insert too:

```
val data = List(
  ("Fred", None, 38),
  ("Pete", Some("sushi"), 17)
)
Person.name.likes$.age insert data
```

### Entity ids

If we have some previously saved entities we can also insert their ids. Here we save some Address entity ids 
with the ref attribute `home`:

```
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

```
// Insert-molecule
val insertPerson = Person.name.likes.age.insert

// Insert 3 persons re-using the insert-molecule
insertPerson("Fred", "pizza", 38)
insertPerson("Lisa", "pizza", 12)
insertPerson("Ben", "pasta", 7)
```

We can use insert-molecules with data assigned to variables too: 

```
val insertPerson = Person.name.likes.age.insert

val personsData = List(
  ("Fred", "pizza", 38),
  ("Lisa", "pizza", 7),
  ("Ben", "pasta", 5)
)

// Re-use insert-molecules with larger data sets 
insertPerson(personsData)
```


### Next

[Get...](/manual/crud/get)