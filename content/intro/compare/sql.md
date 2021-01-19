---
title: "SQL"
weight: 30
menu:
  main:
    parent: intro-compare
---

# Molecule vs SQL


#### SELECT *

```
sql"select * from PERSON".as[Person].list
```
With Molecule we only need to fetch the attributes that we need:
```scala
Person.name.age.get
```

Returned type is `List[(String, Int)]`

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

```
sql"select * from PERSON where AGE >= 18 AND NAME = 'C. Vogt'".as[Person].list
```
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```scala
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### ORDER BY

```
sql"select * from PERSON order by AGE asc, NAME".as[Person].list
```
Ordering is applied on the result set:
```scala
Person.age.name.get.toSeq.sortBy(_._1)
```

#### Aggregations

```
sql"select max(AGE) from PERSON".as[Option[Int]].first
```
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```scala
Person.age(max).get
```
or get a range of top 3 values
```scala
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### GROUP BY

```
sql"""
  select ADDRESS_ID, AVG(AGE)
  from PERSON
  group by ADDRESS_ID
""".as[(Int,Option[Int])].list
```
Molecule automatically group by attributes not having an aggregate expression. In this case the query will group by `address` and calculate the average `age` for persons living there.
```scala
Person.address.age(avg).get
```

#### HAVING

```
sql"""
  select ADDRESS_ID
  from PERSON
  group by ADDRESS_ID
  having avg(AGE) > 50
""".as[Int].list
```
```scala
Person.address.age(avg).get.toSeq.filter(_._2 > 50)
```

#### Implicit join

```
sql"""
  select P.NAME, A.CITY
  from PERSON P, ADDRESS A
  where P.ADDRESS_ID = A.id
""".as[(String,String)].list
```
```scala
Person.name.Address.city.get
```

#### Explicit join

```
sql"""
  select P.NAME, A.CITY
  from PERSON P
  join ADDRESS A on P.ADDRESS_ID = A.id
""".as[(String,String)].list
```
```scala
Person.name.Address.city.get
```

#### left/right/outer join

```
sql"""
  select P.NAME,A.CITY
  from ADDRESS A
  left join PERSON P on P.ADDRESS_ID = A.id
""".as[(Option[String],String)].list
```
```scala
// Add `$` to attribute name to get optional values
val persons: List[(Option[String], String)] = Person.name$.Address.city.get
```

#### Subquery

```
sql"""
  select *
  from PERSON P
  where P.ADDRESS_ID in (select ID
                 from ADDRESS
                 where CITY = 'New York City')
""".as[Person].list
```
```scala
Person.age.name.Address.city_("New York City").get
```

#### INSERT

```
sqlu"""
  insert into PERSON (NAME, AGE, ADDRESS_ID) values ('M Odersky', 12345, 1)
""".first
```
```scala
Person.name("M Odersky").age(12345).address(1).save
```

#### UPDATE

```
sqlu"""
  update PERSON set NAME='M. Odersky', AGE=54321 where NAME='M Odersky'
""".first
```
```scala
// Find entity id with generic Molecule attribute `e`
val oderskyId = Person.e.name_("M Odersky").get.head
Person(oderskyId).name("M. Odersky").age(54321).update
```

#### DELETE

```
sqlu"""
  delete PERSON where NAME='M. Odersky'
""".first
```
```scala
// Retract entity
Person.e.name_("M. Odersky").get.head.retract
```

#### CASE

```
sql"""
  select
    case 
      when ADDRESS_ID = 1 then 'A'
      when ADDRESS_ID = 2 then 'B'
    end
  from PERSON P
""".as[Option[String]].list
```
```scala
Person.address(1 or 2).get map {
  case 1 => "A"
  case 2 => "B"
}
```









### Next

[Compare Slick...](/intro/compare/slick/)