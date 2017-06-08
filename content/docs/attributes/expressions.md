---
date: 2015-01-02T22:06:44+01:00
title: "Expressions"
weight: 40
menu:
  main:
    parent: attributes
up: /docs/attributes
prev: /docs/attributes/mapped
next: /docs/attributes/aggregates
down: /docs/entities
---

# Expressions

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/expression)

### Equality

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Equality.scala)

We can apply values to Attributes in order to filter the data structures we are looking for. We could for instance find people who like pizza:

```scala
Person.likes.apply("pizza")
```
or simply

```scala
Person.likes("pizza")
```

Since the applied value "pizza" ensures that the attributes returned has this value we will get redundant information back for the `likes` 
attribute ("pizza" is returned for all persons):

```scala
Person.name.likes("pizza").get === List(
  ("Fred", "pizza"),
  ("Ben", "pizza")
)
```
This is an ideomatic place to use a tacet attribute `likes_` to say "Give me names of persons that like pizza" without returning the `likes` value "pizza"
over and over again. Then we get a nice list of only the pizza likers:
```scala
Person.name.likes_("pizza").get === List(
  "Fred", "Ben"
)
```
_Note that since we get an arity-1 result back it is simply a list of those values._


### Fulltext search

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/FulltextSearch.scala)


If we add the `fullTextSearch` option to a String attribute definition Datomic will index the text strings saved so that we can do 
fulltext searches accross all values. We could for instance search for Community names containing the word "Town" in their name:
```scala
Community.name.contains("Town")
```
Note that only full words are considered, so "Tow" won't match. 

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

```scala
Person.age.!=(42)
// or
Person.age.not(42)
```


### Comparison

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Comparison.scala)

We can filer attribute values that satisfy comparison expressions:
```scala
Person.age.<(42)
Person.age.>(42)
Person.age.<=(42)
Person.age.>=(42)
```
Comparison of all types are performed with java's `compareTo` method. Text strings can for instance also be sorted by a letter:
```scala
Community.name.<("C").get(3) === List(
  "ArtsWest", "All About South Park", "Ballard Neighbor Connection")
```

### Nil (null)

We can look for non-asserted attributes (like Null values) as in "Persons that have no age asserted"

```scala
Person.age(nil) === // all persons where age hasn't been asserted
```
`nil` is a Molecule keyword object made available with the `molecule._` import.



### OR-logic

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Logic.scala)

We can apply OR-logic to find a selection of alternatives

```scala
Person.age(40 or 41 or 42)
// .. same as
Person.age(40, 41, 42)
// .. same as
Person.age(List(40, 41, 42))
```

With negations we can again apply multiple values as alternatives

```scala
Person.age.!=(40 or 41 or 42)
Person.age.!=(40, 41, 42)
Person.age.!=(List(40, 41, 42))
```


## We can use variables too

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



### Next

[Aggregate attributes...](/docs/attributes/aggregates)