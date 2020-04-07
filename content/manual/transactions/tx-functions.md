---
title: "Tx functions"
weight: 20
menu:
  main:
    parent: transactions
up:   /manual/transactions
prev: /manual/transactions/tx-bundle
next: /manual/transactions/tx-meta-data
down: /manual/time
---

# Transaction functions

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/transaction/TxFunctions.scala) 




## Atomic processing within the transaction

Transaction functions

- run on the transactor inside of transactions
- can atomically analyze and transform database values
- can perform arbitrary logic
- must have no side effect
- must return transaction data (`Seq[Seq[Statement]]`)

Since tx functions have access to the tx database value they are essential to guaranteeing atomicity in updates for instance. You can
query the current db value within the transaction logic and thus be sure certain assertions hold before doing some operation.

## Using tx functions

Molecule facilitates writing tx functions by annotating one or more objects that contain tx function methods:

```
import molecule.macros.TxFns

@TxFns
object myTxFunctions {

  def myTxFunction1(args...)(implicit conn: Conn): Seq[Seq[Statement]] = {
    ...
  }
  
  def myTxFunction2(args...)(implicit conn: Conn): Seq[Seq[Statement]] = {
    ...
  }
  etc...
}


@TxFns
object myTxFunctionsSomewhereElse {...}
etc...
```
The macro annotation `@TxFns` creates an internal "twin" method at compile time for each Scala tx function that you define. 
The twin method basically adapts to the way Datomic expects tx functions and automatically gets saved into the Datomic 
database transparently without any work on your part.

### Enabling the macro annotation

To use the `@TxFns` macro annotation, you'll need to include the Macro Paradise compiler plugin in your build:
 
```

libraryDependencies ++= Seq(
  ...,
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
)
```

>Macro annotations have been considered experimental in Scala and for version 2.12 requires an 
>import of the scala paradise plugin. In version 2.13 they have been incorporated into the core 
>language itself and importing the paradise plugin will no longer be necessary.


### Tx functions on the transactor classpath

The tx functions also need to be visible to the transactor process meaning that the compiled tx function container 
must be on the classpath of the transactor. This will depend on the Datomic setup you are using:

#### Local dev / in-memory
No need to prepare anything since transaction functions defined within the project
will be available on the classpath for the Transactor managed by the Peer.  

#### Starter pro / pro
Set Datomic classpath variable to where your tx functions are before starting the transactor
```
> cd DATOMIC_HOME
> export DATOMIC_EXT_CLASSPATH=/Users/mg/molecule/molecule/coretests/target/scala-2.12/test-classes/
> bin/transactor ...
```

#### Free 
The Free version can't set the classpath variable so we need to provide the tx functions manually
by making a jar of our classes, move it to the transactor bin folder and start the transactor:
```
> cd ~/molecule/molecule/coretests/target/scala-2.12/test-classes  [path to your compiled classes] 
> jar cvf scala-fns.jar .
> mv ~/molecule/molecule/coretests/target/scala-2.12/test-classes/scala-fns.jar DATOMIC_HOME/bin/
> bin/transactor ...
```


## Tx function example

A typical example of needing access to the tx database value before doing an operation on it could be
to transfer money from one account to another. 

### Enforcing atomic constraints
We want to be sure that there is enough available funds
before we do the transfer. If we had done the lookup of the current balance outside a transaction we
could risk that the balance was updated inbetween our lookup and doing the transfer. With a tx function
we can avoid this by having access to the tx database value _within the transaction itself_. This will give us
the necessary atomicity of the whole transfer. The transfer will only happen if there are enough funds available.

Let's look at a simple tx function implementation:

```
@TxFns
object myTxFunctions {

  // Pass in entity ids of from/to accounts and the amount to be transferred
  def transfer(from: Long, to: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
    // Validate sufficient funds in from-account
    val curFromBalance = Account(from).balance.get.headOption.getOrElse(0)
    if (curFromBalance < amount)
      throw new TxFnException(
        s"Can't transfer $amount from account $from having a balance of only $curFromBalance.")

    // Calculate new balances
    val newFromBalance = curFromBalance - amount
    val newToBalance = Account(to).balance.get.headOption.getOrElse(0) + amount

    // Update accounts
    Account(from).balance(newFromBalance).getUpdateTx ++ Account(to).balance(newToBalance).getUpdateTx
  }
}
```

The signature of a tx functions must include an implicit `Conn` parameter. This makes sure that the
macro annotation can inject a database value in the generated transparent twin function for Datomic.

We first check the available-funds constraint by looking up the current balance and throw an
exception if there is not enough money available. Throwing an exception inside a tx function
will cancel the whole transaction and thereby guarantee atomicity.

Tx functions need to return transaction statements. We use Molecule's tx methods on 
molecules for the equivalent operations to get the necessary statements. So, to get the transaction statements 
of an update we simply replace a normal `update` call too `getUpdateTx` which will give us the transaction
statements produced:

```
// Isolated update transaction (not in tx function)
Account(from).balance(newFromBalance).update

// .. equivalent to the transaction statements returned by `getUpdateTx` 
Account(from).balance(newFromBalance).getUpdateTx
```


### Tx functions have to be pure

Tx functions can't modify the database within the body of the tx method. You couldn't for instance
do an update within the method body. Any operations on the database have to be encoded in the returned
transaction statements.

 
## Invoking tx functions

We call the transaction function inside a `transact` method:
```
transact(transfer(fromAccount, toAccount, okAmount))
```
`transact` is a macro that needs the tx function invocation itself as its argument in order to be able 
to analyze the tx function at compile time.

So our complete example could look like this:

```
// Initial balances
Account(fromAccount).balance.get.head === 100
Account(toAccount).balance.get.head === 700

// Invoke tx function to do the transfer and pass the produced tx statements to `transact`
transact(transfer(fromAccount, toAccount, 20))

// Balances after transfer
Account(fromAccount).balance.get.head === 80
Account(toAccount).balance.get.head === 720
```

### Error handling - ensuring atomicity

```
// Trying to transfer a too big amount will throw an exception 
(transact(transfer(fromAccount, toAccount, 500)) must throwA[TxFnException])
  .message === s"Got the exception molecule.macros.exception.TxFnException: " +
  s"Can't transfer 500 from account $fromAccount having a balance of only 100."
  
// No data has been changed
Account(fromAccount).balance.get.head === 100
Account(toAccount).balance.get.head === 700
```

### Async invocations
Tx functions can also be invoked asynchronously:

```
Await.result(
  transactAsync(transfer(fromAccount, toAccount, 20)) map { txReport =>
    // (for brevity we check the current balances synchronously)
    Account(fromAccount).balance.get.head === 80
    Account(toAccount).balance.get.head === 720
  },
  2.seconds
)
```
For brevity examples show synchronous invocations but could as well have used async invocations.


## Composing tx functions

A tx function can be called from another tx function. We can thus compose more complex tx functions
from "sub tx functions". We could for instance de-compose our previous transfer tx function into
two sub tx functions `withdraw` and `deposit` and then call those from a `transferComposed`
tx function:

```
// "Sub" tx fn - can be used on its own or in other tx functions
def withdraw(from: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
  val curFromBalance = Account(from).balance.get.headOption.getOrElse(0)
  if (curFromBalance < amount)
    throw new TxFnException(s"Can't transfer $amount from account $from having a balance of only $curFromBalance.")

  val newFromBalance = curFromBalance - amount
  Account(from).balance(newFromBalance).getUpdateTx
}


// "Sub" tx fn - can be used on its own or in other tx functions
def deposit(to: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
  val newToBalance = Account(to).balance.get.headOption.getOrElse(0) + amount
  Account(to).balance(newToBalance).getUpdateTx
}


// Compose tx function by calling other tx functions
def transferComposed(from: Long, to: Long, amount: Int)(implicit conn: Conn): Seq[Seq[Statement]] = {
  // This tx function guarantees atomicity when calling multiple sub tx functions.
  // If they were called independently outside a tx function, atomicity wouldn't be guaranteed.
  withdraw(from, amount) ++ deposit(to, amount)
}
```

## Adding tx meta data to tx function invocations

Tx meta data can be added to a tx function invocation by adding one or more tx meta data molecules with
applied tx meta data to the `transact`/`transactAsync` method. Say we want to add meta information about
"who did the transfer" then we can add it to the transaction entity like this:
```
// Add tx meta data that John did the transfer
transact(transfer(fromAccount, toAccount, 20), Person.name("John"))

// We can then query for the transfer that John did
Account(fromAccount).balance.Tx(Person.name_("John")).get.head === 80
Account(toAccount).balance.Tx(Person.name_("John")).get.head === 720
```
Note how the tx meta data applies to both accounts since they were both modified in the same
transaction that the tx meta data was applied to.

We can add arbitrary and possibly unrelated tx meta data to a tx function invocation by applying
two or more tx meta data molecules: 
```
// Add tx meta data that John did the transfer and that it is a scheduled transfer
transact(
  transfer(fromAccount, toAccount, 20), 
  Person.name("John"), 
  UseCase.name("Scheduled transfer"))

// Query multiple Tx meta data molecules
Account(fromAccount).balance
  .Tx(Person.name_("John"))
  .Tx(UseCase.name_("Scheduled transfer")).get.head === 80
Account(toAccount).balance
  .Tx(Person.name_("John"))
  .Tx(UseCase.name_("Scheduled transfer")).get.head === 720
```


## Debugging tx function invocations

If you want to see the `Statement`s produced by a tx function you can invoke it 
within `debugTransact` without affecting the live database:

```
// Print debug info for tx function invocation
debugTransact(transfer(fromAccount, toAccount, 20))

// Prints produced tx statements to output:
/*
## 1 ## TxReport 
========================================================================
1          ArrayBuffer(
  1          List(
    1          :db/add       17592186045445       :Account/balance    80        Card(1))
  2          List(
    1          :db/add       17592186045447       :Account/balance    720       Card(1)))
------------------------------------------------
2          List(
  1    1     added: true ,   t: 13194139534345,   e: 13194139534345,   a: 50,   v: Thu Nov 22 16:23:09 CET 2018

  2    2     added: true ,   t: 13194139534345,   e: 17592186045445,   a: 64,   v: 80
       3     added: false,  -t: 13194139534345,  -e: 17592186045445,  -a: 64,  -v: 100

  3    4     added: true ,   t: 13194139534345,   e: 17592186045447,   a: 64,   v: 720
       5     added: false,  -t: 13194139534345,  -e: 17592186045447,  -a: 64,  -v: 700)
========================================================================
*/
```
Two groups of data are shown. The first group is an internal representation in Molecule showing the operations. 
The second group shows the datoms produced in the transaction. For ease of reading, "-" (minus) is prepended
the prefixes (t, e, a, v) for the datoms that are retractions - where `added` is false. The abbreviations 
represents the parts of the Datom:

- `added`: operation, can be true for asserted or false for retracted
- `t`: transaction entity id
- `e`: entity
- `a`: attribute
- `v`: value  

Updating the from-account balance from 100 to 80 for instance creates two Datoms, 
one retracting the old value 100 and one asserting the new value 80.

(The numbers on the left are simply index numbers and not part of the transactional data)

### Next

[Transaction meta data...](/manual/transactions/tx-meta-data)