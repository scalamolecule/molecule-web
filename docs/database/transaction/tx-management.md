
# Transaction management

Molecule provides powerful transaction management capabilities for composing multiple operations, handling errors, and controlling transaction boundaries with precision.

## Basic Transactions

The simplest way to execute a transaction is with `.transact`:

```scala
// Single operation
Person.name("John").age(42).save.transact

// Multiple operations in one transaction
transact(
  Person.name("Alice").age(30).save,
  Person.name("Bob").age(35).save,
)
```

If any operation fails, the entire transaction is rolled back.


## Mixed Operations

You can mix different types of operations in a single transaction:

```scala
transact(
  Person.name("Alice").age(30).save,      // Save
  Person.name.age.insert(List(            // Insert
    ("Bob", 35),
    ("Carol", 28)
  )),
  Person(aliceId).delete,                 // Delete
  Person(bobId).age(36).update,           // Update
)
```

All operations succeed or fail together, maintaining data consistency.


## UnitOfWork

Use `unitOfWork` when you need to compose multiple transactions with queries in between:

```scala
unitOfWork {
  Person.name("Alice").age(30).save.transact
  val aliceId = Person.name_("Alice").id.query.get.head

  Person.name("Bob").age(35).save.transact
  val bobId = Person.name_("Bob").id.query.get.head

  // Use the IDs for further operations
  Address.street("Main St").person(aliceId).save.transact
}
```

If an exception is thrown anywhere within the `unitOfWork` block, all transactions are rolled back:

```scala
Person.name("Alice").age(30).save.transact

try {
  unitOfWork {
    Person.name("Bob").age(35).save.transact
    Person.name.query.get // List("Alice", "Bob")

    throw new Exception("Something went wrong")
  }
} catch {
  case e: Exception => // Handle error
}

Person.name.query.get // List("Alice") - Bob was rolled back
```


## Savepoints

Savepoints allow fine-grained control over rollbacks within a transaction. Use them to create rollback points without aborting the entire transaction:

```scala
unitOfWork {
  Person.name("Alice").age(30).save.transact

  savepoint { sp =>
    Person.name("Bob").age(35).save.transact
    Person.name.query.get // List("Alice", "Bob")

    // Roll back just the savepoint
    sp.rollback()
    Person.name.query.get // List("Alice")
  }

  // Alice's data is still committed
  Person.name.query.get // List("Alice")
}
```


### Throwing Exceptions in Savepoints

Throwing an exception inside a savepoint automatically rolls back operations within that savepoint:

```scala
unitOfWork {
  Person.name("Alice").age(30).save.transact

  try {
    savepoint { _ =>
      Person.name("Bob").age(35).save.transact

      // Automatically rolls back this savepoint
      throw new Exception("Error in savepoint")
    }
  } catch {
    case e: Exception => // Handle error
  }

  // Alice remains, Bob was rolled back
  Person.name.query.get // List("Alice")
}
```


## Nested Savepoints

Savepoints can be nested to any depth, allowing precise control over transaction boundaries:

```scala
unitOfWork {
  savepoint { outer =>
    Person.name("Alice").age(30).save.transact

    savepoint { middle =>
      Person.name("Bob").age(35).save.transact

      savepoint { inner =>
        Person.name("Carol").age(28).save.transact

        // Roll back only Carol
        inner.rollback()
      }
      // Alice and Bob remain
    }
  }
}

Person.name.a1.query.get // List("Alice", "Bob")
```

You can also roll back an outer savepoint from within an inner one:

```scala
unitOfWork {
  savepoint { outer =>
    Person.name("Alice").age(30).save.transact

    savepoint { inner =>
      Person.name("Bob").age(35).save.transact

      // Roll back outer savepoint (includes both Alice and Bob)
      outer.rollback()
    }
  }
}

Person.name.query.get // List() - both rolled back
```


## Practical Example: Money Transfer

Here's a real-world example showing how to use `unitOfWork` for a safe money transfer:

```scala
def transfer(from: String, to: String, amount: Int)
            (using conn: Conn): Unit = {
  unitOfWork {
    // Debit from account
    Account.name_(from).balance.-(amount).update.transact

    // Credit to account
    Account.name_(to).balance.+(amount).update.transact

    // Verify sufficient funds
    val balance = Account.name_(from).balance.query.get.head
    if (balance < 0) {
      // Abort entire transaction
      throw new Exception("Insufficient funds")
    }
  }
}

// Initial balances
Account.name("Alice").balance(100).save.transact
Account.name("Bob").balance(50).save.transact

try {
  transfer("Alice", "Bob", 150) // Would overdraw Alice
} catch {
  case e: Exception =>
    println(e.getMessage) // "Insufficient funds"
}

// Balances unchanged
Account.name.balance.query.get
// List(("Alice", 100), ("Bob", 50))
```


## Validation in Transactions

Validation errors automatically roll back the entire transaction:

```scala
try {
  transact(
    Person.age(25).save,      // Valid
    Person.age(15).save,      // Invalid (age must be >= 18)
  )
} catch {
  case ValidationErrors(errorMap) =>
    // Handle validation errors
}

// No data saved
Person.age.query.get // List()
```


## Key Points

- **Basic `.transact`** - For single operations or multiple operations without intermediate queries
- **`transact(...)`** - For grouping multiple operations in one transaction
- **`unitOfWork`** - For composing transactions with queries in between
- **`savepoint`** - For creating rollback points within a transaction
- **Nested savepoints** - For fine-grained control over complex workflows
- **Exception handling** - Exceptions automatically roll back transactions/savepoints
- **Validation** - Validation errors roll back entire transactions


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Transaction management compliance tests](https://github.com/scalamolecule/molecule/blob/main/db/compliance/jvm/src/test/scala/molecule/db/compliance/test/transaction/Transactions_sync.scala)