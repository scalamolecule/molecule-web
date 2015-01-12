---
date: 2015-01-02T22:06:44+01:00
title: "Schema definition"
weight: 83
menu:
  main:
    parent: manual
---


# Defining a schema

In Molecule you simply define namespaces as Scala vanilla traits having 
fields that model each attribute.

Defining the schema of the 
[Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html) 
for instance looks like this:

```scala
@InOut(3, 8)
trait Community {
  val name         = oneString.fullTextSearch
  val url          = oneString.fullTextSearch
  val category     = manyString.fullTextSearch
  val orgtype      = oneEnum('community, 'commercial, 'nonprofit, 'personal)
  val `type`       = oneEnum('email_list, 'twitter, 'facebook_page, 'blog, 'website, 'wiki, 'myspace, 'ning)
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
```

### In/Out arities

We annotate the first namespace in the schema with `@InOut(x, y)`. This is to tell Molecule
how many inputs and outputs we expect molecules to be able to accept. 

An input molecule like `Community.name(?).url(?)` for instance awaits 2 inputs. For now the 
maximum is 3. Given that input values can be expressions like `name("John" or "Lisa")` it seems 
unlikely that we will need to receive input for much more than 3 attributes at a time.

Outputs are the number of attributes we can build a molecule of. `Community.name.url.Neighborhood.name` 
for instance has 3 attributes (in 2 namespaces). We need to be able to return tuples of values from 
molecules so we can't exceed Scala's arity limit of 22 for tuples.


### Attribute types

In the Seattle schema we defined 3 namespaces with different kinds of attributes:

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


## Transacting a schema

Having defined our domain in namespaces of attributes we then run `sbt compile`. 
This makes Molecule automatically transform our definitions to transactional 
data in a format that Datomic needs in a schema file. Our two first attributes
`name` and `url` for instance transforms to the following list of maps of key/values:

```scala
object SeattleSchema extends Schema {

  lazy val tx = Util.list(

    // Community ----------------------------------------------------------

    Util.map(":db/id"                , Peer.tempid(":db.part/db"),
             ":db/ident"             , ":community/name",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db.install/_attribute", ":db.part/db"),

    Util.map(":db/id"                , Peer.tempid(":db.part/db"),
             ":db/ident"             , ":community/url",
             ":db/valueType"         , ":db.type/string",
             ":db/cardinality"       , ":db.cardinality/one",
             ":db/fulltext"          , true.asInstanceOf[Object],
             ":db.install/_attribute", ":db.part/db"),
           
 // etc...
```
Note how each attribute name is prefixed with the namespace name (":community/name"). 

Now we can easily transact the schema by simply writing:

```scala
conn.transact(SeattleSchema.tx).get()
```


[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[schema]: http://docs.datomic.com/schema.htm

[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial