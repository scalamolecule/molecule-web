---
title: "Testing"
weight: 50
menu:
  main:
    parent: time
    identifier: testing
up:   /manual/time
prev: /manual/time/with
next: /manual/generic
down: /manual/generic
---

# Testing

[TestDbAsOf](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/TestDbAsOf.scala), 
[TestDbSince](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/TestDbSince.scala) and 
[TestDbWith](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/TestDbWith.scala)

For more complex test scenarios we can use a "test database" where we can freely make multiple separate molecule queries against
a temporary filtered database.

### Test db

All molecules expect an implicit connection object to be in scope. If we then set a temporary test database on 
such `conn` object we can subsequentially freely perform tests against this temporary filtered database as though 
it was a "branch" (think git).

When the connection/db goes out of scope it is simply garbage collected automatically by the JVM. At any point we can also 
 explicitly go back to continuing using our live production db.
 
To make a few tests with our filtered db we call `conn.testDbAsOfNow`:

```
// Current state
Person(fredId).name.age.get.head === ("Fred", 27)

// Create "branch" of our production db as it is right now
conn.testDbAsOfNow  

// Perform multiple operations on test db
Person(fredId).name("Frederik").update
Person(fredId).age(28).update

// Verify expected outcome of operations
Person(fredId).name.age.get.head === ("Frederik", 28)

// Then go back to production state
conn.useLiveDb

// Production state is unchanged!
Person(fredId).name.age.get.head === ("Fred", 27)
```


### Test db with domain classes

When molecules are used inside domain classes we want to test the domain operations also without
affecting the state of our production database. And also ideally without having to create mockups of our domain objects. 
This is now possible by setting a temporary test database
on the implicit connection object that all molecules expect to be present in their scope - which includes the molecules inside
 domain classes.

When we test against a temporary filtered database, Molecule internally uses the `with` function of Datomic to 
apply transaction data to a filtered database that is simply garbage collected when it 
goes out of scope!

To make a few tests on a domain object that have molecule calls internally we can now do like this:

```
// Some domain object that we want to test
val domainObj = MyDomainClass(params..) // having molecule transactions inside...
domainObj.myState === "some state"

// Create "branch" of our production db as it is right now
conn.testDbAsOfNow  

// Test some domain object operations
domainObj.doThis
domainObj.doThat

// Verify expected outcome of operations
domainObj.myState === "some expected changed state"

// Then go back to production state
conn.useLiveDb

// Production state is unchanged!
domainObj.myState == "some state"
```

Since internal domain methods will in turn call other domain methods that also expects an implicit conn object then
the same test db is even propragated recursively inside the chain of domain operations.


### Multiple time views

We can apply the above approach with various time views of our database:
 
```
conn.testDbAsOfNow
conn.testDbAsOf(t)
conn.testDbSince(t)
conn.testWith(txData)
```

This make it possible to run arbitrarily complex test scenarios directly against our production data at any point in time without
having to do any manual setup or tear-down of mock domain/database objects!