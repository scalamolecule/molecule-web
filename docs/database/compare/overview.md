# SQL libs overview

Current SQL libraries in the Scala ecosystem can be organised by the kind of code they are accessed with:

- **Plain SQL**
- **SQL-flavored DSL**
- **Scala collection-style DSL**

Libraries in all 3 categories adapt to the semantics of SQL databases in various ways. 

Molecule on the other hand lets you use your domain terms to compose data models of _what_ data you're interested in with no need to write SQL or DSL code - the _how_. 


## 1. Plain-SQL libraries

Libraries that let you write plain SQL directly with low-level control:

- [Doobie](https://typelevel.org/doobie/index.html) - Pure functional JDBC layer for Scala and Cats
- [Magnum](https://github.com/AugustNagro/magnum) - Pure functional raw-SQL library with repository helpers
- [Skunk](https://typelevel.org/skunk/) - Pure functional Postgres library using cats-effect
- [Anorm](https://playframework.github.io/anorm/) - Lightweight SQL wrapper integrated with Play Framework

Example query:

```scala
// Molecule
Person.name.age.Address.street.query.get
```

```scala
// Same query in all 4 plain-SQL libraries: 
sql"""select p.name, p.age, a.street 
      from Person as p 
      inner join Address as a on p.address = a.id"""
```

[More comparisons...](/database/compare/plain-sql)


## 2. SQL-flavored DSL libraries

Libraries that offer a DSL resembling SQL syntax, while ensuring type-safety:

- [ScalikeJDBC](https://scalikejdbc.org) — SQL-centric DSL with interpolation, transaction/connection helpers, and JDBC integration.
- [Typo](https://github.com/oyvindberg/typo) — Schema-first, code-generated, type-safe SQL DSL (commonly used with Skunk for Postgres).
- [ldbc](https://takapi327.github.io/ldbc/) — Pure functional SQL library with a type-safe query builder in Scala 3.
- [Squeryl (Java)](https://www.squeryl.org) — Mature, statically typed SQL DSL with ORM-style mapping on the JVM.
- [JOOQ (Java)](https://www.jooq.org) — Fluent, type-safe SQL builder with powerful schema-driven code generation.

```scala
// Molecule
Person.name.age.Address.street.query.get
```

```scala
// ScalikeJDBC
withSQL {
  select(p.result.name, p.result.age, a.result.street)
    .from(Person as p)
    .innerJoin(Address as a)
    .on(p.addressId, a.id)
}
```

```scala
// Typo
Person.select
  .join(Address)
  .on(_.addressId, _.id)
  .map((p, a) => (p.name, p.age, a.street))
```

```scala
// ldbc
TableQuery[Person]
  .join(TableQuery[Address])
  .on((p, a) => p.id === a.id)
  .select((p, a) => p.name *: p.age *: a.street)
```

```scala
// Squeryl
from(persons, addresses)((p, a) =>
  where(p.addressId === a.id)
    select ((p.name, p.age, a.street))
)
```

```scala
// JOOQ
select(PERSON.NAME, PERSON.AGE, ADDRESS.STREET)
  .from(PERSON)
  .join(ADDRESS)
  .on(PERSON.ADDRESS_ID.eq(ADDRESS.ID))
```


[More comparisons...](/database/compare/sql-dsl)

## 3. Scala collection-style DSL libraries

Libraries that use Scala collection-like syntax to query SQL data:

- [ScalaSql](https://github.com/com-lihaoyi/scalasql) — Lightweight library whose query DSL mirrors Scala collections with minimal ceremony.
- [Slick](https://scala-slick.org) — Functional Relational Mapping with a collection-like, type-safe query API and streaming support.
- [Quill](https://github.com/zio/zio-quill) — Compile-time quoted, collection-style DSL that translates to SQL for multiple backends.


```scala
// Molecule
Person.name.age.Address.street.query.get
```

```scala
// ScalaSql
Person.select
  .join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

```scala
// Slick
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```

```scala
// Quill
quote {
  for {
    p <- query[Person]
    a <- query[Address] if p.addressId == a.id
  } yield (p.name, p.age, a.street)
}
```


[More comparisons...](/database/compare/collection-dsl)
