---
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

By supplying some test transaction data to `getWith(testTxData)` we filter the current database with
the test transaction data applied in-memory. This is a very powerful way of testing future-like "what-if" scenarios.


![](/img/time/with.png)

We could for instance add some transaction data `tx8` to a Person molecule to see if we would get the extected persons back:

```
Person.name.likes.getWith(<tx8Data>) === ... // Persons after applying tx8 
```

### Applying transaction data

To make it easier to supply transaction data to the `getWith(txData)` method, you can simply add `Tx` to a
Molecule transaction function to get some valid transaction data:

Transaction data is supplied to `getWith(txData)` by calling a transaction data getter on a molecule:

```
Person(fred).likes("sushi").getUpdateTx === List(
  [:db/retract, 17592186045445, :Person/likes, "pasta"]
  [:db/add    , 17592186045445, :Person/likes, "sushi"]
) 
```
`getUpdateTx` returns the transaction data that would have been used to update Fred. In that way we
can supply this data to the `getWith(txData)` method to answer the question _"What if we updated Fred?"_. 

When getting the transaction data from a simulated molecule transaction like this, we call it a "transaction molecule":
```
Person.name.likes.getWith(
  Person(fred).likes("sushi").getUpdateTx // "Transaction molecule" with tx8 data
) === List(
  ("Fred", "sushi") // Expected result if applying tx8
)
```

Fred will remain unaffected in the live database after `getWith(tx8)` has been called:

```
Person.name.likes.get.head === ("Fred", "pasta") 
```
The `getWith(txData)` works on a filtered database and is automatically garbage collected. So there is no need to 
set up and tear down database mockups!


### Transaction molecules

We can generate transaction test data by invoking a transactional data getter on a molecule or in the case of 
retraction on an entity id. The tx getters return the transactional data that the 4 transaction functions 
`save`, `insert`, `update` or `retract` would normally have transacted.

- `<molecule>.getInsertTx`
- `<molecule>.getSaveTx`
- `<molecule>.getUpdateTx`
- `<entityId>.getRetractTx`

Here's an example of combining transaction molecules of all types:

```
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

```
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



### With APIs

Data With some `txTestData` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `txTestData` can be either:

- One or more transaction molecules, each returning `Seq[Seq[Statement]]`
- Raw transaction data from edn file (`java.util.List[_]`)

Combine the needed return type with some transactional data `txTestData` and optionally a row limit 
by calling one of the corresponding `With` implementations. All return type/parameter combinations 
have a synchronous and asynchronous implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-4 column ">
        <ul>
            <li><code>getWith(txTestData)</code> (List)</li>
            <li><code>getArrayWith(txTestData)</code></li>
            <li><code>getIterableWith(txTestData)</code></li>
            <li><code>getJsonWith(txTestData)</code></li>
            <li><code>getRawWith(txTestData)</code></li>
        </ul>
        <ul>
            <li><code>getWith(txTestData, limit)</code> (List)</li>
            <li><code>getArrayWith(txTestData, limit)</code></li>
            <li><code>getJsonWith(txTestData, limit)</code></li>
            <li><code>getRawWith(txTestData, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncWith(txTestData)</code> (List)</li>
            <li><code>getAsyncArrayWith(txTestData)</code></li>
            <li><code>getAsyncIterableWith(txTestData)</code></li>
            <li><code>getAsyncJsonWith(txTestData)</code></li>
            <li><code>getAsyncRawWith(txTestData)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncWith(txTestData, limit)</code> (List)</li>
            <li><code>getAsyncArrayWith(txTestData, limit)</code></li>
            <li><code>getAsyncJsonWith(txTestData, limit)</code></li>
            <li><code>getAsyncRawWith(txTestData, limit)</code></li>
        </ul>
    </div>
</div>


>The asynchronous implementations simply wraps the synchronous result in a Future as any
>other database server would normally do internally. The difference is that the Peer (the "database server") 
>runs in the same process as our application code which makes it natural to do the Future-wrapping
>in Molecule as part of running our application.

### Next

[Testing...](/manual/time/testing/)