---
title: "Schema"
weight: 40
menu:
  main:
    parent: generic
    identifier: generic-schema
up:   /manual/generic
prev: /manual/generic/log
next: /manual/debug
down: /manual/debug
---


# Schema API

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/SchemaTest.scala)


The generic Schema interface provides attributes to build a Schema molecule that query the Schema 
structure of the current database. Datomic's bootstrapped attributes are transparently filtered out so that only
schema data of the current database is returned.


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


### Next

[Debug...](/manual/debug)