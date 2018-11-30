---
date: 2015-01-02T22:06:44+01:00
title: "Tx bundle"
weight: 20
menu:
  main:
    parent: transactions
up:   /manual/transactions
prev: /manual/transactions
next: /manual/time
down: /manual/time
---

# Transaction bundle

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/transaction/TxBundle.scala) 

## Multiple actions in one atomic transaction

`save`, `insert`, `update` and `retract` operations on molecules each execute in their own transaction. By bundling 
transacions statements from several operations we can execute a single transaction that will guarantee atomicity. It will either
complete as a whole or abort if there are any transactional errors.

```scala
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

```scala
Await.result(
  transactAsync(
    // retract entity
    e1.getRetractTx,
    // save new entity
    Ns.int(4).getSaveTx,
    // insert multiple new entities
    Ns.int.getInsertTx(List(5, 6)),
    // update entity
    Ns(e2).int(20).getUpdateTx
  ) map { bundleTx =>
    Ns.int.getAsync map { queryResult => 
      queryResult === List(3, 4, 5, 6, 20)    
    }  
  },
  2.seconds
)
```


### Next

[Time...](/manual/time)