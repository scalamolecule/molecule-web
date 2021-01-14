---
title: "Generic APIs"
weight: 100
menu:
  main:
    parent: code
    identifier: generic
---

# Generic attributes

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic)

Molecule provides access to generic data about data and Schema with the following 7 generic interfaces (each with a little example - read more by clicking on each title):


#### [Datom](datom)

Get id of Ben entity with generic Datom attribute `e`
```scala
Person.e.name.get.head === (benEntityId, "Ben")
```


#### [EAVT Index](indexes)

Attributes and values of entity e1
```scala
EAVT(e1).a.v.get === List(
  (":Person/name", "Ben"),
  (":Person/age", 42),
  (":Golf/score", 5.7)
)
``` 

#### [AVET Index](indexes)

Values, entities and transactions where attribute :Person/age is involved
```scala
AVET(":Person/age").e.v.t.get === List(
  (42, e1, t2),
  (37, e2, t5)
  (14, e3, t7),
)

// AVET index filtered with an attribute name and a range of values
AVET.range(":Person/age", Some(14), Some(40)).v.e.t.get === List(
  (14, e4, t7),
  (37, e2, t5)
)
``` 

#### [AEVT Index](indexes)

Entities, values and transactions where attribute :Person/name is involved 
```scala
AEVT(":Person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)
``` 

#### [VAET Index](indexes)

Get entities pointing to entity a1
```scala
VAET(a1).v.a.e.get === List(
  (a1, ":Release/artists", r1),
  (a1, ":Release/artists", r2),
  (a1, ":Release/artists", r3),
)
``` 

#### [Log](log)

Data from transaction t1 until t4 (exclusive)
```scala
Log(Some(t1), Some(t4)).t.e.a.v.op.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 41, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true),

  (t3, e1, ":Person/age", 41, false),
  (t3, e1, ":Person/age", 42, true)
)
``` 

#### [Schema](schema)

Get entities pointing to entity a1 
```scala
Schema.part.ns.attr.fulltext$.doc.get === List(
  ("ind", "person", "name", Some(true), "Person name"), // fulltext search enabled
  ("ind", "person", "age", None, "Person age"),
  ("cat", "sport", "name", None, "Sport category name")
)
``` 







## Entity

An entity in Datomic is a group of Datoms/facts that share an entity id:

![](/img/page/entity/entity1.png)


Attributes with any seemingly unrelated namespaces can group as entities by simply sharing the entity id:

![](/img/page/entity/entity2.png)

This demonstrates that Datomic/Molecule Namespaces are not like Tables in SQL. The above entity for instance has attributes asserted from 2 different namespaces that could be completely unrelated/have no reference to each other. Attributes from any number of namespaces could be asserted sharing the same entity id.


At runtime we can see the facts of an entity by calling `touch` on the entity id (of type `Long`):

```scala
101L.touch === Map(
  ":db/id" -> 101L,
  ":Person/name"  -> "Fred", 
  ":Person/likes" -> "pizza", 
  ":Person/age"   -> 38, 
  ":Person/addr"  -> 102L,        // reference to an address entity with entity id 102 
  ":Site/cat"     -> "customer"
)
```



### Optional attribute values

We can look for an optionally present attribute value. Here we ask the entity id `fredId` if it has a `:Site/cat` attribute value (of type `String`) and we get a typed optional value back:
```scala
val siteCat_? : Option[String] = fredId[String](":Site/cat")
```


### Traversing

The `touch` method can recursively retrieve referenced entities. We could for instance traverse an `Order` with `LineItems`:


```scala
orderId.touch === Map(
  ":db/id" -> orderId,
  ":Order/lineItems" -> List(
    Map(
      ":db/id" -> 102L, 
      ":LineItem/qty" -> 3, 
      ":LineItem/product" -> "Milk",
      ":LineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":LineItem/qty" -> 2, 
      ":LineItem/product" -> "Coffee",
      ":LineItem/price" -> 46.0)))
```

The entity attributes graph might be deep and wide so we can apply a max level to `touch(<maxLevel>)`:

```scala
fredId.touchMax(2) === Map(
  ":db/id" -> fredId,
  ":Person/name" -> "Fred"
  ":Person/friends" -> List(
    Map(
      ":db/id" -> lisaId,
      ":Person/name" -> "Lisa"
      ":Person/friends" -> List(
        Map(
          ":db/id" -> monaId,
          ":Person/name" -> "Mona"
          ":Person/friends" -> Set(ids...) // Mona's friends (3 levels deep) only as ids - not attribute maps
        ),
        Map(...) // + more friends of Lisa (2 levels deep)
      )
    ),
    Map(...) // + more friends of Fred (1 level deep)
  )
)
```




## Datom

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Datom.scala)


### Datoms / quintuplets

Attribute values are saved as quintuplets of information in Datomic:

![](/img/page/generic/datom.png)



The Datom API in Molecule let us retrieve each element generically for any molecule we are working on by providing the following "generic attributes" that we can add to our custom molecules:

- `e` - Entity id (`Long`)
- `a` - Attribute (`String`)
- `v` - Value (`Any`)
- `t` - Transaction point in time (`Long` alternatively `Int`)
- `op` - Operation: assertion / retraction (`Boolean` true/false)

The Transaction value has two more representations

- `tx` - Transaction entity id (`Long`)
- `txInstant` - Transaction wall-clock time (`java.util.Date`)


### Mixing custom and generic attributes

Generic attributes like `e` can be added to retrieve an entity id of a custom molecule:

```scala
// Get entity id of Ben with generic datom attribute `e` on a custom molecule
Person.e.name.get.head === (benEntityId, "Ben")
```

And we can get information about the transaction time of the assertion of some custom attribute value:

```scala
// When was Ben's age updated? Using `txInstant`
Person(benEntityId).age.txInstant.get.head === (42, <April 4, 2019>) // (Date)
```

With a history db we can access the transaction number `t` and assertion/retraction statusses with `op`

```scala
// 
Person(benEntityId).age.t.op.getHistory.sortBy(r => (r._2, r._3)) === List(
  (41, t1, true),  // age 41 asserted in transaction t1
  (41, t2, false), // age 41 retracted in transaction t2
  (42, t2, true)   // age 42 asserted in transaction t2
)
```


### Fully generic Datom molecules

In molecule, attribute names (the `A` of the Datom) are modelled as our custom DSL attributes as we saw above when we retrieved the `Person.age` attribute value along with some generic datom data.

Sometimes we will be interested in more generic data where we don't know in advance what attributes will be involved. Then we can use the generic Datom attribute `a` for Attribute name and `v` for value. We could for instance ask what we know about an entity over time in the database:

```scala
// What do we know about the fred entity?
Person(fred).a.v.t.op.getHistory.sortBy(r => (r._2, r._3)) === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true), 
  (":Person/likes", "pizza", t6, false),
  (":Person/likes", "pasta", t6, true)  
)
```

### Filtering with expressions

By applying values to generic attributes we can filter search results:

```scala
// What was asserted/retracted in transaction tx3 about what Fred likes? 
Person(fred).likes.tx(tx6).op.getHistory.sortBy(r => (r._2, r._3)) === List(
  ("pizza", t6, false), // Fred no longer likes pizza
  ("pasta", t6, true)   // Fred now likes pasta
)
```

## Index

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Index.scala)


_Some index descriptions in the following sections respectfully quoted from the [Datomic documentation](https://docs.datomic.com/on-prem/indexes.html)._

Datomic maintains four indexes that contain ordered sets of datoms. Each of these indexes is named based on the sort order used.  E, A, and V are always sorted in ascending order, while T is always in descending order:

- **EAVT** - Datoms sorted by Entity-Attribute-Value-Transaction.
- **AVET** - Datoms sorted by Attribute-Value-Entity-Transaction.
- **AEVT** - Datoms sorted by Attribute-Entity-Value-Transaction.
- **VAET** - "Reverse index" for reverse lookup of ref types.

Molecule provides access to each index by instantiating a corresponding Index object with one or more arguments and then adding generic attributes matching the data to be returned.

Contrary to Datomic's datoms API that returns Datoms, Molecule returns tuples of data matching the generic attributes added to the Index object. This way, Index molecules transparently share the same return type semantics as normal molecules.

### Generic Index attributes

The following standard generic Index attributes can be used to build Index molecules:

- `e` - Entity id (`Long`)
- `a` - Attribute (`String`)
- `v` - Value (`Any`)
- `t` - Transaction point in time (`Long` alternatively `Int`)
- `tx` - Transaction entity id (`Long`)
- `txInstant` - Transaction wall-clock time (`java.util.Date`)
- `op` - Operation: assertion / retraction (`Boolean` true/false)



### EAVT

The EAVT index provides efficient access to everything about a given entity. Conceptually this is very similar to row access style in a SQL database, except that entities can possess arbitrary attributes rather then being limited to a predefined set of columns.

```scala
// Create EAVT Index molecule with 1 entity id argument
EAVT(e1).e.a.v.t.get === List(
  (e1, ":Person/name", "Ben", t1),
  (e1, ":Person/age", 42, t2),
  (e1, ":Golf/score", 5.7, t2)
)

// Maybe we are only interested in the attribute/value pairs:
EAVT(e1).a.v.get === List(
  (":Person/name", "Ben"),
  (":Person/age", 42),
  (":Golf/score", 5.7)
)

// Two arguments to narrow the search
EAVT(e1, ":Person/age").a.v.get === List(
  (":Person/age", 42)
)
``` 


### AVET

The AVET index provides efficient access to particular combinations of attribute and value.

```scala
// Create AVET Index molecule with 1 entity id argument
AVET(":Person/age").v.e.t.get === List(
  (42, e1, t2),
  (37, e2, t5),
  (14, e3, t7)
)

// Narrow search with multiple arguments
AVET(":Person/age", 42).e.t.get === List( (e1, t2) )
AVET(":Person/age", 42, e1).e.v.get === List( (e1, t2) )
AVET(":Person/age", 42, e1, t2).e.v.get === List( (e1, t2) )
```

The AVET Index can be filtered by a range of values between `from` (inclusive) and `until` (exclusive) for an attribute:

```scala
AVET.range(":Person/age", Some(14), Some(37)).v.e.t.get === List(
  (14, e4, t7) // 14 is included in value range
               // 37 not included in value range
               // 42 outside value range
)

// If `from` is None, the range starts from the beginning
AVET.range(":Person/age", None, Some(40)).v.e.t.get === List(
  (14, e3, t7),
  (37, e2, t5)
)

// If `until` is None, the range goes to the end
AVET.range(":Person/age", Some(20), None).v.e.t.get === List(
  (37, e2, t5),
  (42, e1, t2)
)
```

### AEVT

The AEVT index provides efficient access to all values for a given attribute, comparable to traditional column access style.

```scala
// Create AEVT Index molecule with 1 entity id argument
AEVT(":Person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)

// Narrow search with multiple arguments
AEVT(":Person/name", e1).e.v.get === List( (e1, "Ben") )
AEVT(":Person/name", e1, "Ben").e.v.get === List( (e1, "Ben") )
AEVT(":Person/name", e1, "Ben", t2).e.v.get === List( (e1, "Ben") )
```

### VAET

The VAET index contains all and only datoms whose attribute has a :db/valueType of :db.type/ref. This is also known as the reverse index, since it allows efficient navigation of relationships in reverse.

```scala
// Say we have 3 entities pointing to one entity:
Release.e.name.Artists.e.name.get === List(
  (r1, "Abbey Road", a1, "The Beatles"),
  (r2, "Magical Mystery Tour", a1, "The Beatles"),
  (r3, "Let it be", a1, "The Beatles")
)

// .. then we can get the reverse relationships with the VAET Index:
VAET(a1).v.a.e.get === List(
  (a1, ":Release/artists", r1),
  (a1, ":Release/artists", r2),
  (a1, ":Release/artists", r3)
)

// Narrow search with multiple arguments
VAET(a1, ":Release/artist").e.get === List(r1, r2, r3)
VAET(a1, ":Release/artist", r2).e.get === List(r2)
VAET(a1, ":Release/artist", r2, t7).e.get === List(r2)
```

## Log

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Log.scala)

_Some index descriptions in the following sections respectfully quoted from the [Datomic documentation](https://docs.datomic.com/on-prem/indexes.html)._

Datomic's database log is a recording of all transaction data in historic order, organized for efficient access by transaction. The Log is therefore an efficient source of finding data by transaction time.


### Tx range and generic Log attributes

The Molecule Log implementation takes two arguments to define a range of transactions between `from` (inclusive) and `until` (exclusive). One or more generic attributes are then added to the Log molecule to define what data to return. The following standard generic attributes can be combined to match the required data structure:

- `e` - Entity id (`Long`)
- `a` - Attribute (`String`)
- `v` - Value (`Any`)
- `t` - Transaction point in time (`Long` alternatively `Int`)
- `tx` - Transaction entity id (`Long`)
- `txInstant` - Transaction wall-clock time (`java.util.Date`)
- `op` - Operation: assertion / retraction (`Boolean` true/false)

Contrary to Datomic's Log implementation, Molecule returns data as a flat list of tuples of data that matches the generic attributes in the Log molecule. This is to transparently sharing the same semantics as other molecules.

```scala
// Data from transaction t1 (inclusive) until t4 (exclusive)
Log(Some(t1), Some(t4)).t.e.a.v.op.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 41, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true),

  (t3, e1, ":Person/age", 41, false),
  (t3, e1, ":Person/age", 42, true)
)
``` 

### From beginning

If the `from` argument is `None` data from the beginning of the log is matched:
```scala
Log(None, Some(t3)).v.e.t.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 41, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true)

  // t3 not included
)
``` 

### Until end

If the `until` argument is `None` data from until the end of the log is matched:
```scala
Log(Some(t2), None).v.e.t.get === List(
  // t1 not included

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true),

  (t3, e1, ":Person/age", 41, false),
  (t3, e1, ":Person/age", 42, true)
)
``` 

## Schema

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/SchemaTest.scala)


The generic Schema interface provides attributes to build a Schema molecule that query the Schema structure of the current database. Datomic's bootstrapped attributes are transparently filtered out so that only schema data of the current database is returned.


### Generic Schema attributes

The following Schema attributes can be used to build Schema molecules:

| Attribute                       | Description                                                      | Example value                                         |
| :-                              | :-                                                               | :-                                                    |
| `id`                            | Attribute definition entity id                                   | `Long`                                                |
| `a`                             | (Partition-)Namespace-prefixed attribute name &nbsp;&nbsp;&nbsp; | ":ind_Person/name" or ":Person/name" if no partitions |
| `part`                          | Partition name.                                                  | "ind" or "" if no partitions                          |
| `nsFull`                        | Namespace name with partition prefix                             | "ind_Person" or "Person" if no partitions             |
| `ns`                            | Namespace name                                                   | "Person"                                              |
| `attr`                          | Attribute name                                                   | "name"                                                |
| `tpe`                           | Attribute Datomic type                                           | See types below                                       |
| `card`                          | Attribute cardinality                                            | "one"/"many"                                          |
| `doc`                           | Attribute documentation string                                   | `String`                                              |
| `index`                         | Attribute index status                                           | true / not set                                        |
| `unique`                        | Attribute uniqueness status                                      | true / not set                                        |
| `fulltext`                      | Attribute fulltext search status                                 | true / not set                                        |
| `isComponent`&nbsp;&nbsp;&nbsp; | Attribute isComponent status                                     | true / not set                                        |
| `noHistory`                     | Attribute noHistory status                                       | true / not set                                        |
| `enum`                          | Attribute enum values                                            | `String`                                              |
| `t`                             | Attribute definition transaction point in time                   | `Long` / `Int`                                        |
| `tx`                            | Attribute definition transaction entity id                       | `Long`                                                |
| `txInstant`                     | Attribute definition transaction wall-clock time                 | `java.util.Date`                                      |




### Querying the Schema structure

```scala
// List of attribute entity ids
val attrIds: Seq[Long] = Schema.id.get
```

### Partition/Namespace/Attribute names

```scala
// Attribute name elements
Schema.a.part.ns.nsFull.attr.get === List (
  (":sales_Customer/name", "sales", "Customer", "sales_Customer", "name"),
  (":accounting_Invoice/invoiceLine", "accounting", "Invoice", "accounting_Invoice", "invoiceLine"),
  // etc..
)
```

### Types and cardinality

```scala
// Datomic type and cardinality of attributes
Schema.a.tpe.card.get === List (
  (":sales_Customer/name", "string", "one"),
  (":accounting_Invoice/invoiceLine", "ref", "many")
)
```
Scala `Int` and `Long` are both represented as Datomic type `long`:


| Datomic type &nbsp;&nbsp;&nbsp; | Scala type       |
| :-                              | :-               |
| _string_                        | `String`         |
| _long_                          | `Int`            |
| _long_                          | `Long`           |
| _float_                         | `Float`          |
| _double_                        | `Double`         |
| _bigint_                        | `BigInt`         |
| _bigdec_                        | `BigDecimal`     |
| _boolean_                       | `Boolean`        |
| _instant_                       | `java.util.Date` |
| _uuid_                          | `java.util.UUID` |
| _uri_                           | `java.net.URI`   |
| _ref_                           | `Long`           |



### Optional docs and attribute options

These can be retrieved as mandatory or optional attribute values

```scala
Schema.a
      .index
      .doc$
      .unique$
      .fulltext$
      .isComponent$
      .noHistory$
      .get === List(
  (":sales_Customer/name",
    true,            // indexed
    "Customer name", // doc
    None,            // Uniqueness not set
    Some(true),      // Fulltext search set so that we can search for names
    None,            // Not a component
    None             // History is preserved (noHistory not set)
    ),
  (":accounting_Invoice/invoiceLine",
    true,                   // indexed
    "Ref to Invoice lines", // doc
    None,                   // Uniqueness not set
    None,                   // Fulltext search not set
    Some(true),             // Invoice is a component - owns invoice lines
    None                    // History is preserved (noHistory not set)
    ),
)
```

### Enum values

Enumerated values can be defined in the schema and then retrieved generically with `Schema.enum`:
```scala
// Defined enum values
Schema.a.enum.get.groupBy(_._1).map(g => g._1 -> g._2) === Map(
  ":Person/gender" -> List("female", "male"),
  ":Interests/sports" -> List("golf", "basket", "badminton")
)
```

### Schema transaction times

"In what transaction/when were the attributes created in the schema?"
```scala
Schema.t.tx.txInstant.get === List(
  (t1, tx1, <Date: 2018-11-07 09:28:10>), // Initial schema transaction
  (t2, tx2, <Date: 2019-01-12 12:43:27>), // Additional schema attribute definitions...
)
``` 

