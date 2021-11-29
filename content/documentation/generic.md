---
title: "Generic APIs"
weight: 100
menu:
  main:
    parent: documentation
---

# Generic APIs

Here, we'll walk through how Molecule provides access to Datomic's various generic interfaces and apis.



## Entity API

As we saw in [building blocks](/intro/building-blocks/#entity), an entity can have attributes from multiple namespaces
{{< bootstrap-table "table table-bordered" >}}
Entity id | Attribue      | Value    
:---:     | :---          | :---  
johnId    | :Person/name  | "John"
johnId    | :Person/likes | "pizza"
johnId    | :Person/age   | 24
johnId    | :Site/cat     | "customer"
{{< /bootstrap-table >}}

At runtime we can explore what attributes an entity has by calling `touch` directly on a `Long` entity id:

```scala
johnId.touch.map(_ ==> Map(
  ":db/id" -> johnId,
  ":Person/name"  -> "John", 
  ":Person/likes" -> "pizza", 
  ":Person/age"   -> 24, 
  ":Site/cat"     -> "customer"
))
```

The `touch` method recursively retrieves referenced entities.

So if John had references, we migh want to apply a max depth level with `touch(<maxLevel>)`:

```scala
johnId.touchMax(2).map(_ ==> Map(
  ":db/id" -> johnId,
  ":Person/name" -> "John"
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
    Map(...) // + more friends of John (1 level deep)
  )
))
```

If we want to sort the key-value pairs we can also ask for a List with sorted pairs.
```scala
johnId.touchList.map(_ ==> List(
  ":db/id" -> johnId,
  ":Person/age"   -> 24, 
  ":Person/likes" -> "pizza", 
  ":Person/name"  -> "John", 
  ":Site/cat"     -> "customer"
))
```
The entity id pair `":db/id" -> johnId` is present for all touch calls and will always be first. The remaining pairs are hereafter sorted by key. Nested Lists likewise. 
     
#### Testing

When testing we also have a convenience method `touchListQuoted` that returns output that we can paste into tests so that we can avoid having to quote keys and Strings etc. For our example this would look like this: 

```scala
johnId.touchListQuoted.map(_ ==>
  """List(
    |  ":db/id" -> 101L,
    |  ":Person/age"   -> 24, 
    |  ":Person/likes" -> "pizza", 
    |  ":Person/name"  -> "John", 
    |  ":Site/cat"     -> "customer")""".stripMargin
)
```

`touch`, `touchList` and `touchQuoted` can all have a max-depth applied.


### Optional attribute values

We can look for an optionally present attribute value. Here we ask the entity id `johnId` if it has a `:Site/cat` attribute value:
```scala
johnId(":Site/cat").map(_ ==> Some("customer"))
johnId(":Person/age").map(_ ==> Some(24))

// Likewise, a non-present attribute returns None
johnId(":Site/member").map(_ ==> None) 
```













## Generic components

In the following sections, we'll refer to the 5 basic components E-A-V-T-Op of a [datom](/intro/building-blocks/#datom):

{{< bootstrap-table "table table-bordered" >}}
E      | A             | V     | T   | Op    
:-:    | :-:           | :-:   | :-: | :-:  
johnId | :Person/likes | pizza | t3  | true
{{< /bootstrap-table >}}


The T component can be either a point in time `t` in the database, a transaction entity id `tx` or transaction time `txInstant`.  

In Molecule we use the following syntax for generic attributes to represent the various components of datoms:

{{< bootstrap-table "table table-bordered" >}}
Generic attr | Type      | Component | Generic information   
:-           | :-        | :-:       | :-
`e`          | `Long`    | E         | Entity id
`a`          | `String`  | A         | Attribute
`v`          | `Any`     | V         | Value
`t`          | `Long`    | T         | Point in time in database
`tx`         | `Long`    | T         | Transaction entity id
`txInstant`  | `Date`    | T         | Transaction time
`op`         | `Boolean` | Op        | Operation: assertion (true) / retraction (false)
{{< /bootstrap-table >}}





## Datom API

The Datom API in Molecule is a set of generic attributes, as described above, that can be mixed with your custom attributes to handle generic information.

Here are some examples:

Get the entity id with `e`:
```scala
Person.e.name.get.map(_.head ==> (benId, "Ben"))
```

Get transaction info with `txInstant`, `t` or `tx`:
```scala
// When was Ben's age updated?
Person(benId).age.txInstant.get.map(_.head ==> (26, Date("April 4, 2019")))

// In which transaction entity was Ben's age updated?  
Person(benId).age.tx.get.map(_.head ==> (26, tx2))

// What's the transaction value t where Ben's age was updated?  
Person(benId).age.t.get.map(_.head ==> (26, t2))
```

With a history db we can access the point in time `t` in the database and assertion/retraction statuses with `op`

```scala
// When was Ben's age updated back in time?
Person(benId).age.t.op.getHistory.map(_.sortBy(r => (r._2, r._3)) ==> List(
  (25, t1, true),  // age 25 asserted in transaction t1
  (25, t2, false), // age 25 retracted in transaction t2
  (26, t2, true)   // age 26 asserted in transaction t2
))
```

### Fully generic Datom molecules

Sometimes we will be interested in more generic data where we don't know in advance what attributes will be involved. Then we can use the generic Datom attribute `a` for Attribute name and `v` for value. We could for instance ask what we know about an entity over time in the database:

```scala
// What do we know about the johnId entity?
Person(johnId).a.v.t.op.getHistory.map(_.sortBy(r => (r._2, r._3)) ==> List(
  (":Person/name", "John", t3, true), 
  (":Person/likes", "pizza", t3, true), 
  (":Person/likes", "pizza", t6, false),
  (":Person/likes", "pasta", t6, true)  
))
```

### Filtering with expressions

By applying values to generic attributes we can filter search results:

```scala
// What was asserted/retracted in transaction tx3 about what John likes? 
Person(johnId).likes.tx(tx6).op.getHistory.map(_.sortBy(r => (r._2, r._3)) ==> List(
  ("pizza", t6, false), // John no longer likes pizza
  ("pasta", t6, true)   // John now likes pasta
))
```











## 4 Index APIs

_Some index descriptions in the following sections are respectfully quoted from the [Datomic documentation](https://docs.datomic.com/on-prem/indexes.html)._

Datomic maintains four indexes that contain ordered sets of datoms. Each of these indexes is named based on the sort order used.  E, A, and V are always sorted in ascending order, while T is always in descending order:

- **EAVT** - Datoms sorted by Entity-Attribute-Value-Transaction.
- **AVET** - Datoms sorted by Attribute-Value-Entity-Transaction.
- **AEVT** - Datoms sorted by Attribute-Entity-Value-Transaction.
- **VAET** - "Reverse index" for reverse lookup of ref types.

Molecule provides access to each index by instantiating a corresponding Index object with one or more arguments and then adding generic attributes matching the data to be returned.

Contrary to Datomic's datoms API that returns Datoms, Molecule returns tuples of data matching the generic attributes added to the Index object. This way, Index molecules transparently share the same return type semantics as normal molecules.


### EAVT

The EAVT index provides efficient access to everything about a given entity. Conceptually this is very similar to row access style in a SQL database, except that entities can possess arbitrary attributes rather then being limited to a predefined set of columns.

```scala
// Create EAVT Index molecule with 1 entity id argument
EAVT(e1).e.a.v.t.get.map(_ ==> List(
  (e1, ":Person/name", "Ben", t1),
  (e1, ":Person/age", 25, t2),
  (e1, ":Golf/score", 5.7, t2)
))

// Maybe we are only interested in the attribute/value pairs:
EAVT(e1).a.v.get.map(_ ==> List(
  (":Person/name", "Ben"),
  (":Person/age", 25),
  (":Golf/score", 5.7)
))

// Two arguments to narrow the search
EAVT(e1, ":Person/age").a.v.get.map(_ ==> List(
  (":Person/age", 25)
))
``` 


### AVET

The AVET index provides efficient access to particular combinations of attribute and value.

```scala
// Create AVET Index molecule with 1 entity id argument
AVET(":Person/age").v.e.t.get.map(_ ==> List(
  (25, e1, t2),
  (23, e2, t5),
  (14, e3, t7)
))

// Narrow search with multiple arguments
AVET(":Person/age", 25).e.t.get.map(_ ==> List( (e1, t2) ))
AVET(":Person/age", 25, e1).e.v.get.map(_ ==> List( (e1, t2) ))
AVET(":Person/age", 25, e1, t2).e.v.get.map(_ ==> List( (e1, t2) ))
```

The AVET Index can be filtered by a range of values between `from` (inclusive) and `until` (exclusive) for an attribute:

```scala
AVET.range(":Person/age", Some(14), Some(20)).v.e.t.get.map(_ ==> List(
  (14, e4, t7) // 14 is included in value range
               // 23 not included in value range
               // 25 outside value range
))

// If `from` is None, the range starts from the beginning
AVET.range(":Person/age", None, Some(40)).v.e.t.get.map(_ ==> List(
  (14, e3, t7),
  (23, e2, t5)
))

// If `until` is None, the range goes to the end
AVET.range(":Person/age", Some(20), None).v.e.t.get.map(_ ==> List(
  (23, e2, t5),
  (25, e1, t2)
))
```

### AEVT

The AEVT index provides efficient access to all values for a given attribute, comparable to traditional column access style.

```scala
// Create AEVT Index molecule with 1 entity id argument
AEVT(":Person/name").e.v.t.get.map(_ ==> List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
))

// Narrow search with multiple arguments
AEVT(":Person/name", e1).e.v.get.map(_ ==> List( (e1, "Ben") ))
AEVT(":Person/name", e1, "Ben").e.v.get.map(_ ==> List( (e1, "Ben") ))
AEVT(":Person/name", e1, "Ben", t2).e.v.get.map(_ ==> List( (e1, "Ben") ))
```

### VAET

The VAET index contains all and only datoms whose attribute has a :db/valueType of :db.type/ref. This is also known as the reverse index, since it allows efficient navigation of relationships in reverse.

```scala
// Say we have 3 entities pointing to one entity:
Release.e.name.Artists.e.name.get.map(_ ==> List(
  (r1, "Abbey Road", a1, "The Beatles"),
  (r2, "Magical Mystery Tour", a1, "The Beatles"),
  (r3, "Let it be", a1, "The Beatles")
))

// .. then we can get the reverse relationships with the VAET Index:
VAET(a1).v.a.e.get.map(_ ==> List(
  (a1, ":Release/artists", r1),
  (a1, ":Release/artists", r2),
  (a1, ":Release/artists", r3)
))

// Narrow search with multiple arguments
VAET(a1, ":Release/artist").e.get.map(_ ==> List(r1, r2, r3))
VAET(a1, ":Release/artist", r2).e.get.map(_ ==> List(r2))
VAET(a1, ":Release/artist", r2, t7).e.get.map(_ ==> List(r2))
```














## Log API

_Some index descriptions in the following sections respectfully quoted from the [Datomic documentation](https://docs.datomic.com/on-prem/log.html)._

Datomic's database log is a recording of all transaction data in historic order, organized for efficient access by transaction. The Log is therefore an efficient source of finding data by transaction time.


### Tx range and generic Log attributes

The Molecule Log implementation takes two arguments to define a range of transactions between `from` (inclusive) and `until` (exclusive). One or more generic attributes are then added to the Log molecule to define what data to return.


Contrary to Datomic's Log implementation, Molecule returns data as a flat list of tuples of data that matches the generic attributes in the Log molecule. This is to transparently share the same return semantics as other molecules.

```scala
// Data from transaction t1 (inclusive) until t4 (exclusive)
Log(Some(t1), Some(t4)).t.e.a.v.op.get.map(_ ==> List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 25, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 23, true),

  (t3, e1, ":Person/age", 25, false),
  (t3, e1, ":Person/age", 26, true)
))
``` 

### From beginning

If the `from` argument is `None` data from the beginning of the log is matched:
```scala
Log(None, Some(t3)).v.e.t.get.map(_ ==> List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 25, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 23, true)

  // t3 not included
))
``` 

### Until end

If the `until` argument is `None` data from until the end of the log is matched:
```scala
Log(Some(t2), None).v.e.t.get.map(_ ==> List(
  // t1 not included

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 23, true),

  (t3, e1, ":Person/age", 25, false),
  (t3, e1, ":Person/age", 26, true)
))
``` 













## Schema API

A Datomic database schema is saved as data which makes it possible to make queries about the structure of the schema.

Query your schema with `Schema` and build on with schema attributes:

{{< bootstrap-table "table table-bordered" >}}
| Attribute                       | Description                                                      | Example value                                         |
| :-                              | :-                                                               | :-                                                    |
| `id`                            | Attribute definition entity id                                   | `Long`                                                |
| `a`                             | (Partition-)Namespace-prefixed attribute name &nbsp;&nbsp;&nbsp; | ":ind_Person/name" or ":Person/name" if no partitions |
| `part`                          | Partition name.                                                  | "ind" or "" if no partitions                          |
| `nsFull`                        | Namespace name with partition prefix                             | "ind_Person" or "Person" if no partitions             |
| `ns`                            | Namespace name                                                   | "Person"                                              |
| `attr`                          | Attribute name                                                   | "name"                                                |
| `tpe`                           | Attribute Datomic type                                           | See types below                                       |
| `card`                          | Attribute cardinality                                            | "one" / "many"                                        |
| `doc`                           | Attribute documentation string                                   | `String`                                              |
| `index`                         | Attribute index status                                           | true / false                                          |
| `unique`                        | Attribute uniqueness status                                      | true / false                                          |
| `fulltext`                      | Attribute fulltext search status                                 | true / false                                          |
| `isComponent`&nbsp;&nbsp;&nbsp; | Attribute isComponent status                                     | true / false                                          |
| `noHistory`                     | Attribute noHistory status                                       | true / false                                          |
| `enum`                          | Attribute enum values                                            | `String`                                              |
| `t`                             | Attribute definition transaction point in time                   | `Long` / `Int`                                        |
| `tx`                            | Attribute definition transaction entity id                       | `Long`                                                |
| `txInstant`                     | Attribute definition transaction wall-clock time                 | `java.util.Date`                                      |
{{< /bootstrap-table >}}

Since the schema is based on your Data Model, Schema queries are also a way to query your Data Model.

Datomic's bootstrapped attributes are transparently filtered out so that only custom schema data based on your Data Model is returned.

Here are some examples:

### Attribute names

Here we find various elements of attribute names:

```scala
Schema.a.part.ns.nsFull.attr.get.map(_ ==> List (
  (":sales_Customer/name", "sales", "Customer", "sales_Customer", "name"),
  (":accounting_Invoice/invoiceLine", "accounting", "Invoice", "accounting_Invoice", "invoiceLine"),
  // etc..
))
```

### Types and cardinality

Datomic type and cardinality of attributes
```scala
Schema.a.tpe.card.get.map(_ ==> List (
  (":sales_Customer/name", "string", "one"),
  (":accounting_Invoice/invoiceLine", "ref", "many")
))
```
Scala `Int` and `Long` are both represented as Datomic type `long`:

{{< bootstrap-table "table table-bordered" >}}
| Datomic type &nbsp;&nbsp;&nbsp; | Scala type       |
| :-                              | :-               |
| _string_                        | `String`         |
| _long_                          | `Int`            |
| _long_                          | `Long`           |
| _float_                         | `Double` *       |
| _double_                        | `Double`         |
| _bigint_                        | `BigInt`         |
| _bigdec_                        | `BigDecimal`     |
| _boolean_                       | `Boolean`        |
| _instant_                       | `java.util.Date` |
| _uuid_                          | `java.util.UUID` |
| _uri_                           | `java.net.URI`   |
| _ref_                           | `Long`           |
{{< /bootstrap-table >}}

*) Due to [limitations in JavaScript](http://www.scala-js.org/doc/semantics.html), some `Float` precision is lost on the js platform. Please use `Double` instead to ensure safe double precision.


### Optional docs and attribute options

These can be retrieved as mandatory or optional attribute values:

```scala
Schema.a
      .index
      .doc$
      .unique$
      .fulltext$
      .isComponent$
      .noHistory$
      get.map(_ ==> List(
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
))
```

### Enum values

Enumerated values can be defined in the schema and then retrieved generically with `Schema.enum`:
```scala
// Defined enum values
Schema.a.enum.get.groupBy(_._1).map(g => g._1 -> g._2) ==> Map(
  ":Person/gender" -> List("female", "male"),
  ":Interests/sports" -> List("golf", "basket", "badminton")
))
```

### Schema transaction times

"In what transaction/when were the attributes created in the schema?"
```scala
Schema.t.tx.txInstant.get.map(_ ==> List(
  (t1, tx1, <Date: 2018-11-07 09:28:10>), // Initial schema transaction
  (t2, tx2, <Date: 2019-01-12 12:43:27>), // Additional schema attribute definitions...
))
``` 

