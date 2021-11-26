---
title: "Attributes"
weight: 20
menu:
  main:
    parent: manual
---

# Attributes

Molecules are built by chaining attributes together with the builder pattern. 

Molecule then transforms the constructed molecule to a query string at compile time. Molecules can be constructed explicitly with the `m` method. But generally the implicit call is used.

We could for instance build a molecule representing the data structure of Persons with name, age and gender Attributes:

```scala
// Explicit `m` macro call
m(Person.name.age.gender).get

// Implicit `m` macro call
Person.name.age.gender.get
```
The fundamental building blocks are Namespaces like `Person` and Attributes like `name`, `age` and `gender`. Namespaces are simply prefixes to Attribute names to avoid name clashes and to group our Attributes in meaningful ways according to our domain.

As you see we start our molecule from some Namespace and then build on Attribute by Attribute.


### Molecule max size

The size of molecules are limited to Scala's arity limit of 22 for tuples.

But we can create a [composite molecule](/manual/relationships/#composite-molecules) with up to 22 x 22 = 484 attributes!



### Cardinality

The attributes `name`, `age` and `gender` that we saw above are typical cardinality-one attributes each with one value.

Datomic also has cardinality-many attributes that have a `Set` of values. This means that the same value cannot be saved multiple times, or that only unique values are saved. An example could be a cardinality-many attribute `hobbies` of a `Person`:

```scala
Person.name.hobbies.get.map(_.head ==> ("John", Set("Trains", "Chess")))
```

In the [Update](/manual/transactions/#update) section of CRUD we will see how multiple values are managed with Molecule.












## 3 attribute modes


#### 1. Mandatory `attr`

When we use a molecule to query the Datomic database we ask for entities having all our Attributes associated with them.

_Note that this is different from selecting rows from a sql table where you can also get null values back!_

If for instance we have entities representing Persons in our data set that haven't got any age Attribute associated with them then this query will _not_ return those entities:

```scala
val persons = Person.name.age.get
```
Basically we look for **matches** to our molecule data structure.


#### 2. Tacit `attr_`

Sometimes we want to grap entities that we _know_ have certain attributes, but without returning those values. We call the un-returning attributes "tacit attributes".

If for instance we wanted to find all names of Persons that have an age attribute set but we don't need to return those age values, then we can add an underscore `_` after the `age` Attribute:

```scala
val names = Person.name.age_.get
```
This will return a Future with names of person entities having both a name and age Attribute set. Note how the age values are no longer returned from the type signatures:

```scala
val persons: Future[List[(String, Int)]] = Person.name.age.get
val names  : Future[List[String]]        = Person.name.age_.get
```
This way we can switch on and off individual attributes from the result set without affecting the data structures we look for.


#### 3. Optional `attr$`


If an attribute value is only sometimes set, we can ask for it's optional value by adding a dollar sign `$` after the attribute:

```scala
val names: Future[List[(String, Option[String], String)]] = Person.firstName.middleName$.lastName.get
```
That way we can get all person names with or without middleNames. As you can see from the return type, the middle name is wrapped in an `Option`.











## Map Attributes


Mapped values can be saved with mapped attributes in Molecule. It's a special Molecule construct that makes it easy to save for instance multi-lingual data without having to create language-variations of each attribute. But they can also be used for any other key-value indexed data.

Say you want to save famous Persons names in multiple languages. Then you could use a mapString:

```scala
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
Person.id.name.get.map(_.head ==> (1, 
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
))
```

Molecule concatenates the key and value of each pair to one of several values of an underlying cardinality-many attribute. When data is then retrieved Molecule splits the concatenated string into a typed pair. This all happens automatically and let's us focus on their use in our code.
















## Expressions



### Equality


We can apply values to Attributes in order to filter the data structures we are looking for. We could for instance find people who like pizza:

```scala
Person.likes.apply("pizza")
```
or simply

```scala
Person.likes("pizza")
```

Since the applied value "pizza" ensures that the attributes returned has this value we will get redundant information back for the `likes` attribute ("pizza" is returned for all persons):

```scala
Person.name.likes("pizza").get.map(_ ==> List(
  ("John", "pizza"),
  ("Ben", "pizza")
))
```
This is an ideomatic place to use a tacit attribute `likes_` to say "Give me names of persons that like pizza" without returning the `likes` value "pizza" over and over again. Then we get a nice list of only the pizza likers:
```scala
Person.name.likes_("pizza").get.map(_ ==> List(
  "John", "Ben"
))
```
_Note that since we get an arity-1 result back it is simply a list of those values._

We can apply OR-logic to find a selection of alternatives

```scala
Person.age(40 or 41 or 42)
// .. same as
Person.age(40, 41, 42)
// .. same as
Person.age(List(40, 41, 42))
```


### Fulltext search



If we add the `fulltext` option to a String attribute definition Datomic will index the text strings saved so that we can do fulltext searches across all values. We could for instance search for Community names containing the word "Town" in their name:
```scala
Community.name.contains("Town")
```
Note that only full words are considered, so "Tow" won't match. Searches are case-insensitive.

Also, the following common words are not considered:

```
"a", "an", "and", "are", "as", "at", "be", "but", "by",
"for", "if", "in", "into", "is", "it",
"no", "not", "of", "on", "or", "such",
"that", "the", "their", "then", "there", "these",
"they", "this", "to", "was", "will", "with"
```



### Negation

We can exclude a certain attribute value like in "Persons that are not 42 years old":

```scala
Person.age.!=(42)
// or
Person.age.not(42)
```

Negate multiple values

```scala
Person.age.!=(40 or 41 or 42)
Person.age.!=(40, 41, 42)
Person.age.!=(List(40, 41, 42))
```


### Comparison


We can filer attribute values that satisfy comparison expressions:
```scala
Person.age.<(42)
Person.age.>(42)
Person.age.<=(42)
Person.age.>=(42)
```
Comparison of all types are performed with java's `compareTo` method. Text strings can for instance also be sorted by a letter:
```scala
Community.name.<("C").get(3).map(_ ==> List(
  "ArtsWest", "All About South Park", "Ballard Neighbor Connection"
))
```

### Null


We can look for non-asserted attributes (Null values) as in "Persons that have no age asserted" by applying an empty value to an attribute:
```scala
Person.name.age_() === // all persons where age hasn't been asserted
```
Note that the `age_` attribute has to be tacit (with an underscore) since we naturally can't return missing values.



### Applying variables

Even though Molecule introspects molecule constructions at compile time we can still use (runtime) variables for our expressions:

```scala
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

```scala
Person.birthday(new java.util.Date("2017-05-10"))
```
In this case we could instead apply the expression result to a variable and use that in the molecule:

```scala
val date = new java.util.Date("2017-05-10")
Person.birthday(date)
```



## Aggregates


Molecule wraps Datomic's native aggregate functions by applying special aggregate keyword objects to the attribute we want to aggregate on.

Aggregate functions either return a single value or a collection of values:


### Aggregates returning a single value

#### min/max
Applying the `min` or `max` aggregate keyword object as a value to the `age` attribute returns the lowest/highest ages.
```scala
Person.age(min) // lowest age
Person.age(max) // highest age
```
`min`/`max` supports all attribute types.


#### sum

```scala
Person.age(sum) // sum of all ages
```


#### count

Count the total number of entities with an asserted `age` value (not to be confused with `sum`).
```scala
Person.age(count) // count of all persons with an age (not the sum of ages)
```

#### countDistinct

Count the total number of entities with asserted _unique_ `age` values (not to be confused with `sum`).
```scala
Person.age(countDistinct)  // count of unique ages
```

#### avg

```scala
Person.age(avg) // average of all ages
```

#### median

```scala
Person.age(median) // median of all ages
```

#### variance

```scala
Person.age(variance) // variance of all ages
```

#### stddev

```scala
Person.age(stddev) // standard deviation of all ages
```


### Aggregates returning collections of values

#### distinct

```scala
Person.age(distinct) // distinct ages
```

#### min(n)

```scala
Person.age(min(3)) // 3 lowest ages
```

#### max(n)

```scala
Person.age(max(3)) // 3 highest ages
```

#### rand(n)

```scala
Person.age(rand(3)) // 3 random persons (with potential for duplicates)
```

#### sample(n)

```scala
Person.age(sample(3)) // 3 sample persons (without duplicates)
```



## Input-molecules

Molecules can be parameterized by applying the input placeholder `?` as a value to an attribute. The molecule then expects input for that attribute at runtime.

By assigning parameterized "Input-molecules" to variables we can re-use those variables to query for similar data structures where only some data part varies:

```scala
// 1 input parameter
val person = m(Person.name(?))

val john = person("John").get.head
val lisa = person("Lisa").get.head
```

Of course more complex molecules would benefit even more from this approach.

### Datomic cache and optimization
Datomic caches and optimizes queries from input molecules so performance-wise it's a good idea to use them.


### Parameterized expressions

```scala
val personName  = m(Person.name(?))
val johnOrLisas = personName("John" or "Lisa").get // OR
```

### Multiple parameters
Molecules can have up to 3 `?` placeholder parameters.

```scala
val person      = m(Person.name(?).age(?))
val john        = person("John" and 24).get.map(_.head) // AND
val johnOrJonas = person(("John" and 24) or ("Lisa" and 20)).get // AND/OR
```

### Mix parameterized and static expressions

```scala
val americansYoungerThan = m(Person.name.age.<(?).Country.name("USA"))
val americanKids         = americansYoungerThan(13).get
val americanBabies       = americansYoungerThan(1).get
```




### Next

[Relationships...](/manual/relationships/)


