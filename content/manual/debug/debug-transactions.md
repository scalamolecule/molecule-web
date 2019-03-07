---

title: "Transactions"
weight: 20
menu:
  main:
    parent: debug
    identifier: debug-transactions
up:   /manual/debug
prev: /manual/debug/datalog
next: /manual/debug/err
---


# Debug transactions

Transactional operations can be debugged with the following methods:

 - &lt;molecule&gt;.`debugSave`
 - &lt;molecule&gt;.`debugInsert(data...)`
 - &lt;molecule&gt;.`debugUpdate`
 - &lt;entityId&gt;.`debugRetract`
 - &lt;entityId&gt;.Tx(transactionMolecule).`debugRetract`
 - `debugRetract(entityIds, txMetaDataMolecules*)`

Calling these debug methods will print the produced transaction statements to console only and not perform
any transaction on the database.

## Debug Save

If we are about to save a molecule we can instead call `debugSave` on the same molecule to see what transaction
statements Molecule will send to Datomic. 
```scala
// Normal save
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").save

// Debugging the save transaction statements
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").debugSave
```

Transactional data in Molecule is based on the Model representation that is transformed to an intermediate 
transaction representation, the "Statements Model", and then finally the Datomic transaction statements. 

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

### Debug save with tx meta data

Debug save with transaction meta data by calling `debugSave`. 
```scala
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
Note how the `:Ns/str` attribute meta value "meta data" is asserted with the current transaction entity 
(identified by `datomic.tx`).


## Debug insert

If we for instance have a nested insert it could be valuable to break it down and see what transaction 
statements Molecule produces by applying the same insertion data to the `debugInsert` method:
```scala
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


## Debug update

```scala
// Initial data
val eid = Ns.int(1).str("a").save.eid
// Update - note how we try to update to the same `str` value
Ns(eid).int(2).str("a").update

// Debugging the update
Ns(eid).int(2).str("a").debugUpdate
```
Calling `debugUpdate` on the update molecule shows us, that the `str` data statement is not passed to 
Datomic since the same value is already asserted.
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
Datomic will internally create a retraction of the old value 1 for the attribute `:Ns/int`. We can confirm this
by debugging the history data:

```scala
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


## Debug retract entity

A single entity can be retracted by simply calling the implicit `retract` method on an entity id. To see 
what transaction statements this produces, call `debugRetract` instead:
```scala
eid.debugRetract
```
This shows that a single transaction is produced to retract the entity:
```scala
## 1 ## Debug `retract` on entity 
================================================================================================================
1          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445))
================================================================================================================
```

### Add Tx meta data to retraction on entity id
We can add transaction meta data to the entity retraction in order to be able to later track what kind of
retraction that happened.
```scala
eid.Tx(Ns.str("meta")).debugRetract
```
We see that the additional datom with the meta value "meta" was associated with the transaction entity (datomic.tx)
where the retraction of the entity is performed.
```scala
================================================================================================================
1          List(
  1          List(
    1          :db.fn/retractEntity   17592186045445
    2          :db/add    datomic.tx                       :Ns/str              meta))
================================================================================================================
```








## Debug retract multiple entities

Debug retracting multiple entities with `debugRetract`
```scala
debugRetract(Seq(e1, e2))
```
Two entity retraction statements produced.
```scala
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


### Add Tx meta data to retraction of multiple entity ids
Let's add some transaction meta data to the retraction
```scala
debugRetract(Seq(e1, e2), Ref1.str1("Some tx info"))
```
Then we can see how the `:Ref1/str1` attribute value "Some tx info" is added as a statement to the transaction
that retracts the two entities:
```scala
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





### Next

[Debug errors...](/manual/debug/err)