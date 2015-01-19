---
date: 2014-05-14T02:13:50Z
title: "SQL"
weight: 20
menu:
  main:
    parent: compare
    identifier: sql
---

# SQL vs. Molecule examples

We'll make a similar comparison to SQL as [Slick](http://slick.typesafe.com/doc/3.0.0-M1/sql-to-slick.html#sql-vs-slick-examples) does:

#### SELECT *

```
sql"select * from PERSON".as[Person].list
```
In molecule we would declare each attribute we are interested in also to infer the exact return type
```scala
val persons: Seq[(String, Int)] = Person.name.age.get
```

#### SELECT

```
sql"""
  select AGE, concat(concat(concat(NAME,' ('),ID),')')
  from PERSON
""".as[(Int,String)].list
```
With Molecule we would concatenate `name` and `id` with the returned result set:
```scala
Person.age.name.e.get map { case (age, name, id) => (age, s"$name ($id)" }
```

#### WHERE

```scala
sql"select * from PERSON where AGE >= 18 AND NAME = 'C. Vogt'".as[Person].list
```
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```scala
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### ORDER BY

```scala
sql"select * from PERSON order by AGE asc, NAME".as[Person].list
```
Ordering is applied on the result set:
```scala
Person.age.name.get sortBy(_._1)
```

#### Aggregations

```scala
sql"select max(AGE) from PERSON".as[Option[Int]].first
```
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```scala
Person.age(max).get
// or get a range of top values
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### GROUP BY

```scala
sql"""
  select ADDRESS_ID, AVG(AGE)
  from PERSON
  group by ADDRESS_ID
""".as[(Int,Option[Int])].list
```
Molecule...
```scala
// TODO
Person.address(groupBy).age(avg)).get
```

#### HAVING

```scala
sql"""
  select ADDRESS_ID
  from PERSON
  group by ADDRESS_ID
  having avg(AGE) > 50
""".as[Int].list
```
Molecule...
```scala
// TODO
Person.address(groupBy).age_(avg > 50)).get
```

#### Implicit join

```scala
sql"""
  select P.NAME, A.CITY
  from PERSON P, ADDRESS A
  where P.ADDRESS_ID = A.id
""".as[(String,String)].list
```
Molecule...
```scala
Person.name.Address.city.get
```

#### Explicit join

```scala
sql"""
  select P.NAME, A.CITY
  from PERSON P
  join ADDRESS A on P.ADDRESS_ID = A.id
""".as[(String,String)].list
```
Molecule...
```scala
Person.name.Address.city.get
```

#### left/right/outer join

```scala
sql"""
  select P.NAME,A.CITY
  from ADDRESS A
  left join PERSON P on P.ADDRESS_ID = A.id
""".as[(Option[String],String)].list
```
Molecule...
```scala
// TODO
Person.name(maybe).Address.city.get
```

#### Subquery

```scala
sql"""
  select *
  from PERSON P
  where P.ADDRESS_ID in (select ID
                 from ADDRESS
                 where CITY = 'New York City')
""".as[Person].list
```
Molecule...
```scala
Person.age.name.Address.city_("New York City").get
```

#### Scalar value subquery / custom function

```scala
sql"""
  select * from PERSON P,
                     (select rand() * MAX(ID) as ID from PERSON) RAND_ID
  where P.ID >= RAND_ID.ID
  order by P.ID asc
  limit 1
""".as[Person].first
```
Molecule...
```scala
// TODO
```

#### INSERT

```scala
sqlu"""
  insert into PERSON (NAME, AGE, ADDRESS_ID) values ('M Odersky', 12345, 1)
""".first
```
Molecule...
```scala
Person.name("M Odersky").age(12345).address(1).add
```

#### UPDATE

```scala
sqlu"""
  update PERSON set NAME='M. Odersky', AGE=54321 where NAME='M Odersky'
""".first
```
Molecule...
```scala
val odersky = Person.name("M Odersky").get.id
Person(odersky).name("M. Odersky").age(54321).update
```

#### DELETE

```scala
sqlu"""
  delete PERSON where NAME='M. Odersky'
""".first
```
Molecule...
```scala
Person.name("M. Odersky").get.id.delete
```

#### CASE

```scala
sql"""
  select
    case 
      when ADDRESS_ID = 1 then 'A'
      when ADDRESS_ID = 2 then 'B'
    end
  from PERSON P
""".as[Option[String]].list
```
Molecule...
```scala
Person.Address.e(1 or 2).get map {
  case 1 => "A"
  case 2 => "B"
}
```
































