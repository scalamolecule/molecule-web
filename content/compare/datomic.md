---
date: 2014-05-14T02:13:50Z
title: "Datomic"
weight: 10
menu:
  main:
    parent: compare
---

# Compare with Datomic/Datalog

Even though Molecule is adapting to Datomic we'll have a look the other way around to see how Datomic compares to Molecule. 

[Follow along in the code](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleQueryTests.scala) from which we will pick a few examples based on the Seattle tutorial examples.

### Querying _for_ attribute values

The most basic query is to ask for entities with some attribute values:

```
// Datalog
[:find  ?b ?c (distinct ?d)
 :where [?a :community/name ?b]
        [?a :community/url ?c]
        [?a :community/category ?d]]
```
In Molecule we simply use the namespace name and add the attribute names:
```scala
// Molecule
Community.name.url.category
```
      
Datalog has a `:find` and `:where` section similar to `select` and `where` in the SQL world. The `:find` section defines which values to return and the where section defines one or more clauses filtering the result set. 

In this case we asked for the values of variable `?b`, `?c` and `?d` each one bound in its where clause. With molecule we use the three attribute names all associated to the `Community` namespace.


### Querying _by_ attribute values

Let's query by an enumerated value for the `type` attribute:

```
[:find  ?b
 :where [?a :community/name ?b]
        [?a :community/type ":community.type/twitter"]]
```

```scala
Community.name.type_("twitter")
```
      
Note how we add an underscore to the `type` attribute to tell Molecule that we want to omit returning this value in the result set (since it will have the value "twitter" for all returned entities). 

Since the `type` attribute is defined as en enumeration Molecule checks the "twitter" value at _compile time_ against the defined enumeration values that we have definied in our schema for the `Community` namespace to ensure that "twitter" is one of the enums. If it is not, our molecule won't compile and we'll get an error showing the available enum values.
 
For a many-cardinality attribute like `category` Datalog applies logical OR with Datalog rules:
 
```
[:find  ?b
 :in    $ %
 :where [?a :community/name ?b]
        (rule1 ?a)]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[(rule1 ?a) [?a :community/category "news"]]
     [(rule1 ?a) [?a :community/category "arts"]]]
)
```
In Molecule we can apply the two values either separated with `or` or commas:

```scala
Community.name.category_("news" or "arts")

// Same as
Community.name.category_("news", "arts")
```

### Querying across references

```
[:find  ?b ?e2
 :where [?a :community/name ?b]
        [?a :community/neighborhood ?c]
        [?c :neighborhood/district ?d]
        [?d :district/region ?e]
        [?e :db/ident ?e1]
        [(.getName ^clojure.lang.Keyword ?e1) ?e2]]
```

```scala
Community.name.Neighborhood.District.region
```


### Parameterizing queries

Community input molecule awaiting some type value

```
[:find  ?b ?c2
 :in    $ ?c
 :where [?a :community/name ?b]
        [?a :community/type ?c]
        [?c :db/ident ?c1]
        [(.getName ^clojure.lang.Keyword ?c1) ?c2]]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 :community.type/twitter
)
```

```scala
val communitiesOfType  = m(Community.name.type(?))
val twitterCommunities = communitiesOfType("twitter")
```

Multiple input values for one attribute - logical OR

```
[:find  ?b ?c2
 :in    $ ?c
 :where [?a :community/name ?b]
        [?a :community/type ?c]
        [?c :db/ident ?c1]
        [(.getName ^clojure.lang.Keyword ?c1) ?c2]]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 :community.type/twitter
)
```

```scala
m(Community.name.`type`(?)).apply("facebook_page" or "twitter")
```


Single tuple of input values for two attributes - logical AND

```
[:find  ?b
 :in    $ [[ ?c ?d ]]
 :where [?a :community/name ?b]
        [?a :community/type ?c]
        [?a :community/orgtype ?d]]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[:community.type/email_list, :community.orgtype/community]]
)
```

```scala
m(Community.name.type_(?).orgtype_(?))("email_list" and "community")
```


Multiple tuple of input values for two attributes - logical AND

```
[:find  ?b ?c2 ?d2
 :in    $ [[ ?c ?d ]]
 :where [?a :community/name ?b]
        [?a :community/type ?c]
        [?c :db/ident ?c1]
        [(.getName ^clojure.lang.Keyword ?c1) ?c2]
        [?a :community/orgtype ?d]
        [?d :db/ident ?d1]
        [(.getName ^clojure.lang.Keyword ?d1) ?d2]]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[:community.type/email_list, :community.orgtype/community], 
     [:community.type/website, :community.orgtype/commercial]]
)
```

```scala
m(Community.name.`type`(?).orgtype(?))
.apply(Seq(("email_list", "community"), ("website", "commercial")))
```


### Invoking functions in queries

```
[:find  ?b
 :where [?a :community/name ?b]
        [(.compareTo ^String ?b "C") ?b2]
        [(< ?b2 0)]]
```

```scala
Community.name.<("C")
```



### Fulltext search

```
[:find  ?b
 :where [(fulltext $ :community/name "Wallingford") [[ ?a ?b ]]]]
```

```scala
Community.name.contains("Wallingford")
```

Fulltext search on many-attribute (`category`)

```
[:find  ?b (distinct ?d)
 :where [?a :community/name ?b]
        [?a :community/type ":community.type/website"]
        [(fulltext $ :community/category "food") [[ ?a ?d ]]]]
```

```scala
Community.name.type_("website").category.contains("food")
```




### Querying with rules (logical OR)

Social media communities
```
[:find  ?b
 :in    $ %
 :where [?a :community/name ?b]
        (rule1 ?a)]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[(rule1 ?a) [?a :community/type ":community.type/twitter"]]
     [(rule1 ?a) [?a :community/type ":community.type/facebook_page"]]]
)
```

```scala
Community.name.type_("twitter" or "facebook_page")
```

Social media communities in southern regions

```
[:find  ?b
 :in    $ %
 :where [?a :community/name ?b]
        (rule1 ?a)
        [?a :community/neighborhood ?d]
        [?d :neighborhood/district ?e]
        (rule2 ?e)]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[(rule1 ?a) [?a :community/type ":community.type/twitter"]]
     [(rule1 ?a) [?a :community/type ":community.type/facebook_page"]]
     [(rule2 ?e) [?e :district/region ":district.region/sw"]]
     [(rule2 ?e) [?e :district/region ":district.region/s"]]
     [(rule2 ?e) [?e :district/region ":district.region/se"]]]
)
```

```scala
Community.name.type_("twitter" or "facebook_page")
  .Neighborhood.District.region_("sw" or "s" or "se")
```

Parameterized

```
[:find  ?b
 :in    $ %
 :where [?a :community/name ?b]
        [?a :community/type ?c]
        [?a :community/neighborhood ?d]
        [?d :neighborhood/district ?e]
        [?e :district/region ?f]
        (rule1 ?a)
        (rule2 ?e)]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[(rule1 ?a) [?a :community/type ":community.type/twitter"]]
     [(rule1 ?a) [?a :community/type ":community.type/facebook_page"]]
     [(rule2 ?e) [?e :district/region ":district.region/sw"]]
     [(rule2 ?e) [?e :district/region ":district.region/s"]]
     [(rule2 ?e) [?e :district/region ":district.region/se"]]]
)
```

```scala
m(Community.name.type_(?).Neighborhood.District.region_(?))
  .apply(("twitter" or "facebook_page") and ("sw" or "s" or "se")
```



### Working with time

```
[:find  ?b
 :where [?a :db/txInstant ?b]]
```

```scala
Db.txInstant
```


### Inserting data

```
List(
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/name        ,   AAA                             )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/url         ,   myUrl                           )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/type        ,   :community.type/twitter         )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/orgtype     ,   :community.orgtype/personal     )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/category    ,   my                              )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/category    ,   favorites                       )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/neighborhood,   #db/id[:db.part/user -1000002]  )
  List(  :db/add,   #db/id[:db.part/user -1000002],   :neighborhood/name     ,   myNeighborhood                  )
  List(  :db/add,   #db/id[:db.part/user -1000002],   :neighborhood/district ,   #db/id[:db.part/user -1000003]  )
  List(  :db/add,   #db/id[:db.part/user -1000003],   :district/name         ,   myDistrict                      )
  List(  :db/add,   #db/id[:db.part/user -1000003],   :district/region       ,   :district.region/nw             )
)
```

```scala
Community
  .name("AAA")
  .url("myUrl")
  .`type`("twitter")
  .orgtype("personal")
  .category("my", "favorites")
  .Neighborhood.name("myNeighborhood")
  .District.name("myDistrict").region("nw").save
```

Multiple entities:

```
List(
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/name        ,   DDD Blogging Georgetown                        )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/url         ,   http://www.blogginggeorgetown.com/             )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/type        ,   :community.type/blog                           )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/orgtype     ,   :community.orgtype/commercial                  )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/category    ,   DD cat 1                                       )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/category    ,   DD cat 2                                       )
  List(  :db/add,   #db/id[:db.part/user -1000001],   :community/neighborhood,   #db/id[:db.part/user -1000002]                 )
  List(  :db/add,   #db/id[:db.part/user -1000002],   :neighborhood/name     ,   DD Georgetown                                  )
  List(  :db/add,   #db/id[:db.part/user -1000002],   :neighborhood/district ,   #db/id[:db.part/user -1000003]                 )
  List(  :db/add,   #db/id[:db.part/user -1000003],   :district/name         ,   Greater Duwamish                               )
  List(  :db/add,   #db/id[:db.part/user -1000003],   :district/region       ,   :district.region/s                             )
  
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/name        ,   DDD Interbay District Blog                     )
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/url         ,   http://interbayneighborhood.neighborlogs.com/  )
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/type        ,   :community.type/blog                           )
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/orgtype     ,   :community.orgtype/community                   )
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/category    ,   DD cat 3                                       )
  List(  :db/add,   #db/id[:db.part/user -1000004],   :community/neighborhood,   #db/id[:db.part/user -1000005]                 )
  List(  :db/add,   #db/id[:db.part/user -1000005],   :neighborhood/name     ,   DD Interbay                                    )
  List(  :db/add,   #db/id[:db.part/user -1000005],   :neighborhood/district ,   #db/id[:db.part/user -1000006]                 )
  List(  :db/add,   #db/id[:db.part/user -1000006],   :district/name         ,   Magnolia/Queen Anne                            )
  List(  :db/add,   #db/id[:db.part/user -1000006],   :district/region       ,   :district.region/w                             )
)
```

```scala
Community.name.url.`type`.orgtype.category.Neighborhood.name.District.name.region insert List(
  ("DDD Blogging Georgetown", "http://www.blogginggeorgetown.com/", "blog", "commercial", Set("DD cat 1", "DD cat 2"), "DD Georgetown", "Greater Duwamish", "s"),
  ("DDD Interbay District Blog", "http://interbayneighborhood.neighborlogs.com/", "blog", "community", Set("DD cat 3"), "DD Interbay", "Magnolia/Queen Anne", "w")
)
```




### Updating data

Updating one-attribute

```
List(
  List(  :db/add,   17592186045649,   :community/name,   belltown 2  )
  List(  :db/add,   17592186045649,   :community/url ,   url 2       )
)
```

```scala
Community(belltownId).name("belltown 2").url("url 2").update
```

Updating many-attribute

```
List(
  List(  :db/retract,   17592186045649,   :community/category,   news       )
  List(  :db/add    ,   17592186045649,   :community/category,   Cool news  )
)
```

```scala
Community(belltownId).category("news" -> "Cool news").update
```

Update multiple values of many-attribute
```
List(
  List(  :db/retract,   17592186045649,   :community/category,   Cool news          )
  List(  :db/add    ,   17592186045649,   :community/category,   Super cool news    )
  List(  :db/retract,   17592186045649,   :community/category,   events             )
  List(  :db/add    ,   17592186045649,   :community/category,   Super cool events  )
)
```

```scala
Community(belltownId).category(
  "Cool news" -> "Super cool news",
  "events" -> "Super cool events"
).update
```

Update multiple values of many-attribute
```
List(
  List(  :db/retract,   17592186045649,   :community/category,   Cool news          )
  List(  :db/add    ,   17592186045649,   :community/category,   Super cool news    )
  List(  :db/retract,   17592186045649,   :community/category,   events             )
  List(  :db/add    ,   17592186045649,   :community/category,   Super cool events  )
)
```

```scala
Community(belltownId).category(
  "Cool news" -> "Super cool news",
  "events" -> "Super cool events"
).update
```


Add a value to a many-attribute
```
List(
  List(  :db/add,   17592186045649,   :community/category,   extra category  )
)
```

```scala
Community(belltownId).category.add("extra category").update
```

Remove value from a many-attribute
```
List(
  List(  :db/retract,   17592186045649,   :community/category,   Super cool events  )
)
```

```scala
Community(belltownId).category.remove("Super cool events").update
```

Mixing updates and deletes
```
List(
  List(  :db/add    ,   17592186045649,   :community/name    ,   belltown 3                      )
  List(  :db/retract,   17592186045649,   :community/url     ,   http://www.belltownpeople.com/  )
  List(  :db/retract,   17592186045649,   :community/category,   events                          )
  List(  :db/retract,   17592186045649,   :community/category,   news                            )
)
```

```scala
Community(belltownId).name("belltown 3").url().category().update
```