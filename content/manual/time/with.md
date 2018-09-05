---
date: 2015-01-02T22:06:44+01:00
title: "With"
weight: 40
menu:
  main:
    parent: time
    identifier: with
up:   /manual/time
prev: /manual/time/history
next: /manual/time/testing
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

Transaction data is supplied to `getWith(txData)` by calling a transaction data getter on a molecule:

```scala
Person(fred).likes("sushi").getUpdateTx === List(
  [:db/retract, 17592186045445, :person/likes, "pasta"]
  [:db/add    , 17592186045445, :person/likes, "sushi"]
) 
```
`getUpdateTx` returns the transaction data that would have been used to update Fred. In that way we
can supply this data to the `getWith(txData)` method to answer the question _"What if we updated Fred?"_. 

When getting the transaction data from a simulated molecule transaction like this, we call it a "transaction molecule":
```scala
Person.name.likes.getWith(
  Person(fred).likes("sushi").getUpdateTx // "Transaction molecule" with tx8 data
) === List(
  ("Fred", "sushi") // Expected result if applying tx8
)
```

Fred will remain unaffected in the live database after `getWith(tx8)` has been called:

```scala
Person.name.likes.get.head === ("Fred", "pasta") 
```
The `getWith(txData)` works on a "branch" of the database and is automatically garbage collected. So there is no need to 
set up and tear down database mockups!


### 4 transaction data getters

We can generate transaction data by simulating any of the 4 transaction functions we have available in Molecule:

- `getInsertTx`
- `getSaveTx`
- `getUpdateTx`
- `getRetractTx`

```scala
Person.name.age.getWith(
  // Transaction molecules:
  Person.name("John").age(44).getSaveTx, // John saved
  Person.name.age getInsertTx List(   // Get insert tx with supplied data
    ("Lisa", 23),                     // Lisa and Pete inserted
    ("Pete", 24)
  ),
  Person(fred).age(43).getUpdateTx,   // Fred updated
  someOtherPersonId.getRetractTx      // Some other person retracted (using id)    
) === List(
  // Expected result
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24), // Inserted
  ("Fred", 43)  // Updated
  // (other person retracted)
)
```
This allow us to test any transactions and build complex "what-if" test scenarios without affecting our
live database. 


### Modularizing tx data

Assigning transaction molecules to variables can help us modularize tests where we could for instance
be interested in seeing if various orders of transactions will produce the same result:

```scala
val save    = Person.name("John").age(44).getSaveTx
val insert  = Person.name.age getInsertTx List(("Lisa", 23), ("Pete", 24))
val update  = Person(fred).age(43).getUpdateTx
val retract = someOtherPersonId.getRetractTx
    
val expectedResult = List(
    ("John", 44),
    ("Lisa", 23),
    ("Pete", 24),
    ("Fred", 43) 
)     
    
Person.name.age.getWith(save, insert, update, retract) === expectedResult 
Person.name.age.getWith(insert, update, retract, save) === expectedResult
// etc..
```
Since you can apply any number of transaction molecules, the testing options are extremely powerful.

### Next

[Testing...](/manual/time/testing/)