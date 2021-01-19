---
title: "Inspect Queries"
weight: 20
menu:
  main:
    parent: dev
    identifier: dev-inspect-queries
---


# Inspect queries


All molecule query commands have a corresponding inspect command that will print inspection information to console.

- &lt;molecule&gt;.`inspectGet`
- &lt;molecule&gt;.`inspectGetHistory`
- &lt;molecule&gt;.`inspectGetAsOf(...)`
- &lt;molecule&gt;.`inspectGetSince(...)`
- &lt;molecule&gt;.`inspectGetWith(...)`

## inspectGet

Simply replace a `get` command on a molecule with `inspectGet` to print inspection data when running the code in
a test for instance:

```scala
// Molecule to be inspected
Community.name.Neighborhood.District.region_("ne" or "sw").get(3) === List(
  "Beach Drive Blog", 
  "KOMO Communities - Green Lake", 
  "Delridge Produce Cooperative"
)

// Inspect `get`
Community.name.Neighborhood.District.region_("ne" or "sw").inspectGet
```
This will print out the results of each stage of DSL transformation namely Model &#10230; Query &#10230; Datalog Query &#10230; Data:

```
--------------------------------------------------------------------------
Model(List(
  Atom("Community", "name", "String", 1, VarValue, None, Seq(), Seq()),
  Bond("Community", "neighborhood", "Neighborhood", 1, Seq()),
  Bond("Neighborhood", "district", "District", 1, Seq()),
  Atom("District", "region_", "String", 1, Eq(Seq("ne", "sw")), Some(":District.region/"), Seq(), Seq())))

Query(
  Find(List(
    Var("b"))),
  In(
    List(),
    List(
      Rule("rule1", Seq(Var("d")), Seq(
        DataClause(ImplDS, Var("d"), KW("District", "region", ""), Val(":District.region/ne"), Empty, NoBinding))),
      Rule("rule1", Seq(Var("d")), Seq(
        DataClause(ImplDS, Var("d"), KW("District", "region", ""), Val(":District.region/sw"), Empty, NoBinding)))),
    List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Community", "name", ""), Var("b"), Empty, NoBinding),
    DataClause(ImplDS, Var("a"), KW("Community", "neighborhood", "Neighborhood"), Var("c"), Empty, NoBinding),
    DataClause(ImplDS, Var("c"), KW("Neighborhood", "district", "District"), Var("d"), Empty, NoBinding),
    RuleInvocation("rule1", Seq(Var("d"))))))

[:find  ?b
 :in    $ %
 :where [?a :Community/name ?b]
        [?a :Community/neighborhood ?c]
        [?c :Neighborhood/district ?d]
        (rule1 ?d)]

RULES: [
 [(rule1 ?d) [?d :District/region ":District.region/ne"]]
 [(rule1 ?d) [?d :District/region ":District.region/sw"]]
]

INPUTS: none

OUTPUTS:
1  ["Beach Drive Blog"]
2  ["KOMO Communities - Green Lake"]
3  ["Delridge Produce Cooperative"]
4  ["Alki News"]
5  ["Longfellow Creek Community Website"]
6  ["Laurelhurst Community Club"]
7  ["Licton Springs Neighborhood "]
8  ["Friends of Green Lake"]
9  ["KOMO Communities - Wallingford"]
10  ["Magnuson Environmental Stewardship Alliance"]
11  ["MyWallingford"]
12  ["Greenwood Community Council Announcements"]
13  ["Alki News/Alki Community Council"]
14  ["Nature Consortium"]
15  ["Broadview Community Council"]
16  ["KOMO Communities - West Seattle"]
17  ["KOMO Communities - U-District"]
18  ["ArtsWest"]
19  ["Community Harvest of Southwest Seattle"]
20  ["Highland Park Improvement Club"]
21  ["Admiral Neighborhood Association"]
22  ["Morgan Junction Community Association"]
23  ["Greenwood Community Council"]
24  ["Greenwood Community Council Discussion"]
25  ["Genesee-Schmitz Neighborhood Council"]
26  ["Hawthorne Hills Community Website"]
27  ["KOMO Communities - View Ridge"]
28  ["Fauntleroy Community Association"]
29  ["KOMO Communities - Greenwood-Phinney"]
30  ["Delridge Grassroots Leadership"]
31  ["Aurora Seattle"]
32  ["Greenlake Community Wiki"]
33  ["Highland Park Action Committee"]
34  ["Maple Leaf Community Council"]
35  ["Greenwood Aurora Involved Neighbors"]
36  ["Greenwood Phinney Chamber of Commerce"]
37  ["Junction Neighborhood Organization"]
38  ["Delridge Neighborhoods Development Association"]
39  ["Greenwood Blog"]
40  ["My Greenlake Blog"]
41  ["Greenlake Community Council"]
42  ["Maple Leaf Life"]
(showing up to 500 rows)
--------------------------------------------------------------------------
```
While the Model and Query are internal representations of the molecule, the Datalog query shows you what Molecule sends to Datomic.

Using inspectGet can also be a quick way to test if a required data set is correctly returned with some molecule.


### Experiment with Datalog query

If you want to experiment with changing the raw Datalog query, you can copy an paste the Datalog query from console into the query call on the connection object:

```scala
conn.q(
  // Datalog query:
  """[:find  ?b
    | :in    $ %
    | :where [?a :Community/name ?b]
    |        [?a :Community/neighborhood ?c]
    |        [?c :Neighborhood/district ?d]
    |        (rule1 ?d)]""".stripMargin,
  // Input:
  """[
    | [(rule1 ?d) [?d :District/region ":District.region/ne"]]
    | [(rule1 ?d) [?d :District/region ":District.region/sw"]]
    |]""".stripMargin
) foreach println
``` 
Which will print the raw Datomic rows of data to console:

```
List(Beach Drive Blog)
List(KOMO Communities - Green Lake)
List(Delridge Produce Cooperative)
List(Alki News)
List(Longfellow Creek Community Website)
List(Laurelhurst Community Club)
// etc...
```

## inspectGetHistory

When examining data spanning multiple transactions, the time filter inspect commands can be very useful. Say we have 3 transactions:
```scala
val tx1 = Ns.str("a").int(1).save
val e1  = tx1.eid
val t1  = tx1.t // 1028

val tx2 = Ns(e1).str("b").update
val t2  = tx2.t // 1030

val tx3 = Ns(e1).int(2).update
val t3  = tx3.t // 1031
```
Then we can for instance inspect the history of the `:Ns/str` attribute by calling the `inspectGetHistory` method:
```scala
Ns(e1).str.t.op.inspectGetHistory
```
And get the transformations and resulting data
```
--------------------------------------------------------------------------
Model(List(
  Meta("?", "e_", "Long", Eq(Seq(17592186045445L))),
  Atom("Ns", "str", "String", 1, VarValue, None, Seq(), Seq()),
  Meta("?", "t", "t", NoValue),
  Meta("?", "op", "op", NoValue)))

Query(
  Find(List(
    Var("c"),
    Var("c_t"),
    Var("c_op"))),
  In(
    List(
      InVar(CollectionBinding(Var("a")), Seq(Seq(17592186045445L)))),
    List(),
    List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Ns", "str", ""), Var("c"), Var("c_tx"), Var("c_op")),
    Funct("datomic.Peer/toT ^Long", Seq(Var("c_tx")), ScalarBinding(Var("c_t"))))))

[:find  ?c ?c_t ?c_op
 :in    $ [?a ...]
 :where [?a :Ns/str ?c ?c_tx ?c_op]
        [(datomic.Peer/toT ^Long ?c_tx) ?c_t]]

RULES: none

INPUTS: 
1  [17592186045445]

OUTPUTS:
1  ["b" 1030 true]
2  ["a" 1028 true]
3  ["a" 1030 false]
(showing up to 500 rows)
--------------------------------------------------------------------------
```
We can see that the value "a" was asserted in transaction 1028, retracted in 1030 and the new value "b" asserted in 1030.


## inspectGetAsOf

Using the same example transaction above we can inspect data at a certain point in time by calling `inspectGetAsOf(t)`:
```scala
Ns(e1).t.a.v.op.inspectGetAsOf(t1)
```
And get
```
--------------------------------------------------------------------------
Model(List(
  Meta("?", "e_", "Long", Eq(Seq(17592186045445L))),
  Atom("Ns", "str", "String", 1, VarValue, None, Seq(), Seq()),
  Meta("?", "t", "t", NoValue),
  Meta("?", "op", "op", NoValue)))

Query(
  Find(List(
    Var("c"),
    Var("c_t"),
    Var("c_op"))),
  In(
    List(
      InVar(CollectionBinding(Var("a")), Seq(Seq(17592186045445L)))),
    List(),
    List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Ns", "str", ""), Var("c"), Var("c_tx"), Var("c_op")),
    Funct("datomic.Peer/toT ^Long", Seq(Var("c_tx")), ScalarBinding(Var("c_t"))))))

[:find  ?c ?c_t ?c_op
 :in    $ [?a ...]
 :where [?a :Ns/str ?c ?c_tx ?c_op]
        [(datomic.Peer/toT ^Long ?c_tx) ?c_t]]

RULES: none

INPUTS: 
1  [17592186045445]

OUTPUTS:
1  ["a" 1028 true]
(showing up to 500 rows)
--------------------------------------------------------------------------
```
Showing the `:Ns/str` value at transaction 1028


## inspectGetSince

Likewise we can get changes of attribute `:Ns/int` since transaction t1/1028
```scala
Ns(e1).int.t.op.inspectGetSince(t1)
```
And see that `:Ns/int` value 2 was asserted in transaction 1031:

```
--------------------------------------------------------------------------
Model(List(
  Meta("?", "e_", "Long", Eq(Seq(17592186045445L))),
  Atom("Ns", "int", "Int", 1, VarValue, None, Seq(), Seq()),
  Meta("?", "t", "t", NoValue),
  Meta("?", "op", "op", NoValue)))

Query(
  Find(List(
    Var("c"),
    Var("c_t"),
    Var("c_op"))),
  In(
    List(
      InVar(CollectionBinding(Var("a")), Seq(Seq(17592186045445L)))),
    List(),
    List(DS)),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Ns", "int", ""), Var("c"), Var("c_tx"), Var("c_op")),
    Funct("datomic.Peer/toT ^Long", Seq(Var("c_tx")), ScalarBinding(Var("c_t"))))))

[:find  ?c ?c_t ?c_op
 :in    $ [?a ...]
 :where [?a :Ns/int ?c ?c_tx ?c_op]
        [(datomic.Peer/toT ^Long ?c_tx) ?c_t]]

RULES: none

INPUTS: 
1  [17592186045445]

OUTPUTS:
1  [2 1031 true]
(showing up to 500 rows)
--------------------------------------------------------------------------
```



## inspectGetWith



The `with` time filter is a bit special since transactional data is supplied to the method. So the `inspectGetWith(..)` method will also display the transactional data:

```scala
// Normal `getWith` call with some sample data
val johnId = Ns.str("John").int(24).save.eid
Ns.str.int.inspectGetWith(
  Ns.str("John").int(24).getSaveStmts,
  Ns.str.int getInsertStmts List(
    ("Lisa", 20),
    ("Pete", 55)
  ),
  Ns(johnId).int(25).getUpdateStmts
)
```
Will print Model &#10230; Query &#10230; Datalog Query &#10230; Data + statements for the 3 transaction molecules applied:

```
--------------------------------------------------------------------------
Model(List(
  Atom("Ns", "str", "String", 1, VarValue, None, Seq(), Seq()),
  Atom("Ns", "int", "Int", 1, VarValue, None, Seq(), Seq())))

Query(
  Find(List(
    Var("b"),
    Var("c"))),
  Where(List(
    DataClause(ImplDS, Var("a"), KW("Ns", "str", ""), Var("b"), Empty, NoBinding),
    DataClause(ImplDS, Var("a"), KW("Ns", "int", ""), Var("c"), Empty, NoBinding))))

[:find  ?b ?c
 :where [?a :Ns/str ?b]
        [?a :Ns/int ?c]]

RULES: none

INPUTS: none

OUTPUTS:
1  ["John" 25]
(showing up to 500 rows)
--------------------------------------------------------------------------

## 1 ## Statements, transaction molecule 1: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    #db/id[:db.part/user -1000123]   :Ns/str              John
    2          :db/add    #db/id[:db.part/user -1000123]   :Ns/int              24))
================================================================================================================

## 2 ## Statements, transaction molecule 2: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    #db/id[:db.part/user -1000124]   :Ns/str              Lisa
    2          :db/add    #db/id[:db.part/user -1000124]   :Ns/int              20)
  2          List(
    1          :db/add    #db/id[:db.part/user -1000125]   :Ns/str              Pete
    2          :db/add    #db/id[:db.part/user -1000125]   :Ns/int              55))
================================================================================================================

## 3 ## Statements, transaction molecule 3: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    17592186045451                   :Ns/int              25))
================================================================================================================
```
