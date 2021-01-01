---
title: "Debug"
weight: 110
menu:
  main:
    parent: code
    identifier: debug
up:   /manual/generic
prev: /manual/generic/schema
next: /manual/debug/datalog
---

# Debug molecules

See [debug docs](http://www.scalamolecule.org/api/molecule/api/ShowDebug.html)...
<br><br>

3 resources helping debugging molecules:

#### [Show Datalog queries and data](/manual/debug/datalog)

All molecule query commands have a corresponding debug command that will print debugging information to console.

 - &lt;molecule&gt;.`debugGet`
 - &lt;molecule&gt;.`debugGetHistory`
 - &lt;molecule&gt;.`debugGetAsOf(...)`
 - &lt;molecule&gt;.`debugGetSince(...)`
 - &lt;molecule&gt;.`debugGetWith(...)`
 

#### [Debug transactions](/manual/debug/debug-transactions)

Transactional operations can be debugged with the following methods:

 - &lt;molecule&gt;.`debugSave`
 - &lt;molecule&gt;.`debugInsert(data...)`
 - &lt;molecule&gt;.`debugUpdate`
 - &lt;entityId&gt;.`debugRetract`
 - &lt;entityId&gt;.Tx(transactionMolecule).`debugRetract`
 - `debugRetract(entityIds, txMetaDataMolecules*)` 


#### [Common errors and solutions](/manual/debug/err)

List of possible compilation/runtime errors and solutions...


## Debug queries


All molecule query commands have a corresponding debug command that will print debugging information to console.

- &lt;molecule&gt;.`debugGet`
- &lt;molecule&gt;.`debugGetHistory`
- &lt;molecule&gt;.`debugGetAsOf(...)`
- &lt;molecule&gt;.`debugGetSince(...)`
- &lt;molecule&gt;.`debugGetWith(...)`

### debugGet

Simply replace a `get` command on a molecule with `debugGet` to print debugging data when running the code in
a test for instance:

```
// Molecule to be debugged
Community.name.Neighborhood.District.region_("ne" or "sw").get(3) === List(
  "Beach Drive Blog", 
  "KOMO Communities - Green Lake", 
  "Delridge Produce Cooperative"
)

// Debug `get`
Community.name.Neighborhood.District.region_("ne" or "sw").debugGet
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

Using debugGet can also be a quick way to test if a required data set is correctly returned with some molecule.


#### Experiment with Datalog query

If you want to experiment with changing the raw Datalog query, you can copy an paste the Datalog query from console into the query call on the connection object:

```
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

### debugGetHistory

When examining data spanning multiple transactions, the time filter debug commands can be very useful. Say we have 3 transactions:
```
val tx1 = Ns.str("a").int(1).save
val e1  = tx1.eid
val t1  = tx1.t // 1028

val tx2 = Ns(e1).str("b").update
val t2  = tx2.t // 1030

val tx3 = Ns(e1).int(2).update
val t3  = tx3.t // 1031
```
Then we can for instance debug the history of the `:Ns/str` attribute by calling the `debugGetHistory` method:
```
Ns(e1).str.t.op.debugGetHistory
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


### debugGetAsOf(..)

Using the same example transaction above we can debug data at a certain point in time by calling `debugGetAsOf(t)`:
```
Ns(e1).t.a.v.op.debugGetAsOf(t1)
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


### debugGetSince(..)

Likewise we can get changes of attribute `:Ns/int` since transaction t1/1028
```
Ns(e1).int.t.op.debugGetSince(t1)
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



### debugGetWith(txData..)



The `with` time filter is a bit special since transactional data is supplied to the method. So the `debugGetWith(..)` method will also display the transactional data:

```
// Normal `getWith` call with some sample data
val fred = Ns.str("Fred").int(42).save.eid
Ns.str.int.debugGetWith(
  Ns.str("John").int(44).getSaveTx,
  Ns.str.int getInsertTx List(
    ("Lisa", 23),
    ("Pete", 24)
  ),
  Ns(fred).int(43).getUpdateTx
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
1  ["Fred" 42]
(showing up to 500 rows)
--------------------------------------------------------------------------

## 1 ## Statements, transaction molecule 1: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    #db/id[:db.part/user -1000123]   :Ns/str              John
    2          :db/add    #db/id[:db.part/user -1000123]   :Ns/int              44))
================================================================================================================

## 2 ## Statements, transaction molecule 2: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    #db/id[:db.part/user -1000124]   :Ns/str              Lisa
    2          :db/add    #db/id[:db.part/user -1000124]   :Ns/int              23)
  2          List(
    1          :db/add    #db/id[:db.part/user -1000125]   :Ns/str              Pete
    2          :db/add    #db/id[:db.part/user -1000125]   :Ns/int              24))
================================================================================================================

## 3 ## Statements, transaction molecule 3: 
================================================================================================================
1          List(
  1          List(
    1          :db/add    17592186045451                   :Ns/int              43))
================================================================================================================
```


## Debug transactions

Transactional operations can be debugged with the following methods:

- &lt;molecule&gt;.`debugSave`
- &lt;molecule&gt;.`debugInsert(data...)`
- &lt;molecule&gt;.`debugUpdate`
- &lt;entityId&gt;.`debugRetract`
- &lt;entityId&gt;.Tx(transactionMolecule).`debugRetract`
- `debugRetract(entityIds, txMetaDataMolecules*)`

Calling these debug methods will print the produced transaction statements to console only and not perform any transaction on the database.


### Debug Save

If we are about to save a molecule we can instead call `debugSave` on the same molecule to see what transaction statements Molecule will send to Datomic.
```
// Normal save
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").save

// Debugging the save transaction statements
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").debugSave
```

Transactional data in Molecule is based on the Model representation that is transformed to an intermediate transaction representation, the "Statements Model", and then finally the Datomic transaction statements.

```
## 1 ## output.Molecule.debugSave 
================================================================================================================
1          Model(
  1          Atom("Ns", "str", "String", 1, Eq(Seq("273 Broadway")), None, Seq(), Seq())
  2          Bond("Ns", "ref1", "Ref1", 1, Seq())
  3          Atom("Ref1", "int1", "Int", 1, Eq(Seq(10700)), None, Seq(), Seq())
  4          Atom("Ref1", "str1", "String", 1, Eq(Seq("New York")), None, Seq(), Seq())
  5          Bond("Ref1", "ref2", "Ref2", 1, Seq())
  6          Atom("Ref2", "str2", "String", 1, Eq(Seq("USA")), None, Seq(), Seq()))
----------------------------------------------------------------------------------------------------------------
2          List(
  1          :db/add    'tempId                          :Ns/str              Values(Eq(Seq("273 Broadway")),None)
  2          :db/add    'e                               :Ns/ref1             :ref1
  3          :db/add    'v                               :Ref1/int1           Values(Eq(Seq(10700)),None)
  4          :db/add    'e                               :Ref1/str1           Values(Eq(Seq("New York")),None)
  5          :db/add    'e                               :Ref1/ref2           :ref2
  6          :db/add    'v                               :Ref2/str2           Values(Eq(Seq("USA")),None))
----------------------------------------------------------------------------------------------------------------
3          List(
  1          :db/add    #db/id[:db.part/user -1001200]   :Ns/str              273 Broadway
  2          :db/add    #db/id[:db.part/user -1001200]   :Ns/ref1             #db/id[:db.part/user -1001201]
  3          :db/add    #db/id[:db.part/user -1001201]   :Ref1/int1           10700
  4          :db/add    #db/id[:db.part/user -1001201]   :Ref1/str1           New York
  5          :db/add    #db/id[:db.part/user -1001201]   :Ref1/ref2           #db/id[:db.part/user -1001204]
  6          :db/add    #db/id[:db.part/user -1001204]   :Ref2/str2           USA)
================================================================================================================
```

#### Debug save with tx meta data

Debug save with transaction meta data by calling `debugSave`.
```
Ns.int(1).Tx(Ns.str_("meta data")).debugSave
```
The following is then printed to console:
```
## 1 ## output.Molecule.debugSave 
================================================================================================================
1          Model(
  1          Atom("Ns", "int", "Int", 1, Eq(Seq(1)), None, Seq(), Seq())
  2          TxMetaData(
    1          Atom("Ns", "str_", "String", 1, Eq(Seq("meta data")), None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
2          List(
  1          :db/add    'tempId                          :Ns/int              Values(Eq(Seq(1)),None)
  2          :db/add    'tx                              :Ns/str              Values(Eq(Seq("meta data")),None))
----------------------------------------------------------------------------------------------------------------
3          List(
  1          :db/add    #db/id[:db.part/user -1000590]   :Ns/int              1
  2          :db/add    datomic.tx                       :Ns/str              meta data)
================================================================================================================
```
Note how the `:Ns/str` attribute meta value "meta data" is asserted with the current transaction entity (identified by `datomic.tx`).


### Debug insert

If we for instance have a nested insert it could be valuable to break it down and see what transaction statements Molecule produces by applying the same insertion data to the `debugInsert` method:
```
// Nested insert
m(Ns.str.Refs1 * Ref1.int1.str1).insert(
  "order", List((4, "product1"), (7, "product2"))
)

// Debugging nested insert transaction statements
m(Ns.str.Refs1 * Ref1.int1.str1).debugInsert(
  "order", List((4, "product1"), (7, "product2"))
)
```
The following is then printed to console:
```
## 1 ## output.Molecule._debugInsert 
================================================================================================================
1          Model(
  1          Atom("Ns", "str", "String", 1, VarValue, None, Seq(), Seq())
  2          Nested(
    1          Bond("Ns", "refs1", "Ref1", 2, Seq())
    2          Atom("Ref1", "int1", "Int", 1, VarValue, None, Seq(), Seq())
    3          Atom("Ref1", "str1", "String", 1, VarValue, None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
2          List(
  1          :db/add    'tempId                          :Ns/str              'arg
  2          :db/add    'e                               :Ns/refs1            List(
    1          :db/add    'v                               :Ref1/int1           'arg
    2          :db/add    'e                               :Ref1/str1           'arg))
----------------------------------------------------------------------------------------------------------------
3          List(
  1          List(
    1          order
    2          List(
      1          4 -> product1
      2          7 -> product2)))
----------------------------------------------------------------------------------------------------------------
4          List(
  1          List(
    1          order
    2          List(
      1          4 -> product1
      2          7 -> product2)))
----------------------------------------------------------------------------------------------------------------
5          List(
  1          List(
    1          :db/add    #db/id[:db.part/user -1001476]   :Ns/str              order
    2          :db/add    #db/id[:db.part/user -1001476]   :Ns/refs1            #db/id[:db.part/user -1001477]
    3          :db/add    #db/id[:db.part/user -1001477]   :Ref1/int1           4
    4          :db/add    #db/id[:db.part/user -1001477]   :Ref1/str1           product1
    5          :db/add    #db/id[:db.part/user -1001476]   :Ns/refs1            #db/id[:db.part/user -1001478]
    6          :db/add    #db/id[:db.part/user -1001478]   :Ref1/int1           7
    7          :db/add    #db/id[:db.part/user -1001478]   :Ref1/str1           product2))
================================================================================================================
```
Note how the order entity (-1001476) is referencing each created nested order line entity.

`debugInsert` creates 5 representations from the insert molecule:

- Model
- Statements model
- Raw data
- Untupled data
- Datomic transactions


### Debug update

```
// Initial data
val eid = Ns.int(1).str("a").save.eid
// Update - note how we try to update to the same `str` value
Ns(eid).int(2).str("a").update

// Debugging the update
Ns(eid).int(2).str("a").debugUpdate
```
Calling `debugUpdate` on the update molecule shows us, that the `str` data statement is not passed to Datomic since the same value is already asserted.
```
## 1 ## output.Molecule.debugUpdate 
================================================================================================================
1          Model(
  1          Meta("?", "e_", "Long", Eq(Seq(17592186045445L)))
  2          Atom("Ns", "int", "Int", 1, Eq(Seq(2)), None, Seq(), Seq())
  3          Atom("Ns", "str", "String", 1, Eq(Seq("a")), None, Seq(), Seq()))
----------------------------------------------------------------------------------------------------------------
2          List(
  1          :db/add    17592186045445                   :Ns/int              Values(Eq(Seq(2)),None)
  2          :db/add    17592186045445                   :Ns/str              Values(Eq(Seq("a")),None))
----------------------------------------------------------------------------------------------------------------
3          List(
  1          :db/add    17592186045445                   :Ns/int              2)
================================================================================================================
```
Datomic will internally create a retraction of the old value 1 for the attribute `:Ns/int`. We can confirm this by debugging the history data:

```
Ns(eid).a.v.t.op.debugGetHistory

--------------------------------------------------------------------------
// Model, Query, Datalog...

OUTPUTS:
1  [":Ns/int" 1 1030 false]  // <-- 1 was retracted
2  [":Ns/str" "a" 1028 true]
3  [":Ns/int" 2 1030 true]
4  [":Ns/int" 1 1028 true]
(showing up to 500 rows)
--------------------------------------------------------------------------
```


### Debug retract entity

A single entity can be retracted by simply calling the implicit `retract` method on an entity id. To see what transaction statements this produces, call `debugRetract` instead:
```
eid.debugRetract
```
This shows that a single transaction is produced to retract the entity:
```
## 1 ## Debug `retract` on entity 
================================================================================================================
1          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445))
================================================================================================================
```

#### Add Tx meta data to retraction on entity id
We can add transaction meta data to the entity retraction in order to be able to later track what kind of retraction that happened.
```
eid.Tx(Ns.str("meta")).debugRetract
```
We see that the additional datom with the meta value "meta" was associated with the transaction entity (datomic.tx) where the retraction of the entity is performed.
```
================================================================================================================
1          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445
    2          :db/add    datomic.tx                       :Ns/str              meta))
================================================================================================================
```



### Debug retract multiple entities

Debug retracting multiple entities with `debugRetract`
```
debugRetract(Seq(e1, e2))
```
Two entity retraction statements produced.
```
## 1 ## molecule.Datomic.debugRetract 
================================================================================================================
1          Model(
)
----------------------------------------------------------------------------------------------------------------
2          List(
)
----------------------------------------------------------------------------------------------------------------
3          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445
    2          :db.fn/retractEntity   17592186045446))
================================================================================================================
```


#### Add Tx meta data to retraction of multiple entity ids
Let's add some transaction meta data to the retraction
```
debugRetract(Seq(e1, e2), Ref1.str1("Some tx info"))
```
Then we can see how the `:Ref1/str1` attribute value "Some tx info" is added as a statement to the transaction that retracts the two entities:
```
## 1 ## molecule.Datomic.debugRetract 
================================================================================================================
1          Model(
  1          TxMetaData(
    1          Atom("Ref1", "str1", "String", 1, Eq(Seq("Some tx info")), None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
2          List(
  1          :db/add    'tx                              :Ref1/str1           Values(Eq(Seq("Some tx info")),None))
----------------------------------------------------------------------------------------------------------------
3          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445
    2          :db.fn/retractEntity   17592186045446
    3          :db/add    datomic.tx                       :Ref1/str1           Some tx info))
================================================================================================================
```

## Common errors

As errors and solutions are encountered this list is updated to help developers working with Molecule.

#### Compile time
- [Inferred: Cannot resolve overloaded method 'inputMolecule'
  <br>Compiled: overloaded method value apply with alternatives:](#101)

#### Runtime
- [JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused](#201)



#### Cannot resolve overloaded method 'inputMolecule' {#101}
When forgetting to explicitly calling `m` on an input molecule
```
val inputMolecule = Community.name(?)
inputMolecule("Ben") // will not compile and likely be inferred as an error in your IDE
``` 

Input molecule needs to be declared explicitly with the `m` method
```
val inputMolecule = m(Community.name(?))
// Now we can apply value to input molecule
inputMolecule("Ben")
``` 


#### JdbcSQLException: Connection is broken: "java.net.ConnectException: Connection refused {#201}

Datomic transactor is not running - start it.