---
title: "Mandatory/Tacit/Optional"
weight: 20
menu:
  main:
    parent: attributes
up: /manual/attributes
prev: /manual/attributes/basics
next: /manual/attributes/mapped
down: /manual/entities
---

# 3 attribute modes

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

#### 1. Mandatory `attr`

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them. 

_Note that this is different from selecting rows from a sql table where you can also get null values back!_ 

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated 
with them then this query will _not_ return those entities:

```
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


#### 2. Tacit `attr_`

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values. 
We call the un-returning attributes "tacit attributes". 

If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age 
 values, then we can add an underscore `_` after the `age` Attribute:

```
val names = Person.name.age_.get
```
This will return names of person entities having both a name and age Attribute set. Note how the age values are no 
longer returned from the type signatures:

```
val persons: List[(String, Int)] = Person.name.age.get
val names  : List[String]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures 
we look for.


#### 3. Optional `attr$` 

[tests..](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala)


If an attribute value is only sometimes set, we can ask for it's optional value by adding a dollar sign `$` after the attribute:

```
val names: List[(String, Option[String], String)] = Person.firstName.middleName$.lastName.get
```
That way we can get all person names with or without middleNames. As you can see from the return type, the middle 
name is wrapped in an `Option`.



### Next

[Map attributes...](/manual/attributes/mapped)