---
title: "Transaction Bundle"
weight: 85
menu:
  main:
    parent: manual
    identifier: code-transaction-bundle
---



# Transaction bundle

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/transaction/TxBundle.scala)

### Multiple actions in one atomic transaction

[save](/manual/crud/save), [insert](/manual/crud/insert), [update](/manual/crud/update) and [retract](/manual/crud/retract) operations on molecules each execute in their own transaction. By bundling transactions statements from several of those operations we can execute a single transaction that will guarantee atomicity. The bundled transaction will either complete as a whole or abort if there are any transactional errors.

Each of the above operations has an equivalent method for getting the transaction statements it produces:

- `<molecule>.getSaveStmts`
- `<molecule>.getInsertStmts`
- `<molecule>.getUpdateStmts`
- `<entityId>.getRetractStmts`

We can use those methods to build a bundled transaction to atomically perform 4 operations in one transaction:
```scala
// Some initial data
val List(e1, e2, e3) = Ns.int insert List(1, 2, 3) eids

// Transact multiple molecule statements in one bundled transaction
transact(
  // retract entity
  e1.getRetractStmts,
  // save new entity
  Ns.int(4).getSaveStmts,
  // insert multiple new entities
  Ns.int.getInsertStmts(List(5, 6)),
  // update entity
  Ns(e2).int(20).getUpdateStmts
)

// Data after group transaction
Ns.int.get.sorted.map(_ ==> List(
  // 1 retracted
  3, // unchanged
  4, // saved
  5, 6, // inserted
  20 // 2 updated
)
```

Bundled transactions can also use Datomic's asynchronous API by calling `transactAsync`:

```scala
Await.result(
  transactAsync(
    e1.getRetractStmts,
    Ns.int(4).getSaveStmts,
    Ns.int.getInsertStmts(List(5, 6)),
    Ns(e2).int(20).getUpdateStmts
  ) map { bundleTx =>
    Ns.int.getAsync map { queryResult => 
      queryResult.map(_ ==> List(3, 4, 5, 6, 20)    
    }  
  },
  2.seconds
)
```
### Inspecting bundled transactions

If you want to see the transactional output from a bundled transaction you can call `inspectTransaction` on some bundled transaction data:


```scala
// Print inspect info for group transaction without affecting live db
inspectTransact(
  // retract
  e1.getRetractStmts,
  // save
  Ns.int(4).getSaveStmts,
  // insert
  Ns.int.getInsertStmts(List(5, 6)),
  // update
  Ns(e2).int(20).getUpdateStmts
)

// Prints transaction data to output:
/*
  ## 1 ## TxReport
  ========================================================================
  ArrayBuffer(
    List(
      :db.fn/retractEntity   17592186045445)
    List(
      :db/add       #db/id[:db.part/user -1000247]     :Ns/int          4           Card(1))
    List(
      :db/add       #db/id[:db.part/user -1000252]     :Ns/int          5           Card(1))
    List(
      :db/add       #db/id[:db.part/user -1000253]     :Ns/int          6           Card(1))
    List(
      :db/add       17592186045446                     :Ns/int          20          Card(1)))
  ------------------------------------------------
  List(
    added: true ,   t: 13194139534345,   e: 13194139534345,   a: 50,   v: Wed Nov 14 23:38:15 CET 2018

    added: false,  -t: 13194139534345,  -e: 17592186045445,  -a: 64,  -v: 1

    added: true ,   t: 13194139534345,   e: 17592186045450,   a: 64,   v: 4

    added: true ,   t: 13194139534345,   e: 17592186045451,   a: 64,   v: 5

    added: true ,   t: 13194139534345,   e: 17592186045452,   a: 64,   v: 6

    added: true ,   t: 13194139534345,   e: 17592186045446,   a: 64,   v: 20
    added: false,  -t: 13194139534345,  -e: 17592186045446,  -a: 64,  -v: 2)
  ========================================================================
*/
```
Two groups of data are shown. The first group is an internal representation in Molecule showing the operations. The second group shows the datoms produced in the transaction. For ease of reading, "-" (minus) is prepended the prefixes (t, e, a, v) for the datoms that are retractions - where `added` is false.  The abbreviations represents the parts of the Datom:

- `added`: operation, can be true for asserted or false for retracted
- `t`: transaction entity id
- `e`: entity
- `a`: attribute
- `v`: value

Updating 2 to 20 for instance creates two Datoms, one retracting the old value 2 and one asserting the new value 20.

