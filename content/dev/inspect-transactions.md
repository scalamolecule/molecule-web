---
title: "Inspect Transactions"
weight: 30
menu:
  main:
    parent: dev
    identifier: dev-inspect-transactions
---

# Inspect transactions

Transactional operations can be inspected with the following methods:

- &lt;molecule&gt;.`inspectSave`
- &lt;molecule&gt;.`inspectInsert(data...)`
- &lt;molecule&gt;.`inspectUpdate`
- &lt;entityId&gt;.`inspectRetract`
- &lt;entityId&gt;.Tx(transactionMolecule).`inspectRetract`
- `inspectRetract(entityIds, txMetaDataMolecules*)`

Calling these inspect methods will print the produced transaction statements to console only and not perform any transaction on the database.


## inspectSave

If we are about to save a molecule we can instead call `inspectSave` on the same molecule to see what transaction statements Molecule will send to Datomic.
```scala
// Normal save
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").save

// Inspecting the save transaction statements
Ns.str("273 Broadway").Ref1.int1(10700).str1("New York").Ref2.str2("USA").inspectSave
```

Transactional data in Molecule is based on the Model representation that is transformed to an intermediate transaction representation, the "Statements Model", and then finally the Datomic transaction statements.

```
## 1 ## output.Molecule.inspectSave 
================================================================================================================
Model(
  Atom("Ns", "str", "String", 1, Eq(Seq("273 Broadway")), None, Seq(), Seq())
  Bond("Ns", "ref1", "Ref1", 1, Seq())
  Atom("Ref1", "int1", "Int", 1, Eq(Seq(10700)), None, Seq(), Seq())
  Atom("Ref1", "str1", "String", 1, Eq(Seq("New York")), None, Seq(), Seq())
  Bond("Ref1", "ref2", "Ref2", 1, Seq())
  Atom("Ref2", "str2", "String", 1, Eq(Seq("USA")), None, Seq(), Seq()))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    'tempId                          :Ns/str              Values(Eq(Seq("273 Broadway")),None)
  :db/add    'e                               :Ns/ref1             :ref1
  :db/add    'v                               :Ref1/int1           Values(Eq(Seq(10700)),None)
  :db/add    'e                               :Ref1/str1           Values(Eq(Seq("New York")),None)
  :db/add    'e                               :Ref1/ref2           :ref2
  :db/add    'v                               :Ref2/str2           Values(Eq(Seq("USA")),None))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    #db/id[:db.part/user -1001200]   :Ns/str              273 Broadway
  :db/add    #db/id[:db.part/user -1001200]   :Ns/ref1             #db/id[:db.part/user -1001201]
  :db/add    #db/id[:db.part/user -1001201]   :Ref1/int1           10700
  :db/add    #db/id[:db.part/user -1001201]   :Ref1/str1           New York
  :db/add    #db/id[:db.part/user -1001201]   :Ref1/ref2           #db/id[:db.part/user -1001204]
  :db/add    #db/id[:db.part/user -1001204]   :Ref2/str2           USA)
================================================================================================================
```

### Inspect save with tx meta data

Inspect save with transaction meta data by calling `inspectSave`.
```scala
Ns.int(1).Tx(Ns.str_("meta data")).inspectSave
```
The following is then printed to console:
```
## 1 ## output.Molecule.inspectSave 
================================================================================================================
Model(
  Atom("Ns", "int", "Int", 1, Eq(Seq(1)), None, Seq(), Seq())
  TxMetaData(
    Atom("Ns", "str_", "String", 1, Eq(Seq("meta data")), None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    'tempId                          :Ns/int              Values(Eq(Seq(1)),None)
  :db/add    'tx                              :Ns/str              Values(Eq(Seq("meta data")),None))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    #db/id[:db.part/user -1000590]   :Ns/int              1
  :db/add    datomic.tx                       :Ns/str              meta data)
================================================================================================================
```
Note how the `:Ns/str` attribute meta value "meta data" is asserted with the current transaction entity (identified by `datomic.tx`).


## inspectInsert

If we for instance have a nested insert it could be valuable to break it down and see what transaction statements Molecule produces by applying the same insertion data to the `inspectInsert` method:
```scala
// Nested insert
m(Ns.str.Refs1 * Ref1.int1.str1).insert(
  "order", List((4, "product1"), (7, "product2"))
)

// Inspecting nested insert transaction statements
m(Ns.str.Refs1 * Ref1.int1.str1).inspectInsert(
  "order", List((4, "product1"), (7, "product2"))
)
```
The following is then printed to console:
```
## 1 ## output.Molecule._inspectInsert 
================================================================================================================
Model(
  Atom("Ns", "str", "String", 1, VarValue, None, Seq(), Seq())
  Nested(
    Bond("Ns", "refs1", "Ref1", 2, Seq())
    Atom("Ref1", "int1", "Int", 1, VarValue, None, Seq(), Seq())
    Atom("Ref1", "str1", "String", 1, VarValue, None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    'tempId                          :Ns/str              'arg
  :db/add    'e                               :Ns/refs1            List(
    :db/add    'v                               :Ref1/int1           'arg
    :db/add    'e                               :Ref1/str1           'arg))
----------------------------------------------------------------------------------------------------------------
List(
  List(
    order
    List(
      4 -> product1
      7 -> product2)))
----------------------------------------------------------------------------------------------------------------
List(
  List(
    order
    List(
      4 -> product1
      7 -> product2)))
----------------------------------------------------------------------------------------------------------------
List(
  List(
    :db/add    #db/id[:db.part/user -1001476]   :Ns/str              order
    :db/add    #db/id[:db.part/user -1001476]   :Ns/refs1            #db/id[:db.part/user -1001477]
    :db/add    #db/id[:db.part/user -1001477]   :Ref1/int1           4
    :db/add    #db/id[:db.part/user -1001477]   :Ref1/str1           product1
    :db/add    #db/id[:db.part/user -1001476]   :Ns/refs1            #db/id[:db.part/user -1001478]
    :db/add    #db/id[:db.part/user -1001478]   :Ref1/int1           7
    :db/add    #db/id[:db.part/user -1001478]   :Ref1/str1           product2))
================================================================================================================
```
Note how the order entity (-1001476) is referencing each created nested order line entity.

`inspectInsert` creates 5 representations from the insert molecule:

- Model
- Statements model
- Raw data
- Untupled data
- Datomic transactions


## inspectUpdate

```scala
for {
  // Initial data
  eid <- Ns.int(1).str("a").save.map(_.eid)
  // Update - note how we try to update to the same `str` value
  _ <- Ns(eid).int(2).str("a").update
  
  // Inspecting the update
  _ <- Ns(eid).int(2).str("a").inspectUpdate
} yield ()
```
Calling `inspectUpdate` on the update molecule shows us, that the `str` data statement is not passed to Datomic since the same value is already asserted.
```
## 1 ## output.Molecule.inspectUpdate 
================================================================================================================
Model(
  Meta("?", "e_", "Long", Eq(Seq(17592186045445L)))
  Atom("Ns", "int", "Int", 1, Eq(Seq(2)), None, Seq(), Seq())
  Atom("Ns", "str", "String", 1, Eq(Seq("a")), None, Seq(), Seq()))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    17592186045445                   :Ns/int              Values(Eq(Seq(2)),None)
  :db/add    17592186045445                   :Ns/str              Values(Eq(Seq("a")),None))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    17592186045445                   :Ns/int              2)
================================================================================================================
```
Datomic will internally create a retraction of the old value 1 for the attribute `:Ns/int`. We can confirm this by inspecting the history data:

```scala
Ns(eid).a.v.t.op.inspectGetHistory
```

```
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


## inspectRetract

A single entity can be retracted by simply calling the implicit `retract` method on an entity id. To see what transaction statements this produces, call `inspectRetract` instead:
```scala
eid.inspectRetract
```
This shows that a single transaction is produced to retract the entity:
```
## 1 ## Inspect `retract` on entity 
================================================================================================================
List(
  List(
    :db.fn/retractEntity   17592186045445))
================================================================================================================
```

### Add Tx meta data to retraction on entity id
We can add transaction meta data to the entity retraction in order to be able to later track what kind of retraction that happened.
```scala
eid.Tx(Ns.str("meta")).inspectRetract
```
We see that the additional datom with the meta value "meta" was associated with the transaction entity (datomic.tx) where the retraction of the entity is performed.
```
================================================================================================================
List(
  List(
    :db.fn/retractEntity   17592186045445
    :db/add    datomic.tx                       :Ns/str              meta))
================================================================================================================
```



## inspectRetract(eids)

Inspect retracting multiple entities with `inspectRetract`
```scala
inspectRetract(Seq(e1, e2))
```
Two entity retraction statements produced.
```
## 1 ## molecule.Datomic.inspectRetract 
================================================================================================================
Model()
----------------------------------------------------------------------------------------------------------------
List()
----------------------------------------------------------------------------------------------------------------
List(
  List(
    :db.fn/retractEntity   17592186045445
    :db.fn/retractEntity   17592186045446))
================================================================================================================
```


### Add Tx meta data to retraction of multiple entity ids
Let's add some transaction meta data to the retraction
```scala
inspectRetract(Seq(e1, e2), Ref1.str1("Some tx info"))
```
Then we can see how the `:Ref1/str1` attribute value "Some tx info" is added as a statement to the transaction that retracts the two entities:
```
## 1 ## molecule.Datomic.inspectRetract 
================================================================================================================
Model(
  TxMetaData(
    Atom("Ref1", "str1", "String", 1, Eq(Seq("Some tx info")), None, Seq(), Seq())))
----------------------------------------------------------------------------------------------------------------
List(
  :db/add    'tx                              :Ref1/str1           Values(Eq(Seq("Some tx info")),None))
----------------------------------------------------------------------------------------------------------------
List(
  List(
    :db.fn/retractEntity   17592186045445
    :db.fn/retractEntity   17592186045446
    :db/add    datomic.tx                       :Ref1/str1           Some tx info))
================================================================================================================
```
