---
title: "Transactions"
weight: 80
menu:
  main:
    parent: code
    identifier: transactions
---

# Transactions

All assertions and retractions in Datomic happen within a transaction that guarantees ACID consistency. Along with the domain data involved, Datomic also automatically asserts a timestamp as part of a created Transaction entity for that transaction.

Say that we create a new person with entity id `e5`:
```
val e5 = Person.name("Fred").likes("pizza").save.eid
```

Then the following assertions are made:

![](/img/page/transactions/1.png)

The 4th column of the quintuplets is the entity id of the transaction where the fact was asserted or retracted. In this case the two facts about Fred was asserted in transaction `tx4`. `tx4` is an entity id (`Long`) exactly as the entity id of fred `e5`.

The time of the transaction `date1` (`java.util.Date`) is asserted with the transaction entity id `tx4` as its entity id. And since that timestamp fact is also part of the same transaction `tx4` is also the transaction value (4th column) for that fact.

### Transaction entity id `tx`

Since the transaction is itself an entity exactly as any other entity we can query it as we query our own domain data.

Molecule offers some generic attributes that makes it easy to access transaction data. In our example we could find the transaction entity id of the assertion of Freds `name` by adding the generic attribute `tx` right after the `name_` attribute (that we have made tacit with the underscore since we're not interested in value "Fred"): 

_"In what transaction was Freds name asserted?"_
```
Person(e5).name_.tx.get.head === tx4  // 13194139534340L
```
The `tx` attribute gets the 4th quintuplet value of its preceeding attribute in a molecule. We can see that `name` of entity `e5` (Fred) was asserted in transaction `tx4` since the value `tx4` was saved as the `name` quintuplet's 4th value.

### Transaction value `t`

Alternatively we can get a transaction value `t`

```
Person(e5).name_.t.get.head === t4  // 1028L
```


### Transaction time `txInstant`

With the transaction entity available we can then also get to the value of the timestamp fact of that transaction entity. For convenience Molecule has a generic `txIntstant` to lookup the timestamp which is a `date.util.Date`:

_"When was Freds name asserted?"_
```
Person(e5).name_.txInstant.get.head === date1  // Tue Apr 26 18:35:41
```

### Transaction data per attribute

Since each fact of an entity could have been stated in different transactions, transaction data is always tied to only one attribute. 
 
Say for instance that we assert Fred's `age` in another transaction `tx5`, then we could subsequently get the two transactions involved:

_"Was Fred's name and age asserted in the same transaction?"_
```
// Second transaction
val tx5 = Person(e5).age(38).update.tx

// Retrieve transactions of multiple attributes
Person(e5).name_.tx.age_.tx.get.head === (tx4, tx5)

// No, name and age were asserted in different transactions
tx4 !== tx5
```
Likewise we could ask

_"At what time was Fred's name and age asserted"_
```
Person(e5).name_.txInstant.age_.txInstant.get.head === (date1, date2)

// Fred's name was asserted before his age
date1.before(date2) === true
```

## Transaction bundle

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/transaction/TxBundle.scala)

### Multiple actions in one atomic transaction

[save](/manual/crud/save), [insert](/manual/crud/insert), [update](/manual/crud/update) and [retract](/manual/crud/retract) operations on molecules each execute in their own transaction. By bundling transactions statements from several of those operations we can execute a single transaction that will guarantee atomicity. The bundled transaction will either complete as a whole or abort if there are any transactional errors.

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
Two groups of data are shown. The first group is an internal representation in Molecule showing the operations. The second group shows the datoms produced in the transaction. For ease of reading, "-" (minus) is prepended the prefixes (t, e, a, v) for the datoms that are retractions - where `added` is false.  The abbreviations represents the parts of the Datom:

- `added`: operation, can be true for asserted or false for retracted
- `t`: transaction entity id
- `e`: entity
- `a`: attribute
- `v`: value

Updating 2 to 20 for instance creates two Datoms, one retracting the old value 2 and one asserting the new value 20.

(The numbers on the left are simply index numbers and not part of the transactional data)

## Transaction functions

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/transaction/TxFunctions.scala)




### Atomic processing within the transaction

Transaction functions

- run on the transactor inside of transactions
- can atomically analyze and transform database values
- can perform arbitrary logic
- must have no side effect
- must return transaction data (`Seq[Seq[Statement]]`)

Since tx functions have access to the tx database value they are essential to guaranteeing atomicity in updates for instance. You can query the current db value within the transaction logic and thus be sure certain assertions hold before doing some operation.

### Calling tx functions

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
The macro annotation `@TxFns` creates an internal "twin" method at compile time for each Scala tx function that you define. The twin method basically adapts to the way Datomic expects tx functions and automatically gets saved into the Datomic database transparently without any work on your part.

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

The tx functions also need to be visible to the transactor process meaning that the compiled tx function container must be on the classpath of the transactor. This will depend on the Datomic setup you are using:

#### Local dev / in-memory
No need to prepare anything since transaction functions defined within the project will be available on the classpath for the Transactor managed by the Peer.

#### Starter pro / pro
Set Datomic classpath variable to where your tx functions are before starting the transactor
```
> cd DATOMIC_HOME
> export DATOMIC_EXT_CLASSPATH=/Users/mg/molecule/molecule/coretests/target/scala-2.12/test-classes/
> bin/transactor ...
```

#### Free
The Free version can't set the classpath variable so we need to provide the tx functions manually by making a jar of our classes, move it to the transactor bin folder and start the transactor:
```
> cd ~/molecule/molecule/coretests/target/scala-2.12/test-classes  [path to your compiled classes] 
> jar cvf scala-fns.jar .
> mv ~/molecule/molecule/coretests/target/scala-2.12/test-classes/scala-fns.jar DATOMIC_HOME/bin/
> bin/transactor ...
```


### Tx function example

A typical example of needing access to the tx database value before doing an operation on it could be to transfer money from one account to another.

### Enforcing atomic constraints
We want to be sure that there is enough available funds before we do the transfer. If we had done the lookup of the current balance outside a transaction we could risk that the balance was updated inbetween our lookup and doing the transfer. With a tx function we can avoid this by having access to the tx database value _within the transaction itself_. This will give us the necessary atomicity of the whole transfer. The transfer will only happen if there are enough funds available.

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

The signature of a tx functions must include an implicit `Conn` parameter. This makes sure that the macro annotation can inject a database value in the generated transparent twin function for Datomic.

We first check the available-funds constraint by looking up the current balance and throw an exception if there is not enough money available. Throwing an exception inside a tx function will cancel the whole transaction and thereby guarantee atomicity.

Tx functions need to return transaction statements. We use Molecule's tx methods on molecules for the equivalent operations to get the necessary statements. So, to get the transaction statements of an update we simply replace a normal `update` call too `getUpdateTx` which will give us the transaction statements produced:

```
// Isolated update transaction (not in tx function)
Account(from).balance(newFromBalance).update

// .. equivalent to the transaction statements returned by `getUpdateTx` 
Account(from).balance(newFromBalance).getUpdateTx
```


### Tx functions have to be pure

Tx functions can't modify the database within the body of the tx method. You couldn't for instance do an update within the method body. Any operations on the database have to be encoded in the returned transaction statements.


### Invoking tx functions

We call the transaction function inside a `transact` method:
```
transact(transfer(fromAccount, toAccount, okAmount))
```
`transact` is a macro that needs the tx function invocation itself as its argument in order to be able to analyze the tx function at compile time.

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


### Composing tx functions

A tx function can be called from another tx function. We can thus compose more complex tx functions from "sub tx functions". We could for instance de-compose our previous transfer tx function into two sub tx functions `withdraw` and `deposit` and then call those from a `transferComposed` tx function:

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

### Adding tx meta data to tx function invocations

Tx meta data can be added to a tx function invocation by adding one or more tx meta data molecules with applied tx meta data to the `transact`/`transactAsync` method. Say we want to add meta information about "who did the transfer" then we can add it to the transaction entity like this:
```
// Add tx meta data that John did the transfer
transact(transfer(fromAccount, toAccount, 20), Person.name("John"))

// We can then query for the transfer that John did
Account(fromAccount).balance.Tx(Person.name_("John")).get.head === 80
Account(toAccount).balance.Tx(Person.name_("John")).get.head === 720
```
Note how the tx meta data applies to both accounts since they were both modified in the same transaction that the tx meta data was applied to.

We can add arbitrary and possibly unrelated tx meta data to a tx function invocation by applying two or more tx meta data molecules:
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


### Debugging tx function invocations

If you want to see the `Statement`s produced by a tx function you can invoke it within `debugTransact` without affecting the live database:

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
Two groups of data are shown. The first group is an internal representation in Molecule showing the operations. The second group shows the datoms produced in the transaction. For ease of reading, "-" (minus) is prepended the prefixes (t, e, a, v) for the datoms that are retractions - where `added` is false. The abbreviations represents the parts of the Datom:

- `added`: operation, can be true for asserted or false for retracted
- `t`: transaction entity id
- `e`: entity
- `a`: attribute
- `v`: value

Updating the from-account balance from 100 to 80 for instance creates two Datoms, one retracting the old value 100 and one asserting the new value 80.

(The numbers on the left are simply index numbers and not part of the transactional data)

## Transaction meta data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/transaction/TxMetaData.scala)


As we saw, a [transaction](/manual/transactions/) in Datomic is also an entity with a timestamp fact. Since it's an entity as any of our own entities, we can even add more facts that simply share the entity id of the transaction:

![](/img/page/transactions/2.png)

### Save

Depending on our domain we can tailor any tx meta data that we find valuable to associate with some transactions. We could for instance be interested in "who did it" and "in what use case" it happened and create some generic attributes `user` and `uc` in an `Audit` namespace:

```
trait Audit {
  val user = oneString
  val uc   = oneString
}
```
Then we can assert values of those attributes together with a `save` operation for instance by applying an `Audit` meta molecule

```
Audit.user("Lisa").uc("survey")
```

..to the generic `Tx` namespace:


```
Person.name("Fred").likes("pizza").Tx(Audit.user("Lisa").uc("survey")).save
```
This could read: _"A person Fred liking pizza was saved by Lisa as part of a survey"_


Molecule simply saves the tx meta data attributes `user` and `uc` with the transaction entity id `tx4` as their entity id:

![](/img/page/transactions/3.png)



### Get

Now we can query the tx meta data in various ways:

```
// How was Fred added?
// Fred was added by Lisa as part of a survey
Person(e5).name.Tx(Audit.user.uc).get === List(("Fred", "Lisa", "survey"))

// When did Lisa survey Fred?
Person(e5).name_.txInstant.Tx(Audit.user_("Lisa").uc_("survey")).get.head === dateX
  
// Who were surveyed?  
Person.name.Tx(Audit.uc_("survey")).get === List("Fred")

// What did people that Lisa surveyed like? 
Person.likes.Tx(Audit.user_("Lisa").uc_("survey")).get === List("pizza")

// etc..
```



### Insert

If we insert multiple entities in a transaction, the transaction data is only asserted once:

```
Person.name.likes.Tx(Audit.user_("Lisa").uc_("survey")) insert List(
  ("John", "sushi"),
  ("Pete", "burgers"),
  ("Mona", "snacks")
)
```


### Composites

Similarly we can insert composite molecules composed of sub-molecules/sub-tuples of data - and some tx meta data:

```
Article.name.author + 
  Tag.name.weight
  .Tx(MetaData.submitter_("Brenda Johnson").usecase_("AddArticles")) insert List(
  // 2 rows of data (Articles) 
  // The 2 sub-tuples of each row matches the 2 sub-molecules
  (("Battle of Waterloo", "Ben Bridge"), ("serious", 5)),
  (("Best jokes ever", "John Cleese"), ("fun", 3))
)
```
_"Get serious articles that Brenda submitted"_:
```
m(Article.name.author + 
  Tag.name_("serious").weight.>=(4)
  .Tx(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```



### Update

Transaction meta data can be attached to updates too so that we can for instance follow who changed data in our system.
```
Person(johnId).likes("pasta").Tx(Audit.user_("Ben").uc_("survey-follow-up")).update
```
Now when we look at a list of Persons and what they like we can see that some likes were from an original survey and one is from a follow-up survey that Ben did:

```
Person.name.likes.Tx(Audit.user.uc).get === List(
  ("John", "pasta", "Ben", "survey-follow-up"),
  ("Pete", "burgers", "Lisa", "survey"),
  ("Mona", "snacks", "Lisa", "survey")
)
```


### Retract

It's valuable also to have meta data about retractions so that we can afterwards ask questions like "Who deleted this?".

### Single attribute

To retract an attribute value we apply an empty arg list to the attribute and `update`. Here we also apply some tx meta data about who took away the `likes` value for Pete:
```
Person(peteId).likes().Tx(Audit.user_("Ben").uc_("survey-follow-up")).update
```
We can follow the `likes` of Pete through [history](/manual/time/history/) and see that Ben retracted his `likes` value in a survey follow-up:
```
Person(peteId).likes.t.op.Tx(Audit.user.uc)).getHistory.toSeq.sortBy(r => (r._2, r._3)) === List(
  // Pete's liking was saved by Lisa as part of survey
  ("burgers", 1028, true, "Lisa", "survey"),
  
  // Pete's liking was retracted by Ben in a survey follow-up
  ("burgers", 1030, false, "Ben", "survey-follow-up")
)
```
The entity Pete still exists but now has no current liking:

```
Person(peteId).name.likes$.get.head === ("Pete", None) 
```

### Entities

Using the `retract` method (available via `import molecule.imports._`) we can retract one or more entity ids along with a tx meta data:


```
retract(johnId, Audit.user("Mona").uc("clean-up"))
```
The `Audit.user("Mona").uc("clean-up")` molecule has the tx meta data that we save with the transaction entity.

John has now ben both saved, updated and retracted:

```
Person(johnId).likes.t.op.Tx(Audit.user.uc)).getHistory.toSeq.sortBy(r => (r._2, r._3)) === List(
  // John's liking was saved by Lisa as part of survey
  ("sushi", 1028, true, "Lisa", "survey"), // sushi asserted
  
  // John's liking was updated by Ben in a survey follow-up
  ("sushi", 1030, false, "Ben", "survey-follow-up"), // sushi retracted
  ("pasta", 1030, true, "Ben", "survey-follow-up"), // pasta asserted
  
  // John's liking (actually whole entity) was retracted by Mona in a clean-up
  ("pasta", 1033, false, "Mona", "clean-up") // pasta retracted
)
```

The entity John now currently doesn't exists (although still in history)
```
Person(johnId).name.likes$.get === Nil 
```

