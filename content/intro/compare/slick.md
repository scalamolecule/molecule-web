---
title: "Slick"
weight: 40
menu:
  main:
    parent: intro-compare
---

# Molecule vs Slick

Using examples from [Coming from SQL to Slick](http://slick.lightbend.com/doc/3.3.3/sql-to-slick.html#select):

#### Select all table values

```
// Slick
people.result
```
In molecule we would declare each attribute we are interested in also to infer the exact return type
```scala
// Molecule
Person.name.age.get
```

#### Select certain columns

```
people.map(p => (p.age, p.name ++ " (" ++ p.id.asColumnOf[String] ++ ")")).result
```
With Molecule we would concatenate `name` and `id` with the returned result set:
```scala
Person.age.name.e.get.map(_.map { case (age, name, id) => (age, s"$name ($id)" } )
```

#### filter / WHERE

```
people.filter(p => p.age >= 18 && p.name === "C. Vogt").result
```
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```scala
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### sortBy / ORDER BY

```
people.sortBy(p => (p.age.asc, p.name)).result
```
Ordering is applied on the result set in the application code:
```scala
Person.age.name.get.sortBy(_._1)
```

#### Aggregations

```
people.map(_.age).max.result
```
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```scala
Person.age(max).get

// or get a range of top values
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### groupBy / GROUP BY

```
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .list
```
Molecule automatically group by attributes not having an aggregate expression. In this case the query will group by `address` and calculate the average `age` for persons living there.
```scala
Person.address.age(avg).get
```

#### groupBy+filter / HAVING

```
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .filter{ case (addressId, avgAge) => avgAge > 50 }
       .map(_._1)
       .result
```
```scala
Person.address.age(avg).get.map(_.filter(_._2 > 50))
```

#### Implicit join

```
people.flatMap(p =>
  addresses.filter(a => p.addressId === a.id)
           .map(a => (p.name, a.city))
).result

// or equivalent for-expression:
(for(p <- people;
     a <- addresses if p.addressId === a.id
 ) yield (p.name, a.city)
).result
```
```scala
Person.name.Address.city.get
```

#### Explicit join

```
(people join addresses on (_.addressId === _.id))
  .map{ case (p, a) => (p.name, a.city) }.result
```
```scala
Person.name.Address.city.get
```

#### left/right/outer join

```
(addresses joinLeft people on (_.id === _.addressId))
  .map{ case (a, p) => (p.map(_.name), a.city) }.result
```
```scala
// Add `$` to attribute name to get optional values
Person.name$.Address.city.get
```

#### Subquery

```
val address_ids = addresses.filter(_.city === "New York City").map(_.id)
people.filter(_.id in address_ids).result // <- run as one query
```
```scala
Person.age.name.Address.city_("New York City").get
```

#### insert

```
people.map(p => (p.name, p.age, p.addressId))
       .insert(("M Odersky",12345,1))
```
```scala
Person.name("M Odersky").age(12345).address(1).save
```

#### update

```
people.filter(_.name === "M Odersky")
       .map(p => (p.name,p.age))
       .update(("M. Odersky",54321))
```
```scala
for {
  // Find entity id with meta Molecule attribute `e`
  oderskyId <- Person.e.name_("M Odersky").get.map(_.head)
  _ <- Person(oderskyId).name("M. Odersky").age(54321).update
} yield ()
```

#### delete

```
people.filter(p => p.name === "M. Odersky")
       .delete
```
```scala
Person.e.name_("M. Odersky").get.head.retract
```

#### case

```
people.map(p =>
  Case
    If(p.addressId === 1) Then "A"
    If(p.addressId === 2) Then "B"
).list
```
```scala
Person.address(1 or 2).get.map(_.map {
  case 1 => "A"
  case 2 => "B"
})
```





### Next

[Compare Gremlin...](/intro/compare/gremlin/)