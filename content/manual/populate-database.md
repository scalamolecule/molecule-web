---
date: 2015-01-02T22:06:44+01:00
title: "Populate database"
weight: 90
menu:
  main:
    parent: manual
---

# Populate database

We have [setup the database][setup] and now we want to populate it with data. 

If we recall the Seattle domain then we have some attributes organized in 3 
related namespaces:

[[images/DatomicElements3.png]]

`Community.type`, `Community.orgtype` and `District.region` all have fixed 
enumerated values to choose from. And `Community.category` is the only 
many-cardinality attribute allowed to have multiple values.


### Data molecule

When we ran `sbt compile`, Molecule also created some boilerplate code 
that allow us to insert a new Seattle Community with its related Neighborhood 
and District simply like this:

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
We can insert data by building a molecule where we apply a value to each 
attribute and then simply save it. Molecule makes sure 
that each attribute only accepts values of the expected type (in this 
case all attributes expect Strings).

Note also how we easily insert data across several namespaces in one go!


### Template molecule + data

Normally we would insert bigger data sets that we have exported from 
somewhere else. For this scenario we define a molecule where each 
attribute match a value at a certain position of our rows of data:

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

### Missing values ("Null values")

We might have some "rows" (tuples) of imported data with a missing attribute
value. If for instance some row has no `orgtype` value in the data set, we can
just use a `null` placeholder:

```scala
  ("community4", "url2", "blog", null, Set("cat3", "cat1"), "NbhName4", "DistName4", "ne"), // ...
```
In an sql table we would "insert a null value" for such column. But with
Molecule/Datomic we just simply don't assert any `orgtype` value for that 
entity at all! In other words: there is no `orgtype` fact to be saved.

### Type safety

In this example we have only inserted text strings. But all input is type
checked against the selected attributes of the molecule which makes the
insert operation type safe. We even infer the expected type so that our 
IDE will bark if it finds for instance an Integer somewhere in our input data: 

```scala
  ("community2", "url2", "type2", 42, Set("cat3", "cat1"), "NbhName2", "DistName2", "DistReg2"), // ...
```
A data set having the value `42` as a value for the `orgtype` attribute 
won't compile and our IDE will warn us of an invalid data set.


[setup]: https://github.com/marcgrue/molecule/wiki/Setup-a-Datomic-database
[populate]: https://github.com/marcgrue/molecule/wiki/Populate-the-database
[tutorial]: https://github.com/marcgrue/molecule/wiki/Molecule-Seattle-tutorial