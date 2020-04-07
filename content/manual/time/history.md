---
title: "History"
weight: 30
menu:
  main:
    parent: time
    identifier: history
up:   /manual/time
prev: /manual/time/asof-since
next: /manual/time/with
---

# History

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time/GetHistory.scala)


The history perspective gives us all the assertions and retractions that has happened over time.

![](/img/time/history.png)

## Assertions and retractions

Normally we get a snapshot of the database at a certain point in time. But when we call the `getHistory` method 
on a molecule we get all the assertions and retractions that has happened over time for the attributes of the molecule.

As an example we can imagine Fred being added in tx3 and then updated in tx6.

```
// tx 3 (save)
val result3 = Person.name("Fred").likes("pizza").save
val tx3 = result3.tx
val fred = result3.eid

// tx 6 (update)
val result6 = Person(fred).likes("pasta").update
val tx6 = result6.tx
```

The two transactions (save + update) produces the following 4 facts in the database:

![](/img/time/4.png)

## Generic attributes

#### `tx`, `op`

The 4th column in the facts schema above shows the transaction entity id that is saved with each fact. 
We get this value by appending the "generic attribute" `tx` after an attribute.

The 5th column shows the operation performed. `true` for added/asserted and `false` for retracted. We get this value by adding
the generic attribute `op` after an attribute.

Let's see the transaction values and operations over time for the attribute `likes`:

```
Person(fred).likes.tx.op.getHistory.toSeq.sortBy(r => (r._2, r._3)) === List(
  ("pizza", tx3, true), // 2nd fact
  ("pizza", tx6, false),// 3rd fact
  ("pasta", tx6, true)  // 4th fact
)
```
Since output order is not guaranteed by Datomic we sort by transaction and then operation to get a chronological view 
of the historical data (Datomic of course keeps internal order). For brevity we omit the sorting in the following examples.


#### `t`, `txInstant`

Instead of getting the transaction entity id with `tx` we could also get the transaction value (an auto-incremented internal 
number for each transaction) with the generic attribute `t`:

```
Person(fred).likes.t.op.getHistory === List(
  ("pizza", t3, true), 
  ("pizza", t6, false),
  ("pasta", t6, true)  
)
```
.. or the time/date of the transaction with `txInstant`:

```
Person(fred).likes.txInstant.op.getHistory === List(
  ("pizza", date3, true), 
  ("pizza", date6, false),
  ("pasta", date6, true)  
)
```
.. or all at once:

```
Person(fred).likes.tx.t.txInstant.op.getHistory === List(
  ("pizza", tx3, t3, date3, true), 
  ("pizza", tx6, t6, date6, false),
  ("pasta", tx6, t6, date6, true)  
)
```

#### `a`, `v`

We can even use a generic attribute `a` for the attribute name and `v` for the value of an attribute. This allow us to for instance
track changes to all atrributes of an entity:

```
Person(fred).a.v.t.op.getHistory === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true), 
  (":Person/likes", "pizza", t6, false),
  (":Person/likes", "pasta", t6, true)  
)
```

### Expressions

We can apply values to generic attributes in history queries to narrow our results:

```
// "What has been retracted for the entity `fred`"
// - Fred disliked "pizza" at date6
Person(fred).a.v.txInstant.op_(false).getHistory === List(
  (":Person/likes", "pizza", date6, false) 
)

// What happened for Fred in tx 3?
// - Fred's name and liking was asserted
Person(fred).a.v.tx(tx3).op.getHistory === List(
  (":Person/name", "Fred", t3, true), 
  (":Person/likes", "pizza", t3, true)
)
```

## Combining with tx meta data

Things become really interesting when we combine history with tx meta data since we can then go back and see what a 
transaction was about and what changes were involved.

Here some examples from the [Provenance](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Provenance.scala)
examples from the Day of Datomic tutorials:

_"Who created/updated stories?"_

```
Story.url_(ecURL).title.op.tx_(MetaData.usecase.User.firstName).history.get.reverse === List(
  ("ElastiCache in 6 minutes", true, "AddStories", "Stu"),  // Stu adds the story
  ("ElastiCache in 6 minutes", false, "UpdateStory", "Ed"), // retraction automatically added by Datomic
  ("ElastiCache in 5 minutes", true, "UpdateStory", "Ed")   // Ed's update of the title
)
```
And we can narrow with expressions:

_"What did Ed retract and in what use cases?"_
```
Story.url_(ecURL).title.op_(false).tx_(MetaData.usecase.User.firstName_("Ed")).getHistory === List(
  ("ElastiCache in 6 minutes", "UpdateStory") 
)
```


### Next

[With...](/manual/time/with)