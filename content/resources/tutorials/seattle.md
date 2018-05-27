---
date: 2015-01-02T22:06:44+01:00
title: "Seattle"
weight: 10
menu:
  main:
    parent: tutorials
---

# Seattle tutorial - with molecules {#contents}

- [A first query](#1)
- [Querying _for_ an attribute's value](#2)
- [Querying _by_ attribute values](#3)
- [Querying across references](#4)
- [Parameterizing queries - input molecules](#5)
- [Invoking functions in queries](#6)
- [Querying with fulltext search](#7)
- [Querying with rules - logical OR](#8)
- [Working with time](#9)
- [Insert data](#10)
- [Update and/or delete data](#11)

_Credits: This tutorial is based on the original 
[Datomic Seattle tutorial][seattle] (there is also a [new tutorial](http://docs.datomic.com/tutorial.html)) 
and some text passages have been quoted as-is or been slightly modified to describe 
how Molecule works._



## [☞](#contents) Introduction {#0}

After [setting up the database][setup] and [populating it with data][populate] we can 
start making queries. We make queries by building "molecules" which are 
chains of attributes put together with the builder pattern. We can imagine 
this as a 3-dimensional data structure of atoms bound together in various 
patterns to build molecules...

### Molecule builder pattern

The first thing you do with Molecule is to define your domain namespaces and attributes in a 
trait that defines namespaces with attributes for your domain:

```scala
trait Community {
  val name = oneString.fulltextSearch
  val url  = oneString
  ...
}
```

The `name` field defines an attribute of type String with cardinality one. Adding the
`fulltextSearch` option will tell Datomic that we want to be able to make fulltext
searches on the values of this attribute.

After defining the schema like this, we run `sbt compile` and Molecule will generate some
boilerplate traits that allow us to build molecules of our attributes:

```scala
val nameUrls = m(Community.name.url).get
```

Since the `m` method is implicit we can generally just write

```scala
val nameUrls = Community.name.url.get
```

If you look at the generated namespace code you'll see that it is a 
little more complex behind the scenes. That's because we want our IDE 
to be able to infer the type of each attribute. If we for instance had 
an `age` attribute of type `Int` we could infer the return types of 
calling the `get` method on a molecule. That would return
a List of name/age tuples of type `Iterable[(String, Int)]`:

```scala
val nameAges: Iterable[(String, Int)] = Community.name.age.get
```

A feature of Molecule is to omit the values of an attribute from the result set
by adding an underscore to the attribute name:

```scala
val names: Iterable[String] = Community.name.age_.get
```

This is handy if we want to query for entities that we want to be sure have an age and 
where we at the same time don't need the age returned.


Now let's follow along the 
[Datomic Seattle tutorial][seattle] and 
see how Molecule can perform the same queries.



## [☝︎](#contents) A first query {#1}

To find communities we can make a `communities` molecule looking for entities with Community name:

```scala
val communities = m(Community.name)
```

With this molecule at hand we can get the community names:
Or we can ask for the size of our returned data set:

```scala
communities.get === // List of community names...
```

or we could check how many communities we have

```scala
communities.get.size === 150
```
If we want the entity ids of our communities we can add the generic attribute `e` to our molecule. 
We might not be interested in the names but we want to make sure that we find entities having a name,
so we add the `name` attribute with an underscore (to omit it from the result set):

```scala
Community.e.name_.get(3) === List(17592186045518L, 17592186045516L, 17592186045514L)
```



## [☝︎](#contents) Getting an entity's attribute values

A way to get additional attribute values once we have an entity id is to `touch` it:

```scala
val communityId = Community.e.name_.get.head

// Use the community id to touch all the entity's attribute values
communityId.touch === Map(
  ":community/type" -> ":community.type/website",
  ":community/url" -> "http://www.greenlakecommunitycouncil.org/",
  ":community/category" -> List("community council"),
  ":community/orgtype" -> ":community.orgtype/community",
  ":db/id" -> 17592186045665L,
  ":community/name" -> "Greenlake Community Council",
  ":community/neighborhood" -> Map(
    ":db/id" -> 17592186045666L,
    ":neighborhood/district" -> Map(
      ":db/id" -> 17592186045667L,
      ":district/name" -> "Northwest",
      ":district/region" -> ":district.region/sw"),
    ":neighborhood/name" -> "Green Lake"))
```

We can also retrive attribute values one by one by simply applying an attribute name to the entity id:

```scala
communityId(":community/name") === Some("Greenlake Community Council")
communityId(":community/url") === Some("http://www.greenlakecommunitycouncil.org/")
communityId(":community/category") === Some(Set("community council"))
communityId(":community/emptyOrBogusAttribute") === None
```



## [☝︎](#contents) Querying _for_ an attribute's value {#2}

After defining a molecule like `Community.name` we can call the `get` 
method on it to retrieve values that matches it. When there's only one 
attribute defined in the molecule we'll get a list of this attribute's 
value back.

```scala
Community.name.get(3) === List(
  "KOMO Communities - Ballard",
  "Ballard Blog",
  "Ballard Historical Society")
```

If our molecule defines two or more attributes we'll get tuples of 
values back.

```scala
Community.name.url.get(3) === List(
  ("Broadview Community Council", "http://groups.google.com/group/broadview-community-council"),
  ("KOMO Communities - Wallingford", "http://wallingford.komonews.com"),
  ("Aurora Seattle", "http://www.auroraseattle.com/"))
```


## [☝︎](#contents) Querying _by_ attribute values {#3}

When applying a value to an attribute we narrow the selection of 
entities that will match our molecule data structure. Let's find communities
of type "twitter":

```scala
Community.name.`type`("twitter").get(3) === List(
  ("Columbia Citizens", "twitter"),
  ("Discover SLU", "twitter"),
  ("Fremont Universe", "twitter"))
```
(We use the back-ticks to avoid having Scala to think of `type` 
as a Scala keyword)

Since the `type` will always be "twitter" we could omit it from the result set 
by adding an underscore to the `type` attribute (and we don't need the back-ticks anymore).

```scala
Community.name.type_("twitter").get(3) === List(
  "Magnolia Voice", "Columbia Citizens", "Discover SLU")
```
Notice that we get some different communities. We are not guaranteed a specific order 
of returned values and the first 3 values can therefore vary as we see here even though 
the molecules/queries are similar.


In most of our examples we supply static data like "twitter" but even 
though our molecules are created at compile time we can even supply 
data as variables like we would do with user input from forms etc. 
So we could as well write the following and get the same result.

```scala
val tw = "twitter"
Community.name.type_(tw).get(3) === List(
  "Magnolia Voice", "Columbia Citizens", "Discover SLU")
```

Retrieving values of many-attributes like `category` gives us sets 
of values back

```scala
Community.name_("belltown").category.get.head === Set("events", "news")
```
Since we often want a single result back, Molecule supplies a `one` convenience method that calls `get.head`.

We can apply multiple values to many-attributes like `category` and 
it will match entities having any of those values (OR-semantics).

```scala
Community.name.category_("news", "arts").get(3) === List(
  "Beach Drive Blog",
  "KOMO Communities - Ballard",
  "Ballard Blog")
```



## [☝︎](#contents) Querying across references {#4}

The sample data model includes three main entity types communities, 
neighborhoods and districts that are related to each other with references. 
Molecule lets you traverse those references by going from one namespace 
to the next. Let's find communities in the noth-eastern region:

```scala
Community.name.Neighborhood.District.region_("ne").get(3) === List(
  "Maple Leaf Community Council",
  "Hawthorne Hills Community Website",
  "KOMO Communities - View Ridge")
```
Or comunity names and their region:

```scala
Community.name.Neighborhood.District.region.get(3) === List(
  ("KOMO Communities - North Seattle","n"),
  ("Morgan Junction Community Association","sw"),
  ("Friends of Seward Park","se"))
```



## [☝︎](#contents) Parameterizing queries - input molecules {#5}

When you apply values to molecules, the resulting query 
string is cached by Datomic. If you keep varying the string content, 
the cache is not effective. To take advantage of query caching it is 
recommended to make parameterized queries that can be cached once and 
used with varying input parameters.

### Single input value for an attribute

Instead of applying the constant value "twitter" to a molecule 
`Community.type("twitter")` we can use the `?` input placeholder 
in an "input molecule" telling us that it waits for an input value.

```scala
val communitiesOfType = m(Community.name.type_(?))
```

When can then apply different input values to our input molecule:

```scala
val twitterCommunities = communitiesOfType("twitter")
val facebookCommunities = communitiesOfType("facebook_page")
```
Those two molecules re-use the same cached query and just apply 
different input values. Now we can more efficiently get out results. 

```scala
twitterCommunities.get(3) === List(
  "Magnolia Voice", "Columbia Citizens", "Discover SLU")
  
facebookCommunities.get(3) === List(
  "Magnolia Voice", "Columbia Citizens", "Discover SLU")
```

If we omit the underscore we can get the type too

```scala
val communitiesWithType = m(Community.name.`type`(?))

communitiesWithType("twitter").get(3) === List(
  ("Discover SLU", "twitter"),
  ("Fremont Universe", "twitter"),
  ("Columbia Citizens", "twitter"))
```

### Multiple input values for an attribute - logical OR

Find communities of type "facebook_page" OR "twitter":

```scala
communitiesWithType("facebook_page" or "twitter").get(3) === List(
  ("Eastlake Community Council", "facebook_page"),
  ("Discover SLU", "twitter"),
  ("MyWallingford", "facebook_page"))
```
Alternative syntaxes where comma-separations act as logical OR:

```scala
communitiesWithType("facebook_page", "twitter")
communitiesWithType(Seq("facebook_page", "twitter"))
```

### Tuple of input values for multiple attributes - logical AND

In addition to passing multiple values for a single attribute, you 
can pass a tuple of values for multiple attributes ensuring that both 
values are present.

```scala
val typeAndOrgtype = m(Community.name.type_(?).orgtype_(?))
```
With this input molecule we can find communities that are of `type` 
"email_list" AND `orgtype` "community".

```scala
typeAndOrgtype("email_list" and "community").get(3) === List(
  "Ballard Moms",
  "Admiral Neighborhood Association",
  "15th Ave Community")
```
The order of arguments in the logical AND expression will correspond 
to the order of the input placeholders in the input molecule so that 
"email_list" corresponds to `type_(?)` and "community" corresponds to 
`community_(?)`. 

Arguments in expressions are also type-checked against the expected 
types of the corresponding attributes. Our IDE would infer that the 
`orgtype` attribute doesn't expect an `Int` as the second argument if we 
were to pass the expression "email_list and 42". This helps us avoid 
populating our database with unexpected data.

We can express logical AND expressions with a list of arguments too:

```scala
// AND-semantics given an input molecule expecting 2 inputs!
typeAndOrgtype("email_list", "community")
```

Or we can pass a list of arguments. IMPORTANT: note how the semantics 
of a list of arguments change compared to the OR semantics that we 
saw with the single-input molecule above that had OR-semantics. When 
we have multiple inputs the semantics change to AND-semantics!

```scala
// AND-semantics given an input molecule expecting 2 inputs!
typeAndOrgtype(Seq(("email_list", "community")))
```

### Multiple tuples of input values for multiple attributes - logical AND/OR

We can also ask for alternative tuples of data structures. Since the 
input values can then vary, we could ask our molecule to return the 
input values too.

```scala
val typeAndOrgtype2 = m(Community.name.`type`(?).orgtype(?))
```
Now let's ask for email-list communities OR commercial website 
communities. Note how this combines logical AND and OR.

```scala
typeAndOrgtype2(
  ("email_list" and "community") or 
  ("website" and "commercial")
).get(5) === List(
  ("Fremont Arts Council", "email_list", "community"),
  ("Greenwood Community Council Announcements", "email_list", "community"),
  ("Broadview Community Council", "email_list", "community"),
  ("Alki News", "email_list", "community"),
  ("Beacon Hill Burglaries", "email_list", "community"))
```
As usual we can use alternative syntaxes as well. Here we group the 
AND expression arguments as tuple values. Comma-separations between 
the tuples act as logical OR.

```scala
// ((a AND b) OR (c AND d))
typeAndOrgtype2(("email_list", "community"), ("website", "commercial"))
typeAndOrgtype2(Seq(("email_list", "community"), ("website", "commercial")))
```



## [☝︎](#contents) Invoking functions in queries {#6}

Datomic lets you invoke functions in queries. Molecule use this to 
apply comparison operations on attribute values. Here we can for 
instance find communities whose `name` come before "C" in 
alphabetical order.

```scala
m(Community.name < "C").get(3) === List(
  "Ballard Blog", "Beach Drive Blog", "Beacon Hill Blog")
```
Note how we use the `m` method here to allow the postfix notation 
(spaces around `<`). Alternatively you can call the `<` method 
explicitly if you prefer this syntax:

```scala
Community.name.<("C").get(3) === List(
  "Ballard Blog", "Beach Drive Blog", "Beacon Hill Blog")
```
We can also parameterize the molecule.

```scala
val communitiesBefore = m(Community.name < ?)

communitiesBefore("C").get(3) === List(
  "Ballard Blog", "Beach Drive Blog", "Beacon Hill Blog")
  
communitiesBefore("A").get(3) === List("15th Ave Community")
```



## [☝︎](#contents) Querying with fulltext search {#7}

Datomic supports fulltext searching. When you define an attribute 
of string value, you can indicate whether it should be indexed for 
fulltext search. For instance Community `name` and `category` have 
the fulltextSearch option defined in the Seattle schema. Let's find 
communities with "Wallingford" in the name.

```scala
(Community.name contains "Wallingford" take 3) === List(
  "KOMO Communities - Wallingford")
```
And we can parameterize fulltext searches too:

```scala
val communitiesWith = m(Community.name contains ?)

(communitiesWith("Wallingford") take 3) === List(
  "KOMO Communities - Wallingford")
```

### Fulltext search on many-cardinality attributes

The `category` attribute can have several values so when we do a 
fulltext search on its values we'll get back a set of its values 
that match our seed. We can also combine fulltext search with other 
constraints. Here we look for website communities with a `category` 
containing the word "food":

```scala
m(Community.name.type_("website").category contains "food").get(3) === List(
  ("Community Harvest of Southwest Seattle", Set("sustainable food")),
  ("InBallard", Set("food")))
)
```
And parameterized:

```scala
val typeAndCategory = m(Community.name.type_(?).category contains ?)

typeAndCategory("website", Set("food")).get(3) === List(
  ("Community Harvest of Southwest Seattle", Set("sustainable food")),
  ("InBallard", Set("food")))
)
```
Note how the values of the `category` attribute are now returned 
since they can vary across the result set contrary to the `type` 
attribute which is not since it will have the same value for all 
matches.



## [☝︎](#contents) Querying with rules - logical OR {#8}

Datomic rules are named groups of Datomic clauses that can be 
plugged into Datomic queries. As a Molecule user you don't need to
know about rules since Molecule automatically translates your logic
to Datomic rules. 

We can for instance find social media communities with a 
logical OR expresion:

```scala
Community.name.type_("twitter" or "facebook_page").get(3) === List(
  "Magnolia Voice", "Columbia Citizens", "Discover SLU")
```
... or find communities in the NE or SW regions.

```scala
Community.name.Neighborhood.District.region_("ne" or "sw").get(3) === List(
  "Beach Drive Blog", 
  "KOMO Communities - Green Lake", 
  "Delridge Produce Cooperative")
```
And we can combine them to find social-media communities in 
southern regions.

```scala
val southernSocialMedia = List(
  "Columbia Citizens",
  "Fauntleroy Community Association",
  "MyWallingford",
  "Blogging Georgetown")

Community.name.type_("twitter" or "facebook_page")
  .Neighborhood
  .District.region_("sw" or "s" or "se").get === southernSocialMedia
```

Let's parameterized the same query:

```scala
val typeAndRegion = m(Community.name.type_(?).Neighborhood.District.region_(?))

typeAndRegion(
  ("twitter" or "facebook_page") and 
  ("sw" or "s" or "se")
).get === southernSocialMedia

// or
typeAndRegion(
  Seq("twitter", "facebook_page"), 
  Seq("sw", "s", "se")
).get === southernSocialMedia
```
Note how this syntax for the ((a OR b) AND (c OR d)) expression 
is different from the syntax we had earlier in the section 
"Multiple tuples of input values for multiple attributes" where 
we had a ((a AND b) OR (c AND d)) expression.



## [☝︎](#contents) Working with time {#9}

All of the query results shown in the previous two sections were 
based on the initial seed data we loaded into our database. The 
data hasn't changed since then. In this section we'll load some 
more data, and explain how to work with database values from 
different moments in time.

### Time is built in
One of the key concepts in Datomic is that new facts don't replace 
old facts. Instead, by default, the system keeps track of all the 
facts, forever. This makes it possible to look at the database as 
it was at a certain point in time, or at the changes since a certain 
point in time.

When you submit a transaction to a database, Datomic keeps track 
of the entities, attributes and values you add or retract. It also 
keeps track of the transaction itself. Transactions are entities 
in their own right, and you can write queries to find them. 
The system associates one attribute with each transaction entity, 
Db.txInstant, which records the time the transaction was processed.

Molecule has a `Db` namespace with a `txInstant` attribute that 
we can use to query for transactions instants (represented as 
java.util.Date instances) that has been created. We've only 
executed two transactions, but the earlier system executed a 
few as part of its bootstrapping process. We know, though, that 
our two are the most recent. The code below uses a `Db.txInstant` 
molecule to retrieve transaction times, sort them into reverse 
chronological order, and store the most recent two as dataTxDate 
and schemaTxDate, when we added our data and our schema, respectively.

```scala
val txDates = Db.txInstant.get.toSeq.sorted.reverse
val dataTxDate = txDates(0)
val schemaTxDate = txDates(1)
```

### Revisiting the past - `getAsOf(PastDate)`
Once we have the relevant transaction times, we can look at the 
database as of that point in time. To do this, we retrieve the 
current database value by calling the molecule method `getAsOf`, 
passing in the Date we're interested in. The `getAsOf` method 
returns a new molecule based on the database value that is 
"rewound" back to the requested date.

An example will help make this clear. The code below gets the 
value of the database as of our schema transaction. Then it 
runs our very first query, which retrieves entities representing 
communities, and prints the size of the results. Because we're 
using a database value from before we ran the transaction to 
load seed data, the size is 0.

```scala
// Take all Community entities
val communities = m(Community.e.name_)
    
communities.getAsOf(schemaTxDate).size === 0
```
If we do the same thing using the date of our seed data 
transaction, the query returns 150 results, because as of 
that moment, the seed data is there.

```scala
communities.getAsOf(dataTxDate).size === 150
```

### Changes since a date - `getSince(compareDate)`

The `getAsOf` method allows us to look at a database value 
containing data changes up to a specific point in time. 
There is another method `getSince` that allows us to look at 
a database value containing data changes _since_ a specific 
point in time.

The code below gets the value of the database since our 
schema transaction and counts the number of communities. 
Because we're using a database value containing changes 
made since we ran the transaction to load our schema - 
including the changes made when we loaded our seed data - 
the size is 150.

```scala
communities.getSince(schemaTxDate).size === 150
```
If we do the same thing using the date of our seed data 
transaction, the query returns 0 results, because we haven't 
added any communities since that time.

```scala
communities.getSince(dataTxDate).size === 0
```
While we passed specific transaction dates to `getAsOf` 
and `getSince`, you can pass any date. The system find the 
closest relevant transaction and use that as the basis for 
filtering.

Keeping track of data over time is a very powerful feature. 
However, there may be some data you don't want to keep old 
versions of. You can control whether old versions are kept 
on a per-attribute basis by adding `noHistory` to your 
attribute definition when you create your schema. If you 
choose not to keep history for a given attribute and you 
look at a database as of a time before the most recent change 
to a given entity's value for that attribute, you will not 
find any value for it.


### Imagining the future - `getWith(TestTxData)`

Revisiting the past is a very powerful feature. It's also 
possible to imagine the future. The `getAsOf` and `getSince` 
methods work by removing data from the current database value. 
You can also _add_ data to a database value, using the 
Molecule method `getWith`. 
The result is a database value that's been modified without 
submitting a transaction and changing the data stored 
in the system. The modified database value can be used to 
execute queries, allowing you to perform "what if" 
calculations before committing to data changes. 

When a `getWith(TestTxData)` database object goes out of scope 
it is simply garbage collected. So we don't need to do any tear
down of some state as is common with normal database mockups.

We can explore this feature using a second seed data file 
provided with the sample application, 
"samples/seattle/seattle-data1.edn". The code below reads 
it into a list.

```scala
val data_rdr2 = new FileReader("examples/resources/seattle/seattle-data1a.dtm")
val newDataTx = Util.readAll(data_rdr2).get(0).asInstanceOf[java.util.List[Object]]
```
Once we have this new data transaction, we can build a 
database value that includes it. To do that, we simply 
get the current database value (or one as of or since a 
point in time) and call `getWith`, passing in the 
transaction data. `getWith` returns a molecule based on 
the new value of the database after the new data is added. 
If we execute our community counting query against it, 
we get 258 results.

```scala
// test db
communities.getWith(newDataTx).get.size === 258
```

The actual data hasn't changed yet, so if we query the 
current database value, we still get 150 results. We won't 
see a change in the current database value until we submit 
the new transaction. After that, querying the current 
database value returns 258 results. Finally, if we get 
another database value containing data since our first 
seed data transaction ran, and query for communities we 
get 108 results, the number added by new data transaction.

```scala
// existing db
communities.get.size === 150

// transact
conn.transact(newDataTx)

// updated db
communities.get.size === 258

// number of new transactions
communities.getSince(dataTxDate).size === 108
```



## [☝︎](#contents) Insert data {#10}

You can add data in two ways with Molecule:

1. Build a molecule with data and insert
2. Use a molecule template to insert matching data

### "Data-molecule" with values
To insert a single data structure you can populate a 
molecule with values and then `add` it:

```scala
Community
  .name("AAA")
  .url("myUrl")
  .`type`("twitter")
  .orgtype("personal")
  .category("my", "favorites")
  .Neighborhood.name("myNeighborhood")
  .District.name("myDistrict").region("nw").save.eids === List(
    17592186045891L, 17592186045892L, 17592186045893L)
```
Note how we can add values for referenced namespaces and multiple values for 
cardinality-many attributes like `category` - all in one go!

Apart from the new Community entity two more entities are also added. Since
neither "myNeighborhood" nor "myDistrict" exist they are created to so that
we can reference them.

In Datomic there is no requirement that we add a "complete" 
set of namespace attributes to create an entity. For instance, we could add 
a community only with `Community.name("My community").add`. 

### "Insert-molecule" + matching values

A more efficient way to add larger sets of data is to define an
"Insert-Molecule" that models the data structure we 
want to insert into the database. Note how we call the `insert` method
to define it as an Input-Molecule:

```scala
val insertCommunity = m(
  Community.name.url.`type`.orgtype.category
    .Neighborhood.name
    .District.name.region
) insert
```
We can then create a new Community by applying a matching set of attribute values:

```scala
insertCommunity(
  "BBB", "url B", "twitter", "personal", Set("some", "cat B"), 
    "neighborhood B", 
    "district B", "s"
).eids === List(17592186045895L, 17592186045896L, 17592186045897L)
```
As before, three entities are created here since we reference a new Neighborhood and District.

All values are type checked against the expected type of each attribute!


### Insert-Molecule + multiple data tuples

With our insert-molecule at hand we can insert large numbers 
of data tuples. As an example we can insert 3 communities and 
referenced neighborhoods/district/regions in one go:

```scala
val newCommunitiesData = List(
  ("DDD Blogging Georgetown", "http://www.blogginggeorgetown.com/", 
    "blog", "commercial", Set("DD cat 1", "DD cat 2"), 
    "DD Georgetown", "Greater Duwamish", "s"),
  ("DDD Interbay District Blog", "http://interbayneighborhood.neighborlogs.com/", 
    "blog", "community", Set("DD cat 3"), 
    "DD Interbay", "Magnolia/Queen Anne", "w"),
  ("DDD KOMO Communities - West Seattle", "http://westseattle.komonews.com", 
    "blog", "commercial", Set("DD cat 4"), 
    "DD West Seattle", "Southwest", "sw")
)

// Insert 3 new communities with 3 new neighborhoods
insertCommunity(newCommunitiesData) === List(
  17592186045909L, 17592186045910L, 17592186045911L,
  17592186045912L, 17592186045913L, 17592186045914L,
  17592186045915L, 17592186045916L, 17592186045917L)
```
This approach gives us a clean way of populating a database 
where we can supply raw data from any source easily as long 
as we can format it as a list of tuples of values each matching 
our template molecule. 

We use an insert-molecule also when we initially [populate 
the Seattle database](https://github.com/scalamolecule/blob/master/examples/src/test/scala/examples/seattle/SeattleSpec.scala#L43). 

### Optional attribute values

We might have a data set with some optional attribute values. We can append a `$` to
such attribute names to tell Molecule that this is an optional value:

```scala
val insertCommunity = m(
  Community.name.url.`type`.orgtype$.category
    .Neighborhood.name
    .District.name.region
) insert
```

If for instance some row has no orgtype we can use `None` (and likewise `Some(<value>)`):

```scala
("community4", "url2", "blog", None, Set("cat3", "cat1"), "NbhName4", "DistName4", "ne")
```

In an sql table we would "insert a null value" for such column. 
But with Molecule/Datomic we just simply don't assert any orgtype 
value for that entity at all! In other words: there is no orgtype 
fact to be asserted.

### Type safety

In this example we have only inserted text strings. But all input 
is type checked against the selectedattributes of the molecule which 
makes the insert operation type safe. We even infer the expected type
so that our IDE will bark if it finds for instance an Integer 
somewhere in our input data:

```scala
("community2", "url2", "type2", 42, Set("cat3", "cat1"), "NbhName2", "DistName2", "DistReg2")
```
A data set having the value `42` as a value for the `orgtype` 
attribute won't compile and our IDE will infer that we have an invalid data set.



## [☝︎](#contents) Update and/or delete data {#11}

To update data with Molecule, we first need the id of the entity 
that we want to update.

```scala
val belltown = Community.e.name_("belltown").get.head
```
Then we can "replace" some attributes

```scala
Community(belltown).name("belltown 2").url("url 2").update
```

What really happens is not a mutation of data since no 
data is ever deleted or over-written in Datomic. Instead a 
_new fact for the attribute is asserted_. The new fact will 
turn up when queried for. But if we go back in time we can see 
the previous value at that point in time - many updates could 
have been performed over time, and all previous values are stored.

### Updating cardinality-many attributes

When updating cardinality-many attributes we need to tell which 
of the values we want to update:

```scala
Community(belltown).category("news" -> "Cool news").update
```
This syntax causes Molecule to first retract the old value "news" 
and then assert/add the new value "Cool news". Note that if the 
before-value doesn't exist the new value will still be inserted, 
so you might be sure what the current value is by querying for it first.

We can even update several values of a cardinality-many attribute in one go:

```scala
Community(belltown).category(
  "Cool news" -> "Super cool news",
  "events" -> "Super cool events").update
```

### Adding/removing values of cardinality-many attributes

If we want to add or remove values of a cardinality-many attribute 
we can use the following mehtods:

```scala
// add
Community(belltown).category.add("extra category").update

// remove
Community(belltown).category.remove("Super cool events").update
```

### Delete/retract values

When you update a molecule you can apply an empty value `apply()` 
or simply `()` after an attribute name to delete/retract the 
attributes value(s). We can mix updates and retractions:

```scala
Community(belltown).name("belltown 3").url().category().update
```
`name` gets the new value "belltown 3" and both the `url` and 
`category` attributes have their values retracted.

There are a couple of important things to know about retracting data. 
The first is that we must specify the value of the attribute being 
retracted. When applying the empty value, Molecule therefore first 
looks up the current value in order to be able to retract it.

The other thing to know is that, because we can access database 
values as they existed at specific points in time, we can retrieve 
retracted data by looking in the past. In other words, the data 
isn't really gone. If we want data to really be gone after we 
retract it, we have to disable history for the specific attribute, 
as described in [Database setup][setup].



[seattle]: https://web.archive.org/web/20161007085359/http://docs.datomic.com/tutorial.html
[setup]: /manual/getting-started/
[populate]: /manual/insert/

