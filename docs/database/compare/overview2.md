# SQL libraries overview

Current SQL libraries in the Scala ecosystem can be organised by the kind of code they are accessed with:

- Plain SQL
- SQL-flavored DSL
- Scala collection-style DSL

Libraries in all 3 categories adapt to the semantics of SQL databases in various ways. One could say that "your domain comes to the database".

Molecule flips this direction of attention and provides boilerplate code to _**"let the database come to your domain"**_. 

Instead of writing code that access the database, you can _compose_ your domain terms to form declarative and fully type-inferred molecules, or data models, 
of _what_ data you're interested in. Molecule then translates that into _how_ it is mutated/fetched with SQL.


#### A fourth category

By reversing the dynamics and taking this domain-declarative approach, Molecule places itself in a 4th category of SQL libraries for custom domain-tailored data composition.

Let's get an overview of libraries in the traditional 3 categories with a comparable small query just to get a feeling for the differences (each category sub page has more elaborate examples):


## 1. Plain SQL libraries

Libraries that let you write plain SQL directly with low-level control:

- [Doobie](https://typelevel.org/doobie/index.html)
- [Magnum](https://github.com/AugustNagro/magnum) (with additional repository methods)
- [Anorm](https://playframework.github.io/anorm/)
- [Skunk](https://typelevel.org/skunk/)

SQL query that is the same for all 4 libraries:
```sql
select p.name, p.age, a.street 
from Person as p 
inner join Address as a on p.address = a.id
```
[Compare](/database/compare/plain-sql) Molecule with plain SQL libraries...


## 2. SQL-flavored DSL libraries

Libraries that offer a DSL resembling SQL syntax, while ensuring type-safety:

[ldbc](https://takapi327.github.io/ldbc/)

This example translates the above SQL query using ldbc, which treats case classes as table representations. The query is constructed with `join`, `on`, and `select`, mirroring SQL keywords:
```scala
TableQuery[Person]
  .join(TableQuery[Address])
  .on((p, a) => p.id === a.id)
  .select((p, a) => p.name *: p.age *: a.street)
```

[ScalikeJDBC](https://scalikejdbc.org)


ScalikeJDBC wraps the query in a `withSQL` block, using a SQL-inspired DSL. It also requires setting up table syntax references beforehand:
```scala
val p = Person.syntax("p")
val a = Address.syntax("a")

withSQL {
  select(p.result.name, p.result.age, a.result.street)
    .from(Person as p)
    .innerJoin(Address as a)
    .on(p.addressId, a.id)
}.map(rs => (rs.string(1), rs.int(2), rs.string(3)))
  .list
  .apply()
```

[Typo](https://github.com/oyvindberg/typo)

Typo uses code-generated classes to represent tables, enabling query building through a fluent API that mimics SQL structure:
```scala
val query =
  Person.select
    .join(Address).on(_.addressId, _.id)
    .map((p, a) => (p.name, p.age, a.street))

val result = query.run(transaction)  // Using Skunk as the backend
```

[Squeryl (Java)](https://www.squeryl.org)

Squeryl builds queries in a for-comprehension-like structure using table references brought into scope with from. Table aliases `(p, a)` are used in the rest of the query:
```scala
from(persons, addresses)((p, a) =>
  where(p.addressId === a.id)
    select((p.name, p.age, a.street))
)
```

[JOOQ (Java)](https://www.jooq.org)
JOOQ builds queries with a fluent API directly representing SQL constructs. It uses generated table constants and types for full type safety:
```scala
Result<Record3<String, Integer, String>> result =
  DSL.using(configuration)
    .select(PERSON.NAME, PERSON.AGE, ADDRESS.STREET)
    .from(PERSON)
    .join(ADDRESS).on(PERSON.ADDRESS_ID.eq(ADDRESS.ID))
    .fetch();
```

[Compare](/database/compare/sql-dsl) Molecule with SQL-like DSL libraries...


## 3. Scala collection-style DSL libraries

Libraries that use Scala collection-like syntax to query SQL data:

[ScalaSql](https://github.com/com-lihaoyi/scalasql)
ScalaSql defines tables with case classes and uses a mix of SQL- and Scala collection-inspired operations (`select`, `join`, `map`) to build queries in a familiar style:
```scala
Person.select.join(Address)(_.id === _.personId)
  .map { case (p, a) => (p.name, p.age, a.street) }
```

[Slick](https://scala-slick.org)
Slick uses a table definition layer to create TableQuery objects (e.g. people, addresses). Queries are built with idiomatic Scala constructs that resemble collections:
```scala
(people join addresses on (_.id === _.addressId))
  .map { case (p, a) => (p.name, p.age, a.street) }
```

[Quill / ProtoQuill](https://github.com/zio/zio-quill)
Quill uses a quoted monadic comprehension to define queries. Tables are represented as case classes, and queries are constructed in for-comprehension style and translated at compile-time:
```scala
val q = quote {
  for {
    p <- query[Person]
    a <- query[Address] if p.addressId == a.id
  } yield (p.name, p.age, a.street)
}

val result = run(q)
```


[Compare](/database/compare/collection-dsl) Molecule with Scala collection-like DSL libraries...


## 4. Domain-tailored composition (Molecule)

Libraries that compose queries declaratively from domain terms:

Once your domain structure is defined, Molecule generates a domain-tailored DSL. You then compose your query declaratively with minimal syntax noise, focusing only on what data your domain cares about:
```scala
// Molecule
Person.name.age.Address.street
```
Molecule doesn’t just aim to reduce boilerplate. It shifts the mental model: from writing how to fetch data to declaring what data your domain cares about.

Molecule is also the only SQL library in the Scala ecosystem that can insert and return [nested data](/database/query/relationships.html#nested).
