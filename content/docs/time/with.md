---
date: 2015-01-02T22:06:44+01:00
title: "With"
weight: 40
menu:
  main:
    parent: time
    identifier: with
up:   /docs/time
prev: /docs/time/history
next: /docs/time/testing
---

# With

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/GetWith.scala)

By supplying some test transaction data to `getWith(testTxData)` we can get a "branch" of the current database with
the test transaction data applied in-memory. This is a very powerful way of testing future-like "what-if" scenarios


![](/img/time/with.png)

We could for instance add some transaction data `tx8` to a Person molecule to see if we would get the extected persons back:

```scala
Person.name.likes.getWith(<tx8Data>) === ... // Persons after applying tx8 
```

### Applying transaction data

To make it easier to supply transaction data to the `getWith(txData)` method, you can simply add `Tx` to a
Molecule transaction function to get some valid transaction data:

```scala
Person(fred).likes("sushi").updateTx === List(
  [:db/retract, 17592186045445, :person/likes, "pasta"]
  [:db/add    , 17592186045445, :person/likes, "sushi"]
) 
```
`updateTx` returns transaction data that we can apply to `getWith(txData)` to answer the question

_"What if we updated Fred?"_
```scala
Person.name.likes.getWith(
  Person(fred).likes("sushi").updateTx // "Transaction molecule" with tx8 data
) === List(
  ("Fred", "sushi") // Expected result if applying tx8
)
```

Fred will remain unaffected in the live database after `getWith(tx8)` has been called:

```scala
Person.name.likes.get.head === ("Fred", "pasta") 
```


### Transaction molecules

We can create transaction data by appending `Tx` to any Molecule transaction function. We call molecules used
this way "transaction molecules" and we can apply multiple to the `getWith(txData)` function:

```scala
Person.name.age.getWith(
  // Transaction molecules:
  Person.name("John").age(44).saveTx, // John saved
  Person.name.age insertTx List(
    ("Lisa", 23),                     // Lisa and Pete inserted
    ("Pete", 24)
  ),
  Person(fred).age(43).updateTx,      // Fred updated
  someOtherPerson.retractTx           // Some other person retracted (using id)    
) === List(
  // Expected result
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24), // Inserted
  ("Fred", 43)  // Updated
  // (other person retracted)
)
```
This allow us to test any complexity of transactions.


### Modularizing tx data

Saving transaction molecules in variables can help us modularize tests where we could for instance
be interested in seeing if the various orders of transactions will produce the same result:

```scala
val save1    = Person.name("John").age(44).saveTx
val insert2  = Person.name.age insertTx List(("Lisa", 23), ("Pete", 24))
val update1  = Person(fred).age(43).updateTx
val retract1 = someOtherPerson.retractTx
    
val expectedResult = List(
    ("John", 44),
    ("Lisa", 23),
    ("Pete", 24),
    ("Fred", 43) 
)     
    
Person.name.age.getWith(save1, insert2, update1, retract1) === expectedResult 
Person.name.age.getWith(insert2, update1, retract1, save1) === expectedResult
// etc..
```
Since you can apply any number of transaction molecules the testing options are extremely powerful.

### Next

[Testing...](/docs/time/testing/)