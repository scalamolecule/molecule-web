# ProtoQuill

Compare [ProtoQuill Tutorial](https://github.com/zio/zio-protoquill?tab=readme-ov-file#tutorial) code examples with molecules in the following sections.

_The molecule examples use the simple synchronous api for brevity. [Asynchronous/ZIO/cats.effect.IO](/database/query/attributes#4-apis) apis are also available._


## ProtoQuill Setup

In ProtoQuill you need to define a context and a case class to represent your table in the database:

```scala
case class Person(name: String, age: Int)

// SnakeCase turns firstName -> first_name
val ctx = new PostgresJdbcContext(SnakeCase, "ctx")
import ctx._
```

## Molecule Setup

In Molecule you define things more in terms of your domain structure with Entities and their Attributes.

```scala
object People extends DomainStructure {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
```
From this model, Molecule generates

- An SQL schema that we can transact to create the database
- Boilerplate code to write molecule queries and transactions


## Queries

ProtoQuill queries are built using inline quoted expressions.
```scala
// ProtoQuill
inline def people = quote {
  query[Person]
}
inline def joes = quote {
  people.filter(p => p.name == "Joe")
}
run(joes)
```

This means that you will often over-fetch data with ProtoQuill if not all attribute values of a Table/case class are needed.

In Molecule you instead choose exactly which attributes you need and what order you want them in:

```scala
// Molecule
Person.name.age.query.get
```

#### Quotation

When dynamic parts are not present, ProtoQuill allows inline defs without quotation:
```scala
inline def people = query[Person]
inline def joes = people.filter(p => p.name == "Joe")
run(joes)
```
Whereas dynamic parts require quotation:
```scala
inline def people = quote {
  query[Person]
}
val joes = quote {
  people.filter(p => p.name == "Joe")
}

run(joes)
```

In Molecule, no quotation exists and filters are simply applied to the attribute:

```scala
Person.name("Joe").age.query.get
```

## Insert

ProtoQuill batch queries with different entities
```scala
liftQuery(vips).foreach(v => query[Person].insertValue(Person(v.first + v.last, v.age)))
```
Batch inserts in Molecule:
```scala
Person.first.last.age.insert(vips.map(v => (v.first, v.last, v.age)))
```

## Update

ProtoQuill updates:
```scala
// batch queries with additional lifts
liftQuery(people)
  .foreach(p =>
    query[Person]
      .filter(p => p.age > lift(123))
      .contains(p.age)
  )
  .updateValue(p)
)

// ...even with additional liftQuery clauses!
liftQuery(people)
  .foreach(p =>
    query[Person]
      .filter(p => p.age > lift(123) && liftQuery(List(1, 2, 3)).contains(p.age))
      .updateValue(p)
  )
```
Molecule, update all with age > 123
```scala
Person.age_.>(123).update(people.map(p => (p.first, p.last, p.age)))
```

## Query Meta

```scala
inline given QueryMeta[PersonName, String] =
  queryMeta(
    quote {
      (q: Query[PersonName]) => q.map(p => p.name)
    }
  )((name: String) => PersonName(name))

ctx.run(people)
```

Molecule:
```scala
Person.name.query.get
```


## Shareable Code

```scala
// case class Person(name: String, age: Int)
inline def onlyJoes(p: Person) = p.name == "Joe"

run(query[Person].filter(p => onlyJoes(p)))

val people: List[Person] = ...
val joes = people.filter(p => onlyJoes(p))
```

Molecule:
```scala
val onlyJoes = Person.name("Joe").age
val joes = onlyJoes.query.get
```



## Filter


```scala
val values: Map[String, String] = Map("firstName" -> "Joe", "age" -> "22")

// filterByKeys uses lift so you need a context to use it
val ctx = new MirrorContext(Literal, PostgresDialect)
import ctx._

inline def q = quote {
  query[Person].filterByKeys(values)
}
run(q)
```

Molecule:
```scala
Person.firstName("Joe").age(22).query.get
```
