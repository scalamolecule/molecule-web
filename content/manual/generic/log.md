---
title: "Log"
weight: 30
menu:
  main:
    parent: generic
    identifier: log
up:   /manual/generic
prev: /manual/generic/indexes
next: /manual/generic/schema
down: /manual/debug
---

# Log API

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/generic/Log.scala)

_Some index descriptions in the following sections respectfully quoted from the 
[Datomic documentation](https://docs.datomic.com/on-prem/indexes.html)._

Datomic's database log is a recording of all transaction data in historic order, organized for efficient access by transaction.
The Log is therefore an efficient source of finding data by transaction time.


### Tx range and generic Log attributes

The Molecule Log implementation takes two arguments to define a range of transactions between `from` (inclusive) and
`until` (exclusive). One or more generic attributes are then added to the Log molecule to define what data to return. The 
following standard generic attributes can be combined to match the required data structure:

 - `e` - Entity id (`Long`)
 - `a` - Attribute (`String`)
 - `v` - Value (`Any`)
 - `t` - Transaction point in time (`Long` alternatively `Int`)
 - `tx` - Transaction entity id (`Long`)
 - `txInstant` - Transaction wall-clock time (`java.util.Date`)
 - `op` - Operation: assertion / retraction (`Boolean` true/false)

Contrary to Datomic's Log implementation, Molecule returns data as a flat list of tuples of data that matches
the generic attributes in the Log molecule. This is to transparently sharing the same semantics as other molecules.   

```
// Data from transaction t1 (inclusive) until t4 (exclusive)
Log(Some(t1), Some(t4)).t.e.a.v.op.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 41, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true),

  (t3, e1, ":Person/age", 41, false),
  (t3, e1, ":Person/age", 42, true)
)
``` 

### From beginning

If the `from` argument is `None` data from the beginning of the log is matched:
```
Log(None, Some(t3)).v.e.t.get === List(
  (t1, e1, ":Person/name", "Ben", true),
  (t1, e1, ":Person/age", 41, true),

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true)

  // t3 not included
)
``` 

### Until end

If the `until` argument is `None` data from until the end of the log is matched:
```
Log(Some(t2), None).v.e.t.get === List(
  // t1 not included

  (t2, e2, ":Person/name", "Liz", true),
  (t2, e2, ":Person/age", 37, true),

  (t3, e1, ":Person/age", 41, false),
  (t3, e1, ":Person/age", 42, true)
)
``` 


### Next

[Schema...](/manual/generic/schema)