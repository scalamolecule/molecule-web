# Overview

SQL database libraries in the Scala ecosystem are basically SQL or DSL based. So, the primary choice of library comes down to wether you want to write SQL or use some higher-level DSL abstraction. 

  
- ***[Compare Molecule with DSL-based libraries](sql-dsl)***
  - Slick
  - Quill
  - ScalikeJDBC
  - ScalaSql
  - Typo
  - Squeryl (Java)
  - JOOQ (Java)
  - ...
 

- ***[Compare Molecule with SQL-based libraries](sql-raw)***
  - Doobie
  - Anorm
  - Skunk
  - Magnum
  - ldbc
  - JDBC (Java)
  - ...
  

All the DSL libraries also have SQL interpolation to fallback on raw sql.


## Show me the code

For the impatient, here are some bare-bone comparisons with popular libraries:

#### Query

::: code-tabs#coord
@tab Molecule
```scala
Person.name.age.Address.street.query.get
```

@tab ScalaSql
```scala
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Slick
```scala
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Quill
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```

@tab Doobie
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
@tab Magnum
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
:::


#### Transact

::: code-tabs#coord
@tab Molecule
```scala
Person.name.age.Address.street
```

@tab ScalaSql
```scala
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Slick
```scala
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Quill
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```

@tab Doobie
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
@tab Magnum
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
:::


#### Setup

::: code-tabs#coord
@tab Molecule
```scala
Person.name.age.Address.street
```

@tab ScalaSql
```scala
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Slick
```scala
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```

@tab Quill
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```

@tab Doobie
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
@tab Magnum
```scala
sql"""SELECT
  Person.name,
  Person.age,
  Address.street
FROM Person
  INNER JOIN Address
    ON Person.address = Address.id
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;"""
```
:::


## check...

Prisma - a bit similar to molecule. See how they describe it, maybe I can use some of their thoughts...

## remember...

[typo](https://oyvindberg.github.io/typo/) sql code generation

[scalasql comparisons](https://github.com/com-lihaoyi/scalasql/blob/main/docs/design.md#comparisons)

[Scaladex overview](https://index.scala-lang.org/search?topics=database&q=jdbc)

[Magnum](https://github.com/AugustNagro/magnum)

[LDBC docs](https://takapi327.github.io/ldbc/en/index.html)
[LDBC github](https://github.com/takapi327/ldbc?tab=readme-ov-file)

[Skunk tables](https://github.com/foldables-io/skunk-tables)

[Ldbc](https://github.com/takapi327/ldbc?tab=readme-ov-file)


Nested data structures

Rollback

Savepoints

Collection types

Window functions

Streaming

