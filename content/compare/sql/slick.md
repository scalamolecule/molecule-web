---
date: 2014-05-14T02:13:50Z
title: "Slick"
weight: 20
menu:
  main:
    parent: sql
---

# Slick vs. Molecule examples

Using the examples from [Slick](http://slick.typesafe.com/doc/3.0.0-M1/sql-to-slick.html#sql-vs-slick-examples)...

#### Select all table values

```
// Slick
people.run
```
In molecule we would declare each attribute we are interested in also to infer the exact return type
```scala
// Molecule
Person.name.age.get
```

#### Select certain columns

```
people.map(p => (p.age, p.name ++ " (" ++ p.id.asColumnOf[String] ++ ")")).run
```
With Molecule we would concatenate `name` and `id` with the returned result set:
```scala
Person.age.name.e.get map { case (age, name, id) => (age, s"$name ($id)" }
```

#### filter / WHERE

```scala
people.filter(p => p.age >= 18 && p.name === "C. Vogt").run
```
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```scala
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### sortBy / ORDER BY

```scala
people.sortBy(p => (p.age.asc, p.name)).run
```
Ordering is applied on the result set:
```scala
Person.age.name.get sortBy(_._1)
```

#### Aggregations

```scala
people.map(_.age).max.run
```
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```scala
Person.age(max).get

// or get a range of top values
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### groupBy / GROUP BY

```scala
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .list
```
```scala
// TODO
Person.address(groupBy).age(avg)).get
```

#### groupBy+filter / HAVING

```scala
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .filter{ case (addressId, avgAge) => avgAge > 50 }
       .map(_._1)
       .run
```
```scala
// TODO
Person.address(groupBy).age_(avg > 50)).get
```

#### Implicit join

```scala
people.flatMap(p =>
  addresses.filter(a => p.addressId === a.id)
           .map(a => (p.name, a.city))
).run

// or equivalent for-expression:
(for(p <- people;
     a <- addresses if p.addressId === a.id
 ) yield (p.name, a.city)
).run
```
```scala
Person.name.Address.city.get
```

#### Explicit join

```scala
(people join addresses on (_.addressId === _.id))
  .map{ case (p, a) => (p.name, a.city) }.run
```
```scala
Person.name.Address.city.get
```

#### left/right/outer join

```scala
(addresses joinLeft people on (_.id === _.addressId))
  .map{ case (a, p) => (p.map(_.name), a.city) }.run
```
```scala
// TODO
Person.name(maybe).Address.city.get
```

#### Subquery

```scala
val address_ids = addresses.filter(_.city === "New York City").map(_.id)
people.filter(_.id in address_ids).run // <- run as one query
```
```scala
Person.age.name.Address.city_("New York City").get
```

#### Scalar value subquery / custom function

```scala
val rand = SimpleFunction.nullary[Double]("RAND")

val rndId = (people.map(_.id).max.asColumnOf[Double] * rand).asColumnOf[Int]

people.filter(_.id >= rndId)
       .sortBy(_.id)
       .first
```
```scala
// TODO
```

#### insert

```scala
people.map(p => (p.name, p.age, p.addressId))
       .insert(("M Odersky",12345,1))
```
```scala
Person.name("M Odersky").age(12345).address(1).add
```

#### update

```scala
people.filter(_.name === "M Odersky")
       .map(p => (p.name,p.age))
       .update(("M. Odersky",54321))
```
```scala
val odersky = Person.name("M Odersky").get.id
Person(odersky).name("M. Odersky").age(54321).update
```

#### delete

```scala
people.filter(p => p.name === "M. Odersky")
       .delete
```
```scala
Person.name("M. Odersky").get.id.delete
```

#### case

```scala
people.map(p =>
  Case
    If(p.addressId === 1) Then "A"
    If(p.addressId === 2) Then "B"
).list
```
```scala
Person.Address.e(1 or 2).get map {
  case 1 => "A"
  case 2 => "B"
}
```
































