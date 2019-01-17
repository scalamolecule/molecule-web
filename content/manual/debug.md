---
title: "Debug"
weight: 110
menu:
  main:
    parent: manual
    identifier: debug
up:   /manual/generic
prev: /manual/generic/schema
next: /manual/debug/datalog
---

# Debug molecules

See [debug docs](http://www.scalamolecule.org/api/molecule/api/ShowDebug.html)...
<br><br>

3 resources helping debugging molecules:

### [Show Datalog queries and data](/manual/debug/datalog)

All molecule query commands have a corresponding debug command that will print debugging information to console.

 - &lt;molecule&gt;.`debugGet`
 - &lt;molecule&gt;.`debugGetHistory`
 - &lt;molecule&gt;.`debugGetAsOf(...)`
 - &lt;molecule&gt;.`debugGetSince(...)`
 - &lt;molecule&gt;.`debugGetWith(...)`
 

### [Debug transactions](/manual/debug/debug-transactions)

Transactional operations can be debugged with the following methods:

 - &lt;molecule&gt;.`debugSave`
 - &lt;molecule&gt;.`debugInsert(data...)`
 - &lt;molecule&gt;.`debugUpdate`
 - &lt;entityId&gt;.`debugRetract`
 - &lt;entityId&gt;.Tx(transactionMolecule).`debugRetract`
 - `debugRetract(entityIds, txMetaDataMolecules*)` 


### [Common errors and solutions](/manual/debug/err)

List of possible compilation/runtime errors and solutions...


### Next

[Debug Datalog/data...](/manual/debug/datalog)