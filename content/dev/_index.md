---
title: "Macro transformations"
weight: 10
menu:
    main:
        parent: dev
---

# Macro transformations

Here's some info for developers about how Molecule works in the background.

During _project_ compilation with sbt, the [MoleculePlugin](https://github.com/scalamolecule/sbt-molecule) generates boilerplate code for us so that we can write molecules in our programs. 

Our Molecule source code is then during _program_ compilation transformed through 4 stages into a Datomic query text string or formatted transaction data that Datomic understands. The response from Datomic is then sent back to your program, typed and formatted.



## 1. Molecule source code

Let's transform the source code of a molecule describing southern media communities:

```scala
Community.name.`type`("twitter" or "facebook_page")
  .Neighborhood.District.region("sw" or "s" or "se")
```


## 2. Model AST

The source code of our molecule is pattern matched in [Dsl2Model](https://github.com/scalamolecule/molecule/blob/master/molecule/shared/src/main/scala/molecule/core/transform/Dsl2Model.scala) element by element in order to create an abstracted internal Molecule `Model` of `Atom`s and `Bond`s with information about Namespace, Atribute name, type, cardinality, value, generic options etc. for each attribute/`Atom` and relationship/`Bond`  (just a simple selection shown here):

```scala
Model(List(
  Atom("Community", "name", "String", 1, VarValue, None),
  Atom("Community", "type_", "String", 1, Eq(List("twitter", "facebook_page")), Some(":Community.type/")),
  Bond("Community", "neighborhood", "Neighborhood"),
  Bond("Neighborhood", "district", "District"),
  Atom("District", "region_", "String", 1, Eq(List("sw", "s", "se")), Some(":District.region/")))
)
```
This Molecule `Model` is the generic representation of how we combine the Attributes of our Data Model into molecules.


## 3. Query AST
Our Molecule Model is then transformed in [Model2Query](https://github.com/scalamolecule/molecule/blob/master/molecule/shared/src/main/scala/molecule/datomic/base/transform/Model2Query.scala) to a Query AST which is a little more elaborate:

```scala
Query(
  Find(List(
    Var("b"))),
  In(List(), List(
    Rule("rule1", List(Var("a")), List(
      DataClause(ImplDS, Var("a"), KW("Community", "type"), Val(":Community.type/twitter"), Empty))),
    Rule("rule1", List(Var("a")), List(
      DataClause(ImplDS, Var("a"), KW("Community", "type"), Val(":Community.type/facebook_page"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("District", "region"), Val(":District.region/sw"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("District", "region"), Val(":District.region/s"), Empty))),
    Rule("rule2", List(Var("e")), List(
      DataClause(ImplDS, Var("e"), KW("District", "region"), Val(":District.region/se"), Empty)))), List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Community", "name"), Var("b"), Empty),
    RuleInvocation("rule1", List(Var("a"))),
    DataClause(ImplDS, Var("a"), KW("Community", "neighborhood", "Neighborhood"), Var("d"), Empty),
    DataClause(ImplDS, Var("d"), KW("Neighborhood", "district", "District"), Var("e"), Empty),
    RuleInvocation("rule2", List(Var("e")))))
)
``` 
As you see this [Query AST](https://github.com/scalamolecule/molecule/blob/master/molecule/shared/src/main/scala/molecule/datomic/base/ast/query.scala) is tailored to Datomic.

In principle we should be able to use the same model to create other Query abstractions tailored to other database systems!...


## 4. Datomic query string

Finally Molecule transforms our Query AST in [Query2String](https://github.com/scalamolecule/molecule/blob/master/molecule/shared/src/main/scala/molecule/datomic/base/transform/Query2String.scala) to a Datomic query text strings:

<pre>
[:find  ?b
 :in    $ %
 :where [?a :Community/name ?b]
        (rule1 ?a)
        [?a :Community/neighborhood ?d]
        [?d :Neighborhood/district ?e]
        (rule2 ?e)]

INPUTS:
List(
  1 datomic.db.Db@xxx
  2 [[(rule1 ?a) [?a :Community/type ":Community.type/twitter"]]
     [(rule1 ?a) [?a :Community/type ":Community.type/facebook_page"]]
     [(rule2 ?e) [?e :District/region ":District.region/sw"]]
     [(rule2 ?e) [?e :District/region ":District.region/s"]]
     [(rule2 ?e) [?e :District/region ":District.region/se"]]]
</pre>

All 3 transformations happen at compile time and therefore have no impact on the runtime performance.


## Transactional data

Transactional data is created by transforming a molecule Model to Statements in [Model2Stmts](https://github.com/scalamolecule/molecule/blob/master/molecule/shared/src/main/scala/molecule/core/transform/Model2Stmts.scala).