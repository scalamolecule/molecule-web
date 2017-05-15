---
date: 2015-01-02T22:06:44+01:00
title: "Tx meta data"
weight: 70
menu:
  main:
    parent: query
---

# Transaction meta data

Transaction data in Datomic is basically a list of facts/datoms being asserted or retracted. If we take the 
[Provenance](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Provenance.scala)
example we could add two Story entities each with two datoms (`title` and `url`) with this molecule:

```scala
Story.title.url insert List(
  ("ElastiCache in 6 minutes", "blog.com/elasticache-in-5-minutes.html"),
  ("Keep Chocolate Love Atomic", "blog.com/atomic-chocolate.html")
)
```
Molecule translate this to a transaction list of 4 datoms:
```
List(
  //  operation            entity id               attribute             value  
  List(:db/add,  #db/id[:db.part/user -1000001],  :story/title,  ElastiCache in 6 minutes              ),
  List(:db/add,  #db/id[:db.part/user -1000001],  :story/url  ,  blog.com/elasticache-in-5-minutes.html),
  List(:db/add,  #db/id[:db.part/user -1000002],  :story/title,  Keep Chocolate Love Atomic            ),
  List(:db/add,  #db/id[:db.part/user -1000002],  :story/url  ,  blog.com/atomic-chocolate.html        )
)
```
Each Datom has 4 - and soon 5 - pieces of information:

- Entity id
- Attribute
- Value
- Tx
- Operation (add/retract)

Molecule simply passes our list to be transacted by Datomic. Datomic then creates a transaction entity and makes
 the association between all the datoms and this transaction id (the Tx part).


## "Annotating" a transaction

If we want to "annotate" a transaction with some meta data like "who did it?" or "what use case?" etc, we could 
simply add two more datoms to our list with this meta data. We don't want to repeatedly add the meta data with
all the rows of data, so we use the special `tx_` attribute that is available to all molecules and apply another
Molecule populated with our meta data. So now our "tx-annotated" molecule looks like this:

```scala
Story.title.url.tx_(MetaData.user_(stu).usecase_("AddStories")) insert List(
  ("ElastiCache in 6 minutes", "blog.com/elasticache-in-5-minutes.html"),
  ("Keep Chocolate Love Atomic", "blog.com/atomic-chocolate.html")
)
```
The insert data now includes two more datoms that are saved as part of the transaction entity:
```
List(
  //  operation            entity id               attribute             value  
  List(:db/add,  #db/id[:db.part/user -1000001],  :story/title,      ElastiCache in 6 minutes              ),
  List(:db/add,  #db/id[:db.part/user -1000001],  :story/url  ,      blog.com/elasticache-in-5-minutes.html),
  List(:db/add,  #db/id[:db.part/user -1000002],  :story/title,      Keep Chocolate Love Atomic            ),
  List(:db/add,  #db/id[:db.part/user -1000002],  :story/url  ,      blog.com/atomic-chocolate.html        ),
  List(:db/add,  #db/id[:db.part/tx -1000049],    :metaData/user   , 17592186045423                        ),
  List(:db/add,  #db/id[:db.part/tx -1000049],    :metaData/usecase, AddStories                            )
)
```


## Querying transaction meta data

Now we can find stories based on our transaction meta data knowledge:

```scala
// Stories that Stu added (first meta information used)
Story.title.tx_(MetaData.user_(stu)).get === List(
  "Keep Chocolate Love Atomic",
  "ElastiCache in 6 minutes"
)

// Stories that were added with the AddStories use case (second meta information used)
Story.title.tx_(MetaData.usecase_("AddStories")).get === List(
  "Keep Chocolate Love Atomic",
  "ElastiCache in 6 minutes"
)

// Stories that Stu added with the AddStories use case (both meta data used)
Story.title.tx_(MetaData.user_(stu).usecase_("AddStories")).get === List(
  "Keep Chocolate Love Atomic",
  "ElastiCache in 6 minutes"
)

// Stories and transactions where Stu added stories (`tx` is returned)
Story.title.tx(MetaData.user_(stu).usecase_("AddStories")).get === List(
  ("ElastiCache in 6 minutes", stuTxId),
  ("Keep Chocolate Love Atomic", stuTxId)
)

// Stories and names of who added them (Note that we can have referenced meta data!)
Story.title.tx_(MetaData.User.firstName.lastName).get === List(
  ("ElastiCache in 6 minutes", "Stu", "Halloway"),
  ("Keep Chocolate Love Atomic", "Stu", "Halloway")
)

// Stories added by a user named "Stu"
Story.title.tx_(MetaData.User.firstName_("Stu")).get === List(
  "ElastiCache in 6 minutes",
  "Keep Chocolate Love Atomic"
)

// Stories added by a user with email "stuarthalloway@datomic.com"
Story.title.tx_(MetaData.User.email_("stuarthalloway@datomic.com")).get === List(
  "ElastiCache in 6 minutes",
  "Keep Chocolate Love Atomic"
)

// Count of stories added by a user with email "stuarthalloway@datomic.com"
Story.title(count).tx_(MetaData.User.email_("stuarthalloway@datomic.com")).get.head === 2

// Emails of users who added stories
Story.title_.tx_(MetaData.usecase_("AddStories").User.email).get === List(
  "stuarthalloway@datomic.com"
)
```

## Transaction history

We can also annotate updates like this:

```scala
Story(elasticacheStory).title("ElastiCache in 5 minutes")
  .tx_(MetaData.user(ed).usecase_("UpdateStory")).update
```

We can then use the `history` of the database in combination with out meta data to follow who 
created/updated:

```scala
Story.url_(ecURL).title.op.tx_(MetaData.usecase.User.firstName).history.get.reverse === List(
  ("ElastiCache in 6 minutes", true, "AddStories", "Stu"),  // Stu adds the story
  ("ElastiCache in 6 minutes", false, "UpdateStory", "Ed"), // retraction automatically added by Datomic
  ("ElastiCache in 5 minutes", true, "UpdateStory", "Ed")   // Ed's update of the title
)
```
When a new fact is asserted for a cardinality-one attribute that already has a value, Datomic
automatically makes an extra datom with a retraction (`op false`) to "cancel" the old value and
then adds our assertion datom.

The current database is the latest snapshot of the history, so the ElastiCache now has the correct
title. We can therefore see if stories have been edited:

```scala
// Stories with latest use case meta date
Story.title.tx_(MetaData.usecase).get === List(
  ("Keep Chocolate Love Atomic", "AddStories"),
  ("ElastiCache in 5 minutes", "UpdateStory")
)
```

And we can even look for data without certain transaction meta data
```scala
// Stories without use case meta data
Story.title.tx_(MetaData.usecase_(nil)).get === List(
  "Clojure Rationale",
  "Beating the Averages",
  "Teach Yourself Programming in Ten Years"
)
```

See also [core tests...](https://github.com/scalamolecule/molecule/blob/master/coretest/src/test/scala/molecule/transaction/TransactionMetaData.scala)