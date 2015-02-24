---
date: 2014-05-14T02:13:50Z
title: "Transformation"
weight: 30
menu:
  main:
    parent: developer
---

# Macro transformation

### 1. Source code

A little more elaborate molecule like

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```
is at compile time taken through a series of various abstraction transformations by Molecule macros.

### 2. Model

Our source code is pattern matched element by element and abstracted to a Model of `Atoms` and `Bonds`:

```scala
Model(List(
  Atom("community", "name", "String", 1, VarValue, None),
  Atom("community", "type_", "String", 1, Eq(List("twitter", "facebook_page")), Some(":community.type/")),
  Bond("community", "neighborhood", "neighborhood"),
  Bond("neighborhood", "district", "district"),
  Atom("district", "region_", "String", 1, Eq(List("sw", "s", "se")), Some(":district.region/")))
)
```
This simple [Model AST](https://github.com/scalamolecule/molecule/blob/master/core/src/main/scala/molecule/ast/model.scala#L26-L33) has shown to cover a surprising wide spectre of queries.

### 3. Query
Our model is then transformed to a Query AST which is a little more elaborate:

```scala
Query(
  Find(List(
    Var("b"))),
  In(List(), List(
    Rule("rule1", List(Var("a")), List(
      DataClause(ImplDS, Var("a"), KW("community", "type"), Val(":community.type/twitter"), Empty))),
    Rule("rule1", List(Var("a")), List(
      DataClause(ImplDS, Var("a"), KW("community", "type"), Val(":community.type/facebook_page"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("district", "region"), Val(":district.region/sw"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("district", "region"), Val(":district.region/s"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("district", "region"), Val(":district.region/se"), Empty)))), List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("community", "name"), Var("b"), Empty),
    RuleInvocation("rule1", List(Var("a"))),
    DataClause(ImplDS, Var("a"), KW("community", "neighborhood", "neighborhood"), Var("d"), Empty),
    DataClause(ImplDS, Var("d"), KW("neighborhood", "district", "district"), Var("e"), Empty),
    RuleInvocation("rule2", List(Var("e")))))
)
``` 
As you see this abstraction is tailored to Datomic.

In principle we should be able to use the same model to create other Query abstractions tailored to other database systems!...

### 4. Datomic query string

Finally Molecule transforms our Query AST to Datomic query text strings:

<pre>
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
</pre>

