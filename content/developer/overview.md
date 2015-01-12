---
date: 2014-05-14T02:13:50Z
title: "Developer"
weight: 0
menu:
  main:
    parent: developer
---

# Developer


- Source code transformation
- 

As an example: to find

_Names of twitter/facebook_page communities in neighborhoods of southern districts_
 
we can compose a "molecule query" that is very close to our
human sentence:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```

Molecule transforms this at compile time (with macros) to a little more elaborate Datalog query string and
 input rules that finds those communities in the Datomic database:

<pre>
[:find ?a
 :in $ %
 :where
   [?ent :community/name ?a]
   (rule1 ?ent)
   [?ent :community/neighborhood ?c]
   [?c :neighborhood/district ?d]
   (rule2 ?d)]

INPUTS:
List(
  datomic.db.Db@xxx,
  [[[rule1 ?ent] [?ent :community/type ":community.type/twitter"]]
   [[rule1 ?ent] [?ent :community/type ":community.type/facebook_page"]]
   [[rule2 ?d] [?d :district/region ":district.region/sw"]]
   [[rule2 ?d] [?d :district/region ":district.region/s"]]
   [[rule2 ?d] [?d :district/region ":district.region/se"]]]
)
</pre>

#### Benefits

By not having to write such complex Datalog queries and rules "by hand", Molecule 
allows you to

- Type less
- Make type safe queries with inferred return types
- Use your domain terms directly as query building blocks
- Model complex queries intuitively (easier to understand and maintain)
- Reduce syntactic noise
- Focus more on your domain and less on queries

#### Possible drawbacks

We still need to explore how far Molecule can match the expressive powers
 of Datalog. So far, all 
 examples in the
[Seattle tutorial][seattle] have been 
"molecularized" succesfully (see the 
[Molecule Seattle tutorial][tutorial] and 
[code][tutorialcode]). So as a proof-of-concept it looks promising...