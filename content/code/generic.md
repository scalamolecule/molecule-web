---
title: "Generic APIs"
weight: 100
menu:
  main:
    parent: code
    identifier: generic
up:   /manual/time
prev: /manual/time/testing
next: /manual/generic/datom
down: /manual/debug
---

# Generic attributes

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic)

Molecule provides access to generic data about data and Schema with the following 7 generic interfaces (each with a little example - read more by clicking on each title):


#### [Datom](datom)

Get id of Ben entity with generic Datom attribute `e`
```
Person.e.name.get.head === (benEntityId, "Ben")
```

#### [EAVT Index](indexes)

Attributes and values of entity e1
```
EAVT(e1).a.v.get === List(
  (":Person/name", "Ben"),
  (":Person/age", 42),
  (":Golf/score", 5.7)
)
``` 

#### [AVET Index](indexes)

Values, entities and transactions where attribute :Person/age is involved
```
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
```
AEVT(":Person/name").e.v.t.get === List(
  (e1, "Ben", t2),
  (e2, "Liz", t5)
)
``` 

#### [VAET Index](indexes)

Get entities pointing to entity a1
```
VAET(a1).v.a.e.get === List(
  (a1, ":Release/artists", r1),
  (a1, ":Release/artists", r2),
  (a1, ":Release/artists", r3),
)
``` 

#### [Log](log)

Data from transaction t1 until t4 (exclusive)
```
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
```
Schema.part.ns.attr.fulltext$.doc.get === List(
  ("ind", "person", "name", Some(true), "Person name"), // fulltext search enabled
  ("ind", "person", "age", None, "Person age"),
  ("cat", "sport", "name", None, "Sport category name")
)
``` 

## Datom

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Datom.scala)


### Datoms / quintuplets

Attribute values are saved as quintuplets of information in Datomic:

![](/img/generic/datom.png)
<br><br>




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

```
// Get entity id of Ben with generic datom attribute `e` on a custom molecule
Person.e.name.get.head === (benEntityId, "Ben")
```

And we can get information about the transaction time of the assertion of some custom attribute value:

```
// When was Ben's age updated? Using `txInstant`
Person(benEntityId).age.txInstant.get.head === (42, <April 4, 2019>) // (Date)
```

With a history db we can access the transaction number `t` and assertion/retraction statusses with `op`

```
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

```
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

```
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

```
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

```
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

```
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

```
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

```
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

```
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
```
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
```
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

<table>
<th>
    <tr><td><i><b>Attribute</i></b>         &nbsp;&nbsp;</td> <td><b><i>Description</i></b></td> <td><b><i>Example value</i></b></td></tr>
</th>
<tr><td>`id`         &nbsp;&nbsp;</td> <td>Attribute definition entity id</td><td>`Long`</td></tr>
<tr><td>`a`          &nbsp;&nbsp;</td> <td>(Partition-)Namespace-prefixed attribute name &nbsp;&nbsp;&nbsp;</td> <td>":ind_Person/name" or ":Person/name" if no partitions</td></tr>
<tr><td>`part`       &nbsp;&nbsp;</td> <td>Partition name.</td> <td>"ind" or "" if no partitions</td></tr>
<tr><td>`nsFull`     &nbsp;&nbsp;</td> <td>Namespace name with partition prefix</td> <td>"ind_Person" or "Person" if no partitions</td></tr>
<tr><td>`ns`         &nbsp;&nbsp;</td> <td>Namespace name</td> <td>"Person"</td></tr>
<tr><td>`attr`       &nbsp;&nbsp;</td> <td>Attribute name</td> <td>"name"</td></tr>
<tr><td>`tpe`        &nbsp;&nbsp;</td> <td>Attribute Datomic type</td> <td>See types below </td></tr>
<tr><td>`card`       &nbsp;&nbsp;</td> <td>Attribute cardinality</td> <td>"one"/"many"</td></tr>
<tr><td>`doc`        &nbsp;&nbsp;</td> <td>Attribute documentation string</td> <td>`String`</td></tr>
<tr><td>`index`      &nbsp;&nbsp;</td> <td>Attribute index status</td> <td>true / not set</td></tr>
<tr><td>`unique`     &nbsp;&nbsp;</td> <td>Attribute uniqueness status</td> <td>true / not set</td></tr>
<tr><td>`fulltext`   &nbsp;&nbsp;</td> <td>Attribute fulltext search status</td> <td>true / not set</td></tr>
<tr><td>`isComponent`&nbsp;&nbsp;</td> <td>Attribute isComponent status</td> <td>true / not set</td></tr>
<tr><td>`noHistory`  &nbsp;&nbsp;</td> <td>Attribute noHistory status</td> <td>true / not set</td></tr>
<tr><td>`enum`       &nbsp;&nbsp;</td> <td>Attribute enum values</td> <td>`String`</td></tr>
<tr><td>`t`          &nbsp;&nbsp;</td> <td>Attribute definition transaction point in time</td> <td>`Long` / `Int`</td></tr>
<tr><td>`tx`         &nbsp;&nbsp;</td> <td>Attribute definition transaction entity id</td> <td>`Long`</td></tr>
<tr><td>`txInstant`  &nbsp;&nbsp;</td> <td>Attribute definition transaction wall-clock time</td> <td>`java.util.Date`</td></tr>
</table>&nbsp; <!-- hack to force markdown handling inside table -->



### Querying the Schema structure

```
// List of attribute entity ids
val attrIds: Seq[Long] = Schema.id.get
```

### Partition/Namespace/Attribute names

```
// Attribute name elements
Schema.a.part.ns.nsFull.attr.get === List (
  (":sales_Customer/name", "sales", "Customer", "sales_Customer", "name"),
  (":accounting_Invoice/invoiceLine", "accounting", "Invoice", "accounting_Invoice", "invoiceLine"),
  // etc..
)
```

### Types and cardinality

```
// Datomic type and cardinality of attributes
Schema.a.tpe.card.get === List (
  (":sales_Customer/name", "string", "one"),
  (":accounting_Invoice/invoiceLine", "ref", "many")
)
```
Scala `Int` and `Long` are both represented as Datomic type `long`:


<table>
<th>
    <tr><td><b>Datomic type</b>&nbsp;&nbsp;&nbsp;&nbsp;</td> <td><b>Scala type</b></td></tr>
</th>
<tr><td><i>string                   </i></td> <td>`String`</td></tr>
<tr><td><i>long                     </i></td> <td>`Int`</td></tr>
<tr><td><i>long                     </i></td> <td>`Long`</td></tr>
<tr><td><i>float                    </i></td> <td>`Float`</td></tr>
<tr><td><i>double                   </i></td> <td>`Double`</td></tr>
<tr><td><i>bigint                   </i></td> <td>`BigInt`</td></tr>
<tr><td><i>bigdec                   </i></td> <td>`BigDecimal`</td></tr>
<tr><td><i>boolean&nbsp;&nbsp;&nbsp;</i></td> <td>`Boolean`</td></tr>
<tr><td><i>instant                  </i></td> <td>`java.util.Date`</td></tr>
<tr><td><i>uuid                     </i></td> <td>`java.util.UUID`</td></tr>
<tr><td><i>uri                      </i></td> <td>`java.net.URI`</td></tr>
<tr><td><i>ref                      </i></td> <td>`Long`</td></tr>
</table>&nbsp; <!-- hack to force markdown handling inside table -->


### Optional docs and attribute options

These can be retrieved as mandatory or optional attribute values

```
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
```
// Defined enum values
Schema.a.enum.get.groupBy(_._1).map(g => g._1 -> g._2) === Map(
  ":Person/gender" -> List("female", "male"),
  ":Interests/sports" -> List("golf", "basket", "badminton")
)
```

### Schema transaction times

"In what transaction/when were the attributes created in the schema?"
```
Schema.t.tx.txInstant.get === List(
  (t1, tx1, <Date: 2018-11-07 09:28:10>), // Initial schema transaction
  (t2, tx2, <Date: 2019-01-12 12:43:27>), // Additional schema attribute definitions...
)
``` 

