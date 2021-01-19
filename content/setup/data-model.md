---
title: "1. Data Model"
weight: 20
menu:
  main:
    parent: setup
---

# Data Model

Your Data Model is defined in a plain Scala file that you can have anywhere in your project. Molecule interprets this file and creates Molecule boilerplate code based on your Data Model so that you can write molecule queries with your domain terms.

For Molecule to recognize your Data Model, a simple Data Model definition DSL is imported. A Scala object with the name or your domain and "DataModel" added should contain your definitions:

```scala
package path.to.your.project
import molecule.core.data.model._  // Data Model DSL

object YourDomainDataModel { // Name ending in "DataModel"

    // ... Attribute definitions within Namespaces
}
```
Next, attributes needs to be defined within Namespaces.





## Attributes in Namespaces

An attribute is the smallest unit of information of a Data Model. It's like a field in a database, a property, or, well, an "attribute" of something.

Attributes are organised within "namespaces" that semantically group related attributes. If used with SQL it would correspond to fields in a Table. But with Datomic, the semantics of a namespace is more of a "prefix" to the attribute.

Let's look at an example Data Model from the [Seattle tutorial](/community/seattle/):

```scala
package path.to.your.project
import molecule.core.data.model._  // import Data Model DSL

@InOut(2, 8)
object SeattleDataModel {

  trait Community {
    val name         = oneString.fulltext.doc("A community's name") // optional doc text
    val url          = oneString
    val category     = manyString.fulltext
    val orgtype      = oneEnum("community", "commercial", "nonprofit", "personal")
    val `type`       = oneEnum("email_list", "twitter", "facebook_page") // + more...
    val neighborhood = one[Neighborhood]
  }

  trait Neighborhood {
    val name     = oneString
    val district = one[District]
  }

  trait District {
    val name   = oneString
    val region = oneEnum("n", "ne", "e", "se", "s", "sw", "w", "nw")
  }
}
```




### Molecule arity

The `@InOut(2, 8)` arity annotation at the top instructs the generated boilerplate code to able to create molecules with up to 2 [input attributes](/code/attributes/#input-molecules) and up to 8 "output" attributes.

When developing your Data Model you might just set the first arity annotation variable for input attributes to `0` and then later when your model is stabilizing, then you can add the ability to make input molecules by setting it to 1, 2 or 3 (the maximum). Using parameterized input attributes can be a performance optimization since using input values in queries allows Datomic to cache the query. 

The second arity annotation parameter basically tells how long molecules you can build. This doesn't affect how many attributes you can _define_ in each namespace in the Data Model. The maximum arity of a molecule and for this annotation parameter is 22, the same as for tuples. 
 
>If you at some point need to make molecules with more than 22 attributes you can use [composite molecules](/code/relationships/#composite-molecules).




## Namespaces in Partitions

If your Data Model gets big, you can use an extra layer of organization with "Partitions" that encapsulate multiple related Namespaces. 

A "Partition" in Molecule is used as a conceptual term and not in the traditional sense as a physical database partition. The whole Data Model should be regarded as a conceptual model that can be projected later onto various databases. A database admin might choose to partition a database according to our conceptual partitions and in that case the terms would correlate. 

We could for instance group some generic namespaces (and their respective attribues) in a `gen` partition. And some Namespaces about literature in a `lit` Partition:

```scala
@InOut(3, 22)
object BookstoreDataModel {

  object gen {
    trait Person {
      val name   = oneString
      val gender = oneEnum("male", "female")
    }
    // ..more namespaces in the `gen` partition
  }

  object lit {
    trait Book {
      val title     = oneString
      val author    = one[gen.Person] // ref to namespace in other partition
      val publisher = one[Publisher]  // ref to namespace in this partition
      val cat       = oneEnum("good", "bad")
    }
    trait Publisher {
      val name = oneString
    }
    // ..more namespaces in the `lit` partition
  }
}
```
Each partition can contain as many namespaces as you want. 

Partition names have to be in lowercase and are prepended to the namespaces it contains with an underscore inbetween:

```scala
lit_Book.title.cat.Author.name.gender.get === ...
```

Since `Author` is already defined as a related namespace we don't need to prepend the partition name there.


## Attribute types

In the Seattle example we saw how attributes are defined by assigning various DSL settings to a named variable:

- `oneString`, `manyString` etc defines cardinality and type of an attribute.
- `oneEnum`/`manyEnum` defines enumerated values (pre-defined words).
- `one[<ReferencedNamespace>]` defines a reference to another namespace.

We can define the following types of attributes:

```
Cardinality-one             Cardinality-many                 Mapped cardinality-many
-------------------         -------------------------        --------------------------------
oneString    : String       manyString    : Set[String]      mapString    : Map[String, String]
oneInt       : Int          manyInt       : Set[Int]         mapInt       : Map[String, Int]
oneLong      : Long         manyLong      : Set[Long]        mapLong      : Map[String, Long]
oneFloat     : Float        manyFloat     : Set[Float]       mapFloat     : Map[String, Float]
oneDouble    : Double       manyDouble    : Set[Double]      mapDouble    : Map[String, Double]
oneBigInt    : BigInt       manyBigInt    : Set[BigInt]      mapBigInt    : Map[String, BigInt]
oneBigDecimal: BigDecimal   manyBigDecimal: Set[BigDecimal]  mapBigDecimal: Map[String, BigDecimal]
oneBoolean   : Boolean      manyBoolean   : Set[Boolean]     mapBoolean   : Map[String, Boolean]
oneDate      : Date         manyDate      : Set[Date]        mapDate      : Map[String, Date]
oneUUID      : UUID         manyUUID      : Set[UUID]        mapUUID      : Map[String, UUID]
oneURI       : URI          manyURI       : Set[URI]         mapURI       : Map[String, URI]
oneEnum      : String       manyEnum      : Set[String]
```

Cardinality-one attributes can have one value per entity.

Cardinality-many attributes can have a `Set` of unique values per entity. Often we choose instead to model many-values as a many-reference to another entity that could have more than one attribute.

Mapped cardinality-many attributes are a special Molecule variation based on cardinality-many attributes. Read more [here](/code/attributes/#map-attributes)...



## References

References are also treated like attributes. It's basically a reference to one or many entities. We define such relationship by supplying the referenced namespace as the type parameter to `one`/`many`:
```
Cardinality one         Cardinality many
---------------         ----------------
one[<Ref-namespace>]    many[<Ref-namespace>]
```
In the example above we saw a reference from Community to Neighborhood defined as `one[Neighborhood]`. We would for instance likely define an Order/OrderLine relationship in an Order namespace as `many[OrderLine]`.


### Bidirectional references

In [Bidirectional relationships](/code/relationships/#bidirectional) some specialized reference definitions for bidirectional graphs are explained.



## Attribute options

In Datomic, each attribute can have some extra options:


{{< bootstrap-table "table table-bordered" >}}
Option          | Indexes | Description    
:---            | :---:   | :---        
doc             | 	      | Attribute description.             
uniqueValue     | ✔       | Attribute value is unique to each entity.<br>_Attempts to insert a duplicate value for a different entity id will fail._          
uniqueIdentity  | ✔       | Attribute value is unique to each entity and "upsert" is enabled. <br>_Attempts to insert a duplicate value for a temporary entity id will cause all attributes associated with that temporary id to be merged with the entity already in the database._          
indexed         | ✔       | Generated index for this attribute. By default all attributes are set with the indexed option automatically by Molecule, so you don't need to set this.
fulltext        | ✔       | Generate eventually consistent fulltext search index for this attribute.
isComponent     | ✔       | Specifies that an attribute whose type is :db.type/ref is a component. Referenced entities become subcomponents of the entity to which the attribute is applied. <br>_When you retract an entity with :db.fn/retractEntity, all subcomponents are also retracted. When you touch an entity, all its subcomponent entities are touched recursively._
noHistory       |         | Whether past values of an attribute should not be retained.
{{< /bootstrap-table >}}



Datomic indexes the values of all attributes having an option except for the `doc` and `noHistory` options.

We saw examples of adding options by when we added `fulltext` to some of the attributes in the Seattle definition above. Molecule's schema definition DSL let's you only choose allowed options for any attribute type.





### Next

[Setup sbt...](/setup/sbt-setup)

