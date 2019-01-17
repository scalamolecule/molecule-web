---
title: "Tx meta data"
weight: 20
menu:
  main:
    parent: transactions
up:   /manual/transactions
prev: /manual/transactions/tx-functions
next: /manual/time
down: /manual/time
---

# Transaction meta data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/transaction/TxMetaData.scala) 


As we saw, a [transaction](/manual/transactions/) in Datomic is also an entity with a timestamp fact. Since it's an entity as
any of our own entities, we can even add more facts that simply share the entity id of the transaction:

![](/img/transactions/2.jpg)

## Save

Depending on our domain we can tailor any tx meta data that we find valuable to associate with some transactions. 
We could for instance be interested in "who did it" and "in what use case" it happened and create some meta attributes
 `user` and `uc` in an `Audit` namespace:

```scala
trait Audit {
  val user = oneString
  val uc   = oneString
}
```
Then we can assert values of those attributes together with a `save` operation for instance by applying an `Audit` meta molecule 

```scala
Audit.user("Lisa").uc("survey")
```

..to the generic `Tx` namespace:


```scala
Person.name("Fred").likes("pizza").Tx(Audit.user("Lisa").uc("survey")).save
```
This could read: _"A person Fred liking pizza was saved by Lisa as part of a survey"_


Molecule simply saves the tx meta data attributes `user` and `uc` with the transaction entity id `tx4` as their entity id:

![](/img/transactions/5.jpg)



## Get

Now we can query the tx meta data in various ways:

```scala
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



## Insert

If we insert multiple entities in a transaction, the transaction data is only asserted once:

```scala
Person.name.likes.Tx(Audit.user_("Lisa").uc_("survey")) insert List(
  ("John", "sushi"),
  ("Pete", "burgers"),
  ("Mona", "snacks")
)
```


### Composites

Similarly we can insert composite molecules composed of sub-molecules/sub-tuples of data - and some tx meta data:

```scala
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
```scala
m(Article.name.author + 
  Tag.name_("serious").weight.>=(4)
  .Tx(MetaData.submitter_("Brenda Johnson"))).get === List(
  (("Battle of Waterloo", "Ben Bridge"), 5)
)
```



## Update

Transaction meta data can be attached to updates too so that we can for instance follow who changed data in our system.
```scala
Person(johnId).likes("pasta").Tx(Audit.user_("Ben").uc_("survey-follow-up")).update
```
Now when we look at a list of Persons and what they like we can see that some likes were from an original survey and one is 
from a follow-up survey that Ben did:

```scala
Person.name.likes.Tx(Audit.user.uc).get === List(
  ("John", "pasta", "Ben", "survey-follow-up"),
  ("Pete", "burgers", "Lisa", "survey"),
  ("Mona", "snacks", "Lisa", "survey")
)
```


## Retract
It's valuable also to have meta data about retractions so that we can afterwards ask questions like "Who deleted this?". 

### Single attribute

To retract an attribute value we apply an empty arg list to the attribute and `update`. Here we also apply some tx meta data
about who took away the `likes` value for Pete:
```scala
Person(peteId).likes().Tx(Audit.user_("Ben").uc_("survey-follow-up")).update
```
We can follow the `likes` of Pete through [history](/manual/time/history/) and see that Ben retracted his `likes` value in a survey follow-up:
```scala
Person(peteId).likes.t.op.Tx(Audit.user.uc)).getHistory.toSeq.sortBy(r => (r._2, r._3)) === List(
  // Pete's liking was saved by Lisa as part of survey
  ("burgers", 1028, true, "Lisa", "survey"),
  
  // Pete's liking was retracted by Ben in a survey follow-up
  ("burgers", 1030, false, "Ben", "survey-follow-up")
)
```
The entity Pete still exists but now has no current liking:

```scala
Person(peteId).name.likes$.get.head === ("Pete", None) 
```

### Entities

Using the `retract` method 
(available via `import molecule.imports._`) we can retract one or more entity ids along with a tx meta data:


```scala
retract(johnId, Audit.user("Mona").uc("clean-up"))
```
The `Audit.user("Mona").uc("clean-up")` molecule has the tx meta data that we save with the transaction entity.
 
John has now ben both saved, updated and retracted:

```scala
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
```scala
Person(johnId).name.likes$.get === Nil 
```


### Next

[Time...](/manual/time)