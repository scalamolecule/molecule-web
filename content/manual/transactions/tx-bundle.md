---
title: "Tx bundle"
weight: 20
menu:
  main:
    parent: transactions
up:   /manual/transactions
prev: /manual/transactions
next: /manual/transactions/tx-functions
down: /manual/time
---

# Transaction bundle

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/transaction/TxBundle.scala) 

## Multiple actions in one atomic transaction

[save](/manual/crud/save), 
[insert](/manual/crud/insert), 
[update](/manual/crud/update) and 
[retract](/manual/crud/retract) operations on molecules each execute in their own transaction. By bundling 
transactions statements from several of those operations we can execute a single transaction that will guarantee atomicity. The bundled 
 transaction will either complete as a whole or abort if there are any transactional errors.
 
Each of the above operations has an equivalent method for getting the transaction statements it produces:

- `<molecule>.getSaveTx`  
- `<molecule>.getInsertTx`  
- `<molecule>.getUpdateTx`  
- `<entityId>.getRetractTx`

We can use those methods to build a bundled transaction to atomically perform 4 operations in one transaction:
```
// Some initial data
val List(e1, e2, e3) = Ns.int insert List(1, 2, 3) eids

// Transact multiple molecule statements in one bundled transaction
transact(
  // retract entity
  e1.getRetractTx,
  // save new entity
  Ns.int(4).getSaveTx,
  // insert multiple new entities
  Ns.int.getInsertTx(List(5, 6)),
  // update entity
  Ns(e2).int(20).getUpdateTx
)

// Data after group transaction
Ns.int.get.sorted === List(
  // 1 retracted
  3, // unchanged
  4, // saved
  5, 6, // inserted
  20 // 2 updated
)
```

Bundled transactions can also use Datomic's asynchronous API by calling `transactAsync`:

```
Await.result(
  transactAsync(
    e1.getRetractTx,
    Ns.int(4).getSaveTx,
    Ns.int.getInsertTx(List(5, 6)),
    Ns(e2).int(20).getUpdateTx
  ) map { bundleTx =>
    Ns.int.getAsync map { queryResult => 
      queryResult === List(3, 4, 5, 6, 20)    
    }  
  },
  2.seconds
)
```
### Debugging bundled transactions

If you want to see the transactional output from a bundled transaction you can call `debugTransaction` on some bundled transaction data:


```
// Print debug info for group transaction without affecting live db
debugTransact(
  // retract
  e1.getRetractTx,
  // save
  Ns.int(4).getSaveTx,
  // insert
  Ns.int.getInsertTx(List(5, 6)),
  // update
  Ns(e2).int(20).getUpdateTx
)

// Prints transaction data to output:
/*
  ## 1 ## TxReport
  ========================================================================
  1          ArrayBuffer(
    1          List(
      1          :db.fn/retractEntity   17592186045445)
    2          List(
      1          :db/add       #db/id[:db.part/user -1000247]     :Ns/int          4           Card(1))
    3          List(
      1          :db/add       #db/id[:db.part/user -1000252]     :Ns/int          5           Card(1))
    4          List(
      1          :db/add       #db/id[:db.part/user -1000253]     :Ns/int          6           Card(1))
    5          List(
      1          :db/add       17592186045446                     :Ns/int          20          Card(1)))
  ------------------------------------------------
  2          List(
    1    1     added: true ,   t: 13194139534345,   e: 13194139534345,   a: 50,   v: Wed Nov 14 23:38:15 CET 2018

    2    2     added: false,  -t: 13194139534345,  -e: 17592186045445,  -a: 64,  -v: 1

    3    3     added: true ,   t: 13194139534345,   e: 17592186045450,   a: 64,   v: 4

    4    4     added: true ,   t: 13194139534345,   e: 17592186045451,   a: 64,   v: 5

    5    5     added: true ,   t: 13194139534345,   e: 17592186045452,   a: 64,   v: 6

    6    6     added: true ,   t: 13194139534345,   e: 17592186045446,   a: 64,   v: 20
         7     added: false,  -t: 13194139534345,  -e: 17592186045446,  -a: 64,  -v: 2)
  ========================================================================
*/
```
Two groups of data are shown. The first group is an internal representation in Molecule showing the operations. 
The second group shows the datoms produced in the transaction. For ease of reading, "-" (minus) is prepended
the prefixes (t, e, a, v) for the datoms that are retractions - where `added` is false.  The abbreviations 
represents the parts of the Datom:

- `added`: operation, can be true for asserted or false for retracted
- `t`: transaction entity id
- `e`: entity
- `a`: attribute
- `v`: value

Updating 2 to 20 for instance creates two Datoms, one retracting the old value 2 and one asserting the new value 20.

(The numbers on the left are simply index numbers and not part of the transactional data)


### Next

[Transaction functions...](/manual/transactions/tx-functions)