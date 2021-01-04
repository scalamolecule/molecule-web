---
title: "SQL / Slick"
weight: 40
menu:
  main:
    parent: compare
---

# Molecule vs Slick

Using examples from [Coming from SQL to Slick](http://slick.lightbend.com/doc/3.3.3/sql-to-slick.html#select):

#### Select all table values

<pre class="clean">
// Slick
people.result
</pre>
In molecule we would declare each attribute we are interested in also to infer the exact return type
```
// Molecule
Person.name.age.get
```

#### Select certain columns

<pre class="clean">
people.map(p => (p.age, p.name ++ " (" ++ p.id.asColumnOf[String] ++ ")")).result
</pre>
With Molecule we would concatenate `name` and `id` with the returned result set:
```
Person.age.name.e.get map { case (age, name, id) => (age, s"$name ($id)" }
```

#### filter / WHERE

<pre class="clean">
people.filter(p => p.age >= 18 && p.name === "C. Vogt").result
</pre>
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### sortBy / ORDER BY

<pre class="clean">
people.sortBy(p => (p.age.asc, p.name)).result
</pre>
Ordering is applied on the result set in the application code:
```
Person.age.name.get.sortBy(_._1)
```

#### Aggregations

<pre class="clean">
people.map(_.age).max.result
</pre>
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```
Person.age(max).get

// or get a range of top values
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### groupBy / GROUP BY

<pre class="clean">
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .list
</pre>
Molecule automatically group by attributes not having an aggregate expression. In this case the query will group by `address` and calculate the average `age` for persons living there.
```
Person.address.age(avg).get
```

#### groupBy+filter / HAVING

<pre class="clean">
people.groupBy(p => p.addressId)
       .map{ case (addressId, group) => (addressId, group.map(_.age).avg) }
       .filter{ case (addressId, avgAge) => avgAge > 50 }
       .map(_._1)
       .result
</pre>
```
Person.address.age(avg).get.filter(_._2 > 50)
```

#### Implicit join

<pre class="clean">
people.flatMap(p =>
  addresses.filter(a => p.addressId === a.id)
           .map(a => (p.name, a.city))
).result

// or equivalent for-expression:
(for(p <- people;
     a <- addresses if p.addressId === a.id
 ) yield (p.name, a.city)
).result
</pre>
```
Person.name.Address.city.get
```

#### Explicit join

<pre class="clean">
(people join addresses on (_.addressId === _.id))
  .map{ case (p, a) => (p.name, a.city) }.result
</pre>
```
Person.name.Address.city.get
```

#### left/right/outer join

<pre class="clean">
(addresses joinLeft people on (_.id === _.addressId))
  .map{ case (a, p) => (p.map(_.name), a.city) }.result
</pre>
```
// Add `$` to attribute name to get optional values
Person.name$.Address.city.get
```

#### Subquery

<pre class="clean">
val address_ids = addresses.filter(_.city === "New York City").map(_.id)
people.filter(_.id in address_ids).result // <- run as one query
</pre>
```
Person.age.name.Address.city_("New York City").get
```

#### insert

<pre class="clean">
people.map(p => (p.name, p.age, p.addressId))
       .insert(("M Odersky",12345,1))
</pre>
```
Person.name("M Odersky").age(12345).address(1).save
```

#### update

<pre class="clean">
people.filter(_.name === "M Odersky")
       .map(p => (p.name,p.age))
       .update(("M. Odersky",54321))
</pre>
```
// Find entity id with meta Molecule attribute `e`
// Omit `name` value by adding underscore `_` to attribute name   
val oderskyId = Person.e.name_("M Odersky").get.head
Person(oderskyId).name("M. Odersky").age(54321).update
```

#### delete

<pre class="clean">
people.filter(p => p.name === "M. Odersky")
       .delete
</pre>
```
Person.e.name_("M. Odersky").get.head.retract
```

#### case

<pre class="clean">
people.map(p =>
  Case
    If(p.addressId === 1) Then "A"
    If(p.addressId === 2) Then "B"
).list
</pre>
```
Person.address(1 or 2).get map {
  case 1 => "A"
  case 2 => "B"
}
```
































