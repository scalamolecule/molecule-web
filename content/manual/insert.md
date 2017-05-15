---
date: 2015-01-02T22:06:44+01:00
title: "Insert"
weight: 40
menu:
  main:
    parent: manual
    identifier: insert
---

# Insert data

We basically have 3 ways of entering data with Molecule:

### 1. Data-molecule

We can insert data by populating a molecule with data by applying a value to each 
attribute and then simply `save` it. Molecule makes sure 
that each attribute only accepts values of the expected type.

```scala
Community.insert
  .name("AAA")
  .url("myUrl")
  .`type`("twitter")
  .orgtype("personal")
  .category("my", "favorites") // many cardinality allows multiple values
  .Neighborhood.name("myNeighborhood")
  .District.name("myDistrict").region("nw").save
```

Note also how we easily insert data across several namespaces in one go!


### 2. Insert-molecule

Normally we would insert bigger data sets that we have exported from 
somewhere else. For this scenario we define a molecule where each 
attribute defines what type of data we can receive in a list of data tuples:

```scala
Community.name.url.`type`.orgtype.category
    .Neighborhood.name.District.name.region insert List(
  ("community1", "url1", "twitter", "community", Set("cat1", "cat2"), 
    "NbhName1", "DistName1", "e"),
  ("community2", "url2", "myspace", "nonprofit", Set("cat3", "cat1"), 
    "NbhName2", "DistName2", "nw"),
  ("community3", "url3", "website", "personal", Set("cat1", "cat2"), 
    "NbhName3", "DistName3", "w"),
  // etc..
)
```
Note how we insert data accross namespaces here too.

### 3. Insert-molecule as template

We can assign an Insert-molecule to a variable in order to re-use it as a temple to insert data with various inputs.

```scala
// Define Insert-molecule
val insertPerson = Person.firstName.lastName.age.insert

// Re-use Insert-molecule by aplying different (type-inferred) data sets
insertPerson("John", "Doe", 33)
insertPerson("Lisa", "Tux", 27)
// etc...
```
This makes it easy to insert similar data sets.

### Optional values ("Null values")

We might have some "rows" (tuples) of imported data with an optional attribute
value. If for instance some row has no `orgtype` value in the data set, we can
just use `None`:

```scala
  ("community4", "url2", "blog", None, Set("cat3", "cat1"), "NbhName4", "DistName4", "ne"), // ...
```
(we need to type-cast it for the implicits to resolve correctly)

#### Difference to SQL

In an sql table we would have inserted a null value for such column. But with
Molecule/Datomic we just simply _don't assert_ any `orgtype` value for that 
entity at all! In other words: there is no `orgtype` fact to be saved.

### Type safety

In this example we have only inserted text strings. But all input is type
checked against the selected attributes of the molecule which makes the
insert operation type safe. 

We even infer the expected type so that our 
IDE will bark if it finds for instance an Integer somewhere in our input data: 

```scala
  ("community2", "url2", "type2", 42, Set("cat3", "cat1"), "NbhName2", "DistName2", "DistReg2"), // ...
```
A data set having the value `42` as a value for the `orgtype` attribute 
woudn't compile and our IDE will infer that and warn us of an invalid data set.