---
date: 2015-01-02T22:06:44+01:00
title: "Composites"
weight: 80
menu:
  main:
    parent: query
---

# Composites

<br>


![](/img/DatomicElements2.png)

_Entities can have attributes from **any** namespace_

## Composite inserts

A core feature of Datomic is that any attribute value can be associated with an entity. This allow us 
to "compose" entities with attribute values from un-related namespaces. 

Say for instance if we want to tag various part of our domain. It would be tedious and counter-intuitive 
to have references between every single namespace to a tag namespace. Tags are not an intrinsic value of most 
entities and are therefore better modelled in isolation.

Molecule allow us to make "composites" that will type-safely bring tag data together with any entity. We could 
for instance create 2 Article entities each with 2 tags:

```scala
insert(
  Article.name.author ~ Tag.name.weight
)(
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)()
```
The `~` method merges the involved sub-molecules (could be up to 22!) into one composite model where all 
sub-molecules share the entity id. Here we just created two entities each with 4 values of the 4 attributes.
No need to make a reference from Articles to Tags or vice versa. The relationship is established through
the shared entity id.

We could add tags to any other part of our domain - and even some transaction meta data: 

```scala
insert(
  Review.title.text ~ Tag.name.weight
)(
  (("Okay", "Well, maybe"), ("noise", 1)),
  (("Excellent", "Go buy it!"), ("serious", 4))
)(
  MetaData.submitter_("Brenda Johnson").usecase_("AddReviews")
)
```
Note that each "row" of data is a tuple with 2 tuples of type-checked data matching the 2 sub-molecules.


## Composite queries

Use any part of the composite data to filter out what we are looking for:

```scala
// All articles
m(Article.name.author ~ Tag.name.weight).get === List(
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)

// Fun articles by John Cleese
m(Article.name.author("John Cleese") ~ Tag.name_("fun")).get === List(
  (("Best jokes ever", "John Cleese"))
)

// Important articles
m(Article.name.author ~ Tag.weight.>=(4)).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```

And we could even combine our filtering with transaction meta data

```scala
// Important articles submitted by Brenda Johnson
m(Article.name.author ~ Tag.weight.>=(4).tx_(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```

## Arity 22+ molecules

Since composites are composed of up to 22 sub-molecules we could potentially insert and retrieve 
mega composite molecules with up to 22 x 22 = 484 attributes, although the compiler probably wouldn't 
be too happy about that. 

Compile time for molecules longer than about 14-16 attributes long tend to increase sharply, so simply 
splitting up long molecules in 2 or more sub-molecules easily keeps compilation fast.