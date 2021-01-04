---
title: "SQL"
weight: 30
menu:
  main:
    parent: compare
---

# Molecule vs SQL

We'll make a similar comparison to SQL in general as [Slick](http://slick.lightbend.com/doc/3.3.3/sql-to-slick.html#select) does:

#### SELECT *

<pre class="clean">
sql"select * from PERSON".as[Person].list
</pre>
In molecule we would declare each attribute we are interested in also to infer the exact return type
```
val persons: List[(String, Int)] = Person.name.age.get
```

#### SELECT

<pre class="clean">
sql"""
  select AGE, concat(concat(concat(NAME,' ('),ID),')')
  from PERSON
""".as[(Int,String)].list
</pre>
With Molecule we would concatenate `name` and `id` with the returned result set:
```
Person.age.name.e.get map { case (age, name, id) => (age, s"$name ($id)" }
```
Actually, Molecule treats attributes as mandatory values whereas Select-attributes in a SQL query could be Null values (if allowed). Molecule can also ask for optional values too if a `$` sign is appended to an attribute name:
```
Person.age.firstName.middleName$.lastName.e.get map { 
  case (age, firstName, Some(middleName), lastName, id) => (age, s"$firstName $middleName $lastName ($id)" 
  case (age, firstName, None, lastName, id)             => (age, s"$firstName $lastName ($id)" 
}
```


#### WHERE

<pre class="clean">
sql"select * from PERSON where AGE >= 18 AND NAME = 'C. Vogt'".as[Person].list
</pre>
Molecule filter values by applying a required value to an attribute or supply a value to compare against (`>=(18)`):
```
Person.age.>=(18).name("C. Vogt").get
```
(Again we would define which attribute values we want to return)


#### ORDER BY

<pre class="clean">
sql"select * from PERSON order by AGE asc, NAME".as[Person].list
</pre>
Ordering is applied on the result set:
```
Person.age.name.get.toSeq.sortBy(_._1)
```

#### Aggregations

<pre class="clean">
sql"select max(AGE) from PERSON".as[Option[Int]].first
</pre>
Aggregate functions like `max` are all applied as a keyword value to an attribute.
```
Person.age(max).get
// or get a range of top values
Person.age(max(3)).get
```
We can aggregate values also with the counterpart `min` or get a random value with `rand`. Or perform aggregate calculations with `count`, `countDistinct`, `sum`, `avg`, `median`, `variance` and `stddev` which are all built functions in Datomic.


#### GROUP BY

<pre class="clean">
sql"""
  select ADDRESS_ID, AVG(AGE)
  from PERSON
  group by ADDRESS_ID
""".as[(Int,Option[Int])].list
</pre>
Molecule automatically group by attributes not having an aggregate expression. In this case the query will group by `address` and calculate the average `age` for persons living there.
```
Person.address.age(avg).get
```

#### HAVING

<pre class="clean">
sql"""
  select ADDRESS_ID
  from PERSON
  group by ADDRESS_ID
  having avg(AGE) > 50
""".as[Int].list
</pre>
```
Person.address.age(avg).get.toSeq.filter(_._2 > 50)
```

#### Implicit join

<pre class="clean">
sql"""
  select P.NAME, A.CITY
  from PERSON P, ADDRESS A
  where P.ADDRESS_ID = A.id
""".as[(String,String)].list
</pre>
```
Person.name.Address.city.get
```

#### Explicit join

<pre class="clean">
sql"""
  select P.NAME, A.CITY
  from PERSON P
  join ADDRESS A on P.ADDRESS_ID = A.id
""".as[(String,String)].list
</pre>
```
Person.name.Address.city.get
```

#### left/right/outer join

<pre class="clean">
sql"""
  select P.NAME,A.CITY
  from ADDRESS A
  left join PERSON P on P.ADDRESS_ID = A.id
""".as[(Option[String],String)].list
</pre>
```
// Add `$` to attribute name to get optional values
val persons: List[(Option[String], String)] = Person.name$.Address.city.get
```

#### Subquery

<pre class="clean">
sql"""
  select *
  from PERSON P
  where P.ADDRESS_ID in (select ID
                 from ADDRESS
                 where CITY = 'New York City')
""".as[Person].list
</pre>
```
Person.age.name.Address.city_("New York City").get
```

#### INSERT

<pre class="clean">
sqlu"""
  insert into PERSON (NAME, AGE, ADDRESS_ID) values ('M Odersky', 12345, 1)
""".first
</pre>
```
Person.name("M Odersky").age(12345).address(1).save
```

#### UPDATE

<pre class="clean">
sqlu"""
  update PERSON set NAME='M. Odersky', AGE=54321 where NAME='M Odersky'
""".first
</pre>
```
// Find entity id with generic Molecule attribute `e`
// Omit `name` value by adding underscore `_` to attribute name   
val oderskyId = Person.e.name_("M Odersky").get.head
Person(oderskyId).name("M. Odersky").age(54321).update
```

#### DELETE

<pre class="clean">
sqlu"""
  delete PERSON where NAME='M. Odersky'
""".first
</pre>
```
// Retract entity
Person.e.name_("M. Odersky").get.head.retract
```

#### CASE

<pre class="clean">
sql"""
  select
    case 
      when ADDRESS_ID = 1 then 'A'
      when ADDRESS_ID = 2 then 'B'
    end
  from PERSON P
""".as[Option[String]].list
</pre>
```
Person.address(1 or 2).get map {
  case 1 => "A"
  case 2 => "B"
}
```
































