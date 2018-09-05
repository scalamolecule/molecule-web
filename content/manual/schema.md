---
date: 2015-01-02T22:06:44+01:00
title: "Schema"
weight: 20
menu:
  main:
    parent: manual
    identifier: schema
    
up:   /manual/getting-started
prev: /manual/getting-started/setup
next: /manual/schema/transaction
down: /manual/attributes
---

# Schema

A [Datomic schema](http://docs.datomic.com/schema.html) defines the set of possible attributes that we can use. 

In Molecule we make this definition 
in a Schema definition file:

## Schema definition file

Molecule provides an intuitive and type-safe dsl to model your schema in a Schema definition file. 
After each change you make in this file you need to compile your project with `sbt compile` so that the
sbt-plugin can create a Molecule DSL from your definitions.

Let's look at the schema definition from the [Seattle tutorial](/resources/tutorials/seattle):

```scala
package yourpackage
import molecule.schema.definition._  // import schema definition DSL

@InOut(3, 8)
object SeattleDefinition {

  trait Community {
    val name         = oneString.fulltextSearch
    val url          = oneString
    val category     = manyString.fulltextSearch
    val orgtype      = oneEnum('community, 'commercial, 'nonprofit, 'personal)
    val `type`       = oneEnum('email_list, 'twitter, 'facebook_page) // + more...
    val neighborhood = one[Neighborhood]
  }

  trait Neighborhood {
    val name     = oneString
    val district = one[District]
  }

  trait District {
    val name   = oneString
    val region = oneEnum('n, 'ne, 'e, 'se, 's, 'sw, 'w, 'nw)
  }
}
```

The outer object `SeattleDefinition` encapsulates our schema definition. All such objects have to have a 
named ending in "Definition" in order for the sbt-MoleculePlugin to be able to find it.


### Molecule arity

The `@InOut(3, 8)` arity annotation instructs Molecule to generate boilerplate code with the ability to create
molecules with up to 8 attributes including up to 3 [input attributes](/manual/attributes/parameterized).

When developing your schema you might just set the first arity annotation variable for input attributes to `0` and
then later when your schema is stabilizing add the ability to make input molecules by setting it to 1, 2 or 3 (the maximum). 
Using parameterized input attributes can be a performance
optimization since using input values in Datalog queries allow Datomic to cache the query. 

The second arity annotation parameter basically tells how long molecules you can build (this doesn't affect
 how many attributes you can _define_ in each namespace). Generally you want to keep this number as low as possible
 not to generate more boilerplate code than necessary. The maximum arity is 22, the same as for tuples. 
 
If you at some point need to make molecules with more than 22 attributes
  you can insert/query in two steps as described in [attribute basics](/manual/attributes/basics).


### Namespaces

Attribute names in Datomic are namespaced keywords with the lexical form `<namespace>/<name>`. Molecule lets you
define the `<namespace>` part with the name of the trait, like `Community` in the Seattle 
example above. In this way Molecule can construct the full name of the `community/category` attribute etc.

![Schema](/img/DatomicElements1.png)

We might as well have defined a list of attributes manually adding a namespace 
part to each attribute:

```
val communityName
val communityUrl
//...

val neighborhoodName
// etc...
```

### Namespace != Table

If coming from an sql background one might at first think of a namespace as
a table having columns (attributes). But this is not the case. An
entity in Datomic can associate values of attributes _from any namespace_:

![](/img/DatomicElements2.png)

So, when we build a molecule

```
val toughCommunities = Community.name.Neighborhood.name("Tough").get
```

we _shouldn't_ think of it like a

"`Community` table with `name` field with a join to `Neighborhood` table with a `name` field set to 'Tough'" (wrong!)

but rather think it as

"**_Entities_** with a `communityName` attribute having a reference to an entity with a `neighborhoodName` value 'Tough'"





## Partitions

Namespaces can also be organized in partitions.

From the [Datomic schema][schema] reference:

"All entities created in a database reside within a partition. Partitions group data together, providing locality of reference 
when executing queries across a collection of entities. In general, you want to group entities based on how you'll use them. 
Entities you'll often query across - like the community-related entities in our sample data - should be in the same partition 
to increase query performance. Different logical groups of entities should be in different partitions. Partitions are discussed 
in more detail in the Indexes topic."

In Molecule we can organize namespaces in partitions with objects:

### 

```scala
@InOut(0, 4)
object PartitionTestDefinition {

  object gen {
    trait Person {
      val name   = oneString
      val gender = oneEnum('male, 'female)
    }
    // ..more namespaces in the `gen` partition
  }

  object lit {
    trait Book {
      val title  = oneString
      val author = one[gen.Person]
      // To avoid attr/partition name clashes we can prepend the definition object name
      // (in case we would have needed an attribute named `gen` for instance)
      val editor = one[PartitionTestDefinition.gen.Person]
      val cat    = oneEnum('good, 'bad)
    }
    // ..more namespaces in the `lit` partition
  }
}
```
Here we have a `gen` (general) partition and a `lit` (litterature) partition. Each partition can contain as many 
namespaces as you want. This can be a way also to structure large domains conceptually. The partition name is 
prepended to the namespaces it contains. 

When we build molecules the partition name is prepended to the namespace like this:

```scala
lit_Book.title.cat.Author.name.gender.get === ...
```

Since `Author` is already defined as a related namespace we don't need to prepend the partition name there.

When we insert a `Person` the created entity will automatically be saved in the `gen` partition (or whatever we call it).



## Attribute types

In the Seattle example we see the attributes being defined with the following types that should be
 pretty self-explanatory:

- `oneString`, `manyString` etc defines cardinality and type of an attribute
- `oneEnum`/`manyEnum` defines enumerated values (pre-defined words)
- `one[<ReferencedNamespace>]` defines a reference to another namespace

We can define the following types of attributes:

```
Cardinality one              Cardinality many                 Mapped cardinality many
-------------------          -------------------------        --------------------------------
oneString     : String       manyString    : Set[String]      mapString     : Map[String, String]
oneInt        : Int          manyInt       : Set[Int]         mapInt        : Map[String, Int]
oneLong       : Long         manyLong      : Set[Long]        mapLong       : Map[String, Long]
oneFloat      : Float        manyFloat     : Set[Float]       mapFloat      : Map[String, Float]
oneDouble     : Double       manyDouble    : Set[Double]      mapDouble     : Map[String, Double]
oneBigInt     : BigInt       manyBigInt    : Set[BigInt]      mapBigInt     : Map[String, BigInt]
oneBigDecimal : BigDecimal   manyBigDecimal: Set[BigDecimal]  mapBigDecimal : Map[String, BigDecimal]
oneBoolean    : Boolean      manyBoolean   : Set[Boolean]     mapBoolean    : Map[String, Boolean]
oneDate       : Date         manyDate      : Set[Date]        mapDate       : Map[String, Date]
oneUUID       : UUID         manyUUID      : Set[UUID]        mapUUID       : Map[String, UUID]
oneURI        : URI          manyURI       : Set[URI]         mapURI        : Map[String, URI]
oneEnum       : String       manyEnum      : Set[String]
```

Cardinality-one attributes can have one value per entity.

Cardinality-many attributes can have a _Set of unique values_ per entity. Often we choose instead to model many-values as a 
many-reference to another entity that could have more than one attribute.

Mapped cardinality many attributes are a special Molecule variation based on cardinality-many attributes. Read more [here](/manual/query/mapped)...


### Reference types

References are also treated like attributes. It's basically a reference to one or many entities. 
We define such relationship by supplying the referenced namespace as the type parameter to `one`/`many`:
```
Cardinality one         Cardinality many
---------------         ----------------
one[<Ref-namespace>]    many[<Ref-namespace>]
```
In the example above we saw a reference from Community to Neighborhood defined as `one[Neighborhood]`. We would for instance 
likely define an Order/OrderLine relationship in an Order namespace as `many[OrderLine]`.


## Attribute options

Each attribute can also have some extra options:

<p>
<table border="1" cellpadding="5" cellspacing="0" style="background-color:#f5f5f5;">
  <tr>
    <th align="left" valign="top" scope="col">Option</th>
    <th valign="top" scope="col"><strong>Indexes</strong></th>
    <th scope="col">Description</th>
  </tr>
  <tr valign="top">
    <td valign="top">doc</td>
    <td align="center" valign="top">&nbsp;</td>
    <td>Attribute description.</td>
  </tr>
  <tr valign="top">
    <td valign="top">uniqueValue</td>
    <td align="center" valign="top">✔︎</td>
    <td>Attribute value is unique to each entity.<br>
      <em>Attempts to insert a duplicate value for a different entity id will fail.</em></td>
  </tr>
  <tr valign="top">
    <td valign="top">uniqueIdentity</td>
    <td align="center" valign="top">✔︎</td>
    <td>Attribute value is unique to each entity and &quot;upsert&quot; is enabled.<br>
      <em>Attempts to insert a duplicate value for a temporary entity id will cause all attributes associated with that temporary 
      id to be merged with the entity already in the database.</em></td>
  </tr>
  <tr>
    <td valign="top">indexed</td>
    <td align="center" valign="top">✔︎</td>
    <td>Generated index for this attribute. By default all attributes are set with the indexed option automatically by Molecule, so you don't need to set this.</td>
  </tr>
  <tr>
    <td valign="top">fulltextSearch</td>
    <td align="center" valign="top">✔︎</td>
    <td>Generate eventually consistent fulltext search index for this attribute.</td>
  </tr>
  <tr>
    <td valign="top">isComponent</td>
    <td align="center" valign="top">✔︎</td>
    <td>Specifies that an attribute whose type is :db.type/ref is a component. Referenced entities become subcomponents of the entity to which the attribute is applied.<br>
    <em>When you retract an entity with :db.fn/retractEntity, all subcomponents are also retracted. When you touch an entity, all its 
    subcomponent entities are touched recursively.</em></td>
  </tr>
  <tr>
    <td valign="top">noHistory</td>
    <td align="center" valign="top">&nbsp;</td>
    <td>Whether past values of an attribute should not be retained.</td>
  </tr>
</table>
</p>

Datomic indexes the values of all attributes having an option except for the `doc` and `noHistory` options.

As you saw, we added `fulltextSearch` to some of the attributes in the Seattle definition above. Molecule's schema 
definition DSL let's you only choose allowed options for any attribute type.

### Next

[Schema transaction...](/manual/schema/transaction)



[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[schema]: http://docs.datomic.com/schema.html

[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial
