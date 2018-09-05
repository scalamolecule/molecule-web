---
date: 2015-01-02T22:06:44+01:00
title: "AsOf/Since"
weight: 10
menu:
  main:
    parent: time
    identifier: asof-since
up:   /docs/time
prev: /docs/time
next: /docs/time/history
---

# AsOf / Since

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time)

`getAsOf(t)` and `getSince` are complementary functions that either get us a snapshop of the database at some
point in time or a current snapshot filtered with only changes after a point in time. Like before/after scenarios. 


## AsOf

[Temp test](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetAsOf.scala)

Calling `getAsOf(t)` on a molecule gives us the data as of a certain point in time like `t4`:

![](/img/time/as-of.png)

<br>

As we saw in [point in time](/docs/time#pointintime), a `t` can be either a transaction entity
id like `tx4`, a transaction number `t4` or a `java.util.Date` like `date4`. So we could get to 
the same data in 3 different ways:

```scala
Person.name.age.getAsOf(tx4) === ... // Persons as of transaction entity id `tx4` (inclusive)
 
Person.name.age.getAsOf(t4) === ... // Persons as of transaction value `t4` (inclusive) 

Person.name.age.getAsOf(date4) === ... // Persons as of some Date `date4` (inclusive) 
```

Note that `t` is "inclusive" meaning that it is how the database looked right _after_ transaction
`tx4`, `t4` or `date4`.


## Since
[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetSince.scala)

As a complementary function to `getAsOf(t)` we have `getSince(t)` that gives us a snapshot of 
the current database filtered with only changes added _after/since_ `t`:

![](/img/time/since.png)

<br>

In this case the `t` is not included 


```scala
Person.name.age.getSince(tx4) === ... // Persons added since/after transaction entity id `tx4` (exclusive)
 
Person.name.age.getSince(t4) === ... // Persons added since/after transaction value `t4` (exclusive) 

Person.name.age.getSince(date4) === ... // Persons added since/after some Date `date4` (exclusive) 
```



### Next

[History...](/docs/time/history)