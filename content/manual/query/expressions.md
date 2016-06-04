---
date: 2015-01-02T22:06:44+01:00
title: "Expressions"
weight: 30
menu:
  main:
    parent: query
---

# Expressions

(See [expression tests](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule/expression))

### Equality

We can apply values to Attributes in order to filter the data structures we are looking for. We could for instance look for names of female persons:

```scala
Person.age.apply(42)
```
or simply

```scala
Person.age(42)
```

### OR-logic

We can apply OR-logic to find a selection of alternatives

```scala
Person.age(40 or 41 or 42)
// .. same as
Person.age(40, 41, 42)
// .. same as
Person.age(List(40, 41, 42))
```


### Negation

We can exclude a certain attribute value

```scala
Person.age.!=(42)
// or
Person.age.not(42)
```

With negations we can again apply multiple values as alternatives

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
Community.name.<("C").get(3) === List(
  "ArtsWest", "All About South Park", "Ballard Neighbor Connection")
```


### Fulltext search

If we add the `fullTextSearch` option to a String attribute definition Datomic will index the text strings saved so that we can do fulltext searches accross all values. We could for instance search for Community names containing the word "Town" in their name:
```scala
Community.name.contains("Town")
```
Note that only full words are considered, so "Tow" won't match. Also the following common words are not considered:

```
"a", "an", "and", "are", "as", "at", "be", "but", "by",
"for", "if", "in", "into", "is", "it",
"no", "not", "of", "on", "or", "such",
"that", "the", "their", "then", "there", "these",
"they", "this", "to", "was", "will", "with"
```

### We can use variables too

Even though Molecule introspects molecule constructions at compile time we can still use (runtime) variables for our expressions

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
Technically, Molecule saves the TermName of the variable for later resolution at runtime so that we can freely use variables in our expressions.

```scala
val ages = List(youngAge, goodAge)
Person.age(goodAge)
Person.age.>(goodAge)
Person.age.<=(goodAge)
Person.age.>=(goodAge)
Person.age.!=(goodAge)
Person.age.!=(youngAge or goodAge)
// etc...
```