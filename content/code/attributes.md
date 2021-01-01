---
title: "Attributes"
weight: 40
menu:
  main:
    parent: code
    identifier: attributes
---

# Attributes


Molecules are built by chaining attributes together with the builder pattern. Here are some groups of different attribute types and their use with links to their manual pages:

<br>

[Attribute basics](/manual/attributes/basics), return types, arity, cardinality ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala))
```
val persons: List[(String, Int)] = Person.name.age.get
```
<br>

[Mandatory/Tacit/Optional](/manual/attributes/modes) attributes ([tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala))
```
Person.name.age.get  // all required values              ("mandatory value")
Person.name.age_.get // age is required but not returned ("tacit value")
Person.name.age$.get // optional age returned            ("optional value")
```
<br>

[Map attributes](/manual/attributes/mapped) - mapped attribute values
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap))
```
Person.id.name.get.head === (
  1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```
<br>

[Expressions](/manual/attributes/expressions) - filter attribute values with expressions
([tests](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression))
```
Person.age(42)                  // equality
Person.name.contains("John")    // fulltext search
Person.age.!=(42)               // negation
Person.age.<(42)                // comparison
Person.age(nil)                 // nil (null)
Person.name("John" or "Jonas")  // OR-logic
```
<br>

[Aggregates](/manual/attributes/aggregates) - aggregate attribute values
([tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))
```
Person.age(min) 
Person.age(max) 
// rand, sample, count, countDistinct, sum, avg, median, variance, stddev
```
<br>

[Parameterize](/manual/attributes/parameterized) - re-use molecules and let Datomic cache queries and optimize performance
(tests: 
[1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1),
[2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2),
[3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3))
```
val person = m(Person.name(?).age(?))

// Re-use `person` input molecule
val Johan  = person("John", 33).get.head
val Lisa   = person("Lisa", 27).get.head
```


## Building molecules

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/Attribute.scala)

When we have defined a schema, Molecule generates the necessary boilerplate code so that we can build "molecular data structures" by building sequences of Attributes separated with dots (the "builder pattern").

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```
Person.name.age.gender // etc
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.



### Sync API for returning data

Molecule returns all result sets as a List of tuples of values (with `get`).

```
val persons: List[(String, Int)] = Person.name.age.get
```
Data can be returned in 5 different formats:

```
// List for convenient access to smaller data sets
val list : List[(String, Int)] = m(Person.name.age).get

// Mutable Array for fastest retrieval and traversing of large data sets
val array: Array[(String, Int)] = m(Person.name.age).getArray

// Iterable for lazy traversing with an Iterator
val iterable: Iterable[(String, Int)] = m(Person.name.age).getIterable

// Json formatted string 
val json: String = m(Person.name.age).getJson

// Raw untyped Datomic data if data doesn't need to be typed
val raw: java.util.Collection[java.util.List[AnyRef]] = m(Person.name.age).getRaw
```

### Async API


Molecule provide all operations both synchronously and asynchronously, so the 5 getter methods also has equivalent asynchronous methods returning data in a Future:
```
val list    : Future[List[(String, Int)]] = m(Person.name.age).getAsync
val array   : Future[Array[(String, Int)]] = m(Person.name.age).getAsyncArray
val iterable: Future[Iterable[(String, Int)]] = m(Person.name.age).getAsyncIterable
val json    : Future[String] = m(Person.name.age).getAsyncJson
val raw     : Future[java.util.Collection[java.util.List[AnyRef]]] = m(Person.name.age).getAsyncRaw
```




### Molecule max size

The size of molecules are limited to Scala's arity limit of 22 for tuples.

If we need to insert more than 22 attribute values we can easily do this by using the entity id to work with further attributes/values:

```
// Insert maximum of 22 facts and return the created entity id
val eid = Ns.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.insert(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
).eid

// Use entity id to continue adding more values for the same entity if necessary
Ns.a23.a24.a25.insert(eid, 23, 24, 25)
```

Likewise we can retrieve more than 22 values in 2 steps

```
val first22values = Ns(eid).a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.get

// Use entity id to continue adding more values
val next3values = Ns(eid).a23.a24.a25.get
```

### Cardinality

The attributes `name`, `age` and `gender` that we saw above are typical cardinality-one attributes each with one value.

Datomic also has cardinality-many attributes that have a `Set` of values. This means that the same value cannot be saved multiple times, or that only unique values are saved. An example could be a cardinality-many attribute `hobbies` of a `Person`:

```
Person.name.hobbies.get.head === ("Fred", Set("Trains", "Chess"))
```

In the [Update](/manual/crud/update/) section of CRUD we will see how multiple values are managed with Molecule.



## 3 attribute modes

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

#### 1. Mandatory `attr`

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them.

_Note that this is different from selecting rows from a sql table where you can also get null values back!_

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated with them then this query will _not_ return those entities:

```
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


#### 2. Tacit `attr_`

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values. We call the un-returning attributes "tacit attributes".

If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age values, then we can add an underscore `_` after the `age` Attribute:

```
val names = Person.name.age_.get
```
This will return names of person entities having both a name and age Attribute set. Note how the age values are no longer returned from the type signatures:

```
val persons: List[(String, Int)] = Person.name.age.get
val names  : List[String]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures we look for.


#### 3. Optional `attr$`

[tests..](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/attr/OptionalValues.scala)


If an attribute value is only sometimes set, we can ask for it's optional value by adding a dollar sign `$` after the attribute:

```
val names: List[(String, Option[String], String)] = Person.firstName.middleName$.lastName.get
```
That way we can get all person names with or without middleNames. As you can see from the return type, the middle name is wrapped in an `Option`.


## Map Attributes

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap)

Mapped values can be saved with mapped attributes in Molecule. It's a special Molecule construct that makes it easy to save for instance multi-lingual data without having to create language-variations of each attribute. But they can also be used for any other key-value indexed data.

Say you want to save famous Persons names in multiple languages. Then you could use a mapString:

```
// In definition file
val name = mapString
 
// Insert mapped data
Person.id.name.insert(
  1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)

// Retrieve mapped data
Person.id.name.get.head === (1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```

Molecule concatenates the key and value of each pair to one of several values of an underlying cardinality-many attribute. When data is then retrieved Molecule splits the concatenated string into a typed pair. This all happens automatically and let's us focus on their use in our code.

All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity.

There's a broad range of ways we can query mapped attributes and you can see a lot of examples of their use in the [`attrMap` test cases](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/attrMap).


## Expressions

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression)

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

### Equality

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/equality)

We can apply values to Attributes in order to filter the data structures we are looking for. We could for instance find people who like pizza:

```
Person.likes.apply("pizza")
```
or simply

```
Person.likes("pizza")
```

Since the applied value "pizza" ensures that the attributes returned has this value we will get redundant information back for the `likes` attribute ("pizza" is returned for all persons):

```
Person.name.likes("pizza").get === List(
  ("Fred", "pizza"),
  ("Ben", "pizza")
)
```
This is an ideomatic place to use a tacit attribute `likes_` to say "Give me names of persons that like pizza" without returning the `likes` value "pizza" over and over again. Then we get a nice list of only the pizza likers:
```
Person.name.likes_("pizza").get === List(
  "Fred", "Ben"
)
```
_Note that since we get an arity-1 result back it is simply a list of those values._

We can apply OR-logic to find a selection of alternatives

```
Person.age(40 or 41 or 42)
// .. same as
Person.age(40, 41, 42)
// .. same as
Person.age(List(40, 41, 42))
```


### Fulltext search

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Fulltext.scala)


If we add the `fulltext` option to a String attribute definition Datomic will index the text strings saved so that we can do fulltext searches across all values. We could for instance search for Community names containing the word "Town" in their name:
```
Community.name.contains("Town")
```
Note that only full words are considered, so "Tow" won't match. Searches are case-insensitive.

Also the following common words are not considered:

```
"a", "an", "and", "are", "as", "at", "be", "but", "by",
"for", "if", "in", "into", "is", "it",
"no", "not", "of", "on", "or", "such",
"that", "the", "their", "then", "there", "these",
"they", "this", "to", "was", "will", "with"
```



### Negation

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Negation.scala)

We can exclude a certain attribute value like in "Persons that are not 42 years old":

```
Person.age.!=(42)
// or
Person.age.not(42)
```

Negate multiple values

```
Person.age.!=(40 or 41 or 42)
Person.age.!=(40, 41, 42)
Person.age.!=(List(40, 41, 42))
```


### Comparison

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Comparison.scala)

We can filer attribute values that satisfy comparison expressions:
```
Person.age.<(42)
Person.age.>(42)
Person.age.<=(42)
Person.age.>=(42)
```
Comparison of all types are performed with java's `compareTo` method. Text strings can for instance also be sorted by a letter:
```
Community.name.<("C").get(3) === List(
  "ArtsWest", "All About South Park", "Ballard Neighbor Connection")
```

### Null

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Null.scala)

We can look for non-asserted attributes (Null values) as in "Persons that have no age asserted" by applying an empty value to an attribute:
```
Person.name.age_() === // all persons where age hasn't been asserted
```
Note that the `age_` attribute has to be tacit (with an underscore) since we naturally can't return missing values.



### Applying variables

Even though Molecule introspects molecule constructions at compile time we can still use (runtime) variables for our expressions:

```
val youngAge = 25
val goodAge = 42
Person.age(goodAge)
Person.age.>(goodAge)
Person.age.<=(goodAge)
Person.age.>=(goodAge)
Person.age.!=(goodAge)
Person.age.!=(youngAge or goodAge)
```

Technically, Molecule saves the `TermName` like 'goodAge' of the variable for later resolution at runtime so that we can freely use variables in our expressions.

For now Molecule can't though evaluate arbitrary applied expressions like this one:

```
Person.birthday(new java.util.Date("2017-05-10"))
```
In this case we could instead apply the expression result to a variable and use that in the molecule:

```
val date = new java.util.Date("2017-05-10")
Person.birthday(date)
```



## Aggregates

[Core tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Aggregates.scala) |
[Example tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala)

Molecule wraps Datomic's native aggregate functions by applying special aggregate keyword objects to the attribute we want to aggregate on.

Aggregate functions either return a single value or a collection of values:


### Aggregates returning a single value

#### min/max
Applying the `min` or `max` aggregate keyword object as a value to the `age` attribute returns the lowest/highest ages.
```
Person.age(min) // lowest age
Person.age(max) // highest age
```
`min`/`max` supports all attribute types.


#### sum

```
Person.age(sum) // sum of all ages
```


#### count

Count the total number of entities with an asserted `age` value (not to be confused with `sum`).
```
Person.age(count) // count of all persons with an age (not the sum of ages)
```

#### countDistinct

Count the total number of entities with asserted _unique_ `age` values (not to be confused with `sum`).
```
Person.age(countDistinct)  // count of unique ages
```

#### avg

```
Person.age(avg) // average of all ages
```

#### median

```
Person.age(median) // median of all ages
```

#### variance

```
Person.age(variance) // variance of all ages
```

#### stddev

```
Person.age(stddev) // standard deviation of all ages
```


### Aggregates returning collections of values

#### distinct

```
Person.age(distinct) // distinct ages
```

#### min(n)

```
Person.age(min(3)) // 3 lowest ages
```

#### max(n)

```
Person.age(max(3)) // 3 highest ages
```

#### rand(n)

```
Person.age(rand(3)) // 3 random persons (with potential for duplicates)
```

#### sample(n)

```
Person.age(sample(3)) // 3 sample persons (without duplicates)
```



## Input-molecules

Tests:
[1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1),
[2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2),
[3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3)


Molecules can be parameterized by applying the input placeholder `?` as a value to an attribute. The molecule then expects input for that attribute at runtime.

By assigning parameterized "Input-molecules" to variables we can re-use those variables to query for similar data structures where only some data part varies:

```
// 1 input parameter
val person = m(Person.name(?))

val john = person("John").get.head
val lisa = person("Lisa").get.head
```

Of course more complex molecules would benefit even more from this approach.

### Datomic cache and optimization
Datomic caches and optimizes queries from input molecules so performance-wise it's a good idea to use them.


### Parameterized expressions

```
val personName  = m(Person.name(?))
val johnOrLisas = personName("John" or "Lisa").get // OR
```

### Multiple parameters
Molecules can have up to 3 `?` placeholder parameters.

```
val person      = m(Person.name(?).age(?))
val john        = person("John" and 42).get.head // AND
val johnOrJonas = person(("John" and 42) or ("Jonas" and 38)).get // AND/OR
```

### Mix parameterized and static expressions

```
val americansYoungerThan = m(Person.name.age.<(?).Country.name("USA"))
val americanKids         = americansYoungerThan(13).get
val americanBabies       = americansYoungerThan(1).get
```

For more examples, please see the [Seattle examples](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTests.scala#L136-L233) and tests for [1 input](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input1), [2 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input2), [3 inputs](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/input3)

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

