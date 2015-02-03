---
date: 2015-01-02T22:06:44+01:00
title: "Definition"
weight: 82
menu:
  main:
    parent: schema
---

# Define Molecule Schema

Molecule provides an easy way to model your domain in a Molecule Schema. 

A simple dsl lets you define attributes of your domain by type, cardinality and options/values with as little ceremony as possible.



### Attribute types

```scala
@InOut(3, 8)
trait SeattleDefinition {

  trait Community {
    val name         = oneString.fullTextSearch
    val url          = oneString
    val category     = manyString.fullTextSearch
    val orgtype      = oneEnum('community, 'commercial, 'nonprofit)
    val `type`       = oneEnum('email_list, 'twitter, 'facebook_page)
    val neighborhood = one[Neighborhood]
  }

  trait Neighborhood {
    val name     = oneString.fullTextSearch.uniqueIdentity
    val district = one[District]
  }

  trait District {
    val name   = oneString.fullTextSearch.uniqueIdentity
    val region = oneEnum('n, 'ne, 'e, 'se, 's, 'sw, 'w, 'nw)
  }
}
```

In the Seattle schema we define 3 namespaces with different kinds of attributes:

- `oneString`, `manyString` etc defines cardinality and type of an attribute
- `oneEnum`/`manyEnum` defines enumerated values
- `one[<ReferencedNamespace>]` defines a reference to another namespace

###  Attribute options

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
      <em>Attempts to insert a duplicate value for a temporary entity id will cause all attributes associated with that temporary id to be merged with the entity already in the database.</em></td>
  </tr>
  <tr>
    <td valign="top">indexed</td>
    <td align="center" valign="top">✔︎</td>
    <td>Generated index for this attribute.</td>
  </tr>
  <tr>
    <td valign="top">fullTextSearch</td>
    <td align="center" valign="top">✔︎</td>
    <td>Generate eventually consistent fulltext search index for this attribute.</td>
  </tr>
  <tr>
    <td valign="top">isComponent</td>
    <td align="center" valign="top">✔︎</td>
    <td>Specifies that an attribute whose type is :db.type/ref refers to a subcomponent of the entity to which the attribute is applied.<br>
    <em>When you retract an entity with :db.fn/retractEntity, all subcomponents are also retracted. When you touch an entity, all its subcomponent entities are touched recursively.</em></td>
  </tr>
  <tr>
    <td valign="top">noHistory</td>
    <td align="center" valign="top">&nbsp;</td>
    <td>Whether past values of an attribute should not be retained.</td>
  </tr>
</table>
</p>

Datomic indexes the values of all attributes having an option except for the `doc` and `noHistory` options.

As you saw, we added `fulltextSearch` and `uniqueIdentity` to some of the attributes in the Seattle definition above. Molecule's schema definition DSL let's you only choose allowed options for any attribute type.



[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[schema]: http://docs.datomic.com/schema.htm

[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial