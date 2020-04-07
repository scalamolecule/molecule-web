---
title: "AsOf/Since"
weight: 10
menu:
  main:
    parent: time
    identifier: asof-since
up:   /manual/time
prev: /manual/time
next: /manual/time/history
---

# AsOf / Since

[Tests...](https://github.com/scalamolecule/molecule/tree/master/coretests/src/test/scala/molecule/coretests/time)

`getAsOf(t)` and `getSince` are complementary functions that either get us a snapshop of the database at some
point in time or a current snapshot filtered with only changes after a point in time. Like before/after scenarios. 


## AsOf

[AsOf test...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetAsOf.scala)

Calling `getAsOf(t)` on a molecule gives us the data as of a certain point in time like `t4`:

![](/img/time/as-of.png)

<br>

As we saw in [point in time](/manual/time#pointintime), a `t` can be either a transaction entity
id like `txE4`, a transaction number `t4`, the resulting transaction report `tx4` from some transactional 
operation or a `java.util.Date` like `date4`. So we could get to the same data in 4 different ways:

```
Person.name.age.getAsOf(txE4) === ... // Persons as of transaction entity id `txE4` (inclusive)
 
Person.name.age.getAsOf(t4) === ... // Persons as of transaction value `t4` (inclusive) 

Person.name.age.getAsOf(tx4) === ... // Persons as of transaction report `tx4` (inclusive) 

Person.name.age.getAsOf(date4) === ... // Persons as of some Date `date4` (inclusive) 
```

Note that `t` is "inclusive" meaning that it is how the database looked right _after_ transaction
`txE4`/`t4`/`tx4`/`date4`.

### AsOf APIs

Data AsOf some point in time `t` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `t` can be any of:

- Transaction entity id (`Long`)
- Transaction number (`Long`)
- Transaction report (`molecule.facade.TxReport`)
- Date (`java.util.Date`)

Combine the needed return type with some representation of `t` and optionally a row limit by calling one of the 
corresponding `AsOf` implementations. All return type/parameter combinations have a synchronous and asynchronous 
implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-3 column ">
        <ul>
            <li><code>getAsOf(t)</code> (List)</li>
            <li><code>getArrayAsOf(t)</code></li>
            <li><code>getIterableAsOf(t)</code></li>
            <li><code>getJsonAsOf(t)</code></li>
            <li><code>getRawAsOf(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsOf(t, limit)</code> (List)</li>
            <li><code>getArrayAsOf(t, limit)</code></li>
            <li><code>getJsonAsOf(t, limit)</code></li>
            <li><code>getRawAsOf(t, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncAsOf(t)</code> (List)</li>
            <li><code>getAsyncArrayAsOf(t)</code></li>
            <li><code>getAsyncIterableAsOf(t)</code></li>
            <li><code>getAsyncJsonAsOf(t)</code></li>
            <li><code>getAsyncRawAsOf(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncAsOf(t, limit)</code> (List)</li>
            <li><code>getAsyncArrayAsOf(t, limit)</code></li>
            <li><code>getAsyncJsonAsOf(t, limit)</code></li>
            <li><code>getAsyncRawAsOf(t, limit)</code></li>
        </ul>
    </div>
</div>

`getIterableAsOf(t, limit)` and `getAsyncIterableAsOf(t, limit)` are not implemented since the data is 
lazily evaluated with calls to `next` on the `Iterator`.  


## Since
[Since tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/time/GetSince.scala)

As a complementary function to `getAsOf(t)` we have `getSince(t)` that gives us a snapshot of 
the current database filtered with only changes added _after/since_ `t`:

![](/img/time/since.png)

<br>

Contrary to the getAsOf(t) method, the `t` is _not_ included in `getSince(t)`.

`t` can be either a transaction entity id like `txE4`, a transaction number `t4`, the resulting transaction 
report `tx4` from some transactional operation or a `java.util.Date` like `date4`. So we could get to the same data 
in 4 different ways:

```
Person.name.age.getSince(txE4) === ... // Persons added since/after transaction entity id `txE4` (exclusive)
 
Person.name.age.getSince(t4) === ... // Persons added since/after transaction value `t4` (exclusive) 

Person.name.age.getSince(tx4) === ... // Persons added since/after transaction report `tx4` (exclusive)

Person.name.age.getSince(date4) === ... // Persons added since/after some Date `date4` (exclusive) 
```




### Since APIs

Data Since some point in time `t` can be returned as

- `List` for convenient access to smaller data sets
- `Array` for fastest retrieval and traversing of large typed data sets
- `Iterable` for lazy traversing with an Iterator
- Json (`String`)
- Raw (`java.util.Collection[java.util.List[AnyRef]]`) for fast access to untyped data

where `t` can be any of:

- Transaction entity id (`Long`)
- Transaction number (`Long`)
- Transaction report (`molecule.facade.TxReport`)
- Date (`java.util.Date`)

Combine the needed return type with some representation of `t` and optionally a row limit by calling one of the 
corresponding `Since` implementations. All return type/parameter combinations have a synchronous and asynchronous 
implementation:

<div class="container" style="margin-left: -30px">
    <div class="col-sm-3 column ">
        <ul>
            <li><code>getSince(t)</code> (List)</li>
            <li><code>getArraySince(t)</code></li>
            <li><code>getIterableSince(t)</code></li>
            <li><code>getJsonSince(t)</code></li>
            <li><code>getRawSince(t)</code></li>
        </ul>
        <ul>
            <li><code>getSince(t, limit)</code> (List)</li>
            <li><code>getArraySince(t, limit)</code></li>
            <li><code>getJsonSince(t, limit)</code></li>
            <li><code>getRawSince(t, limit)</code></li>
        </ul>
    </div>
    <div class="col-sm-5 column ">
        <ul>
            <li><code>getAsyncSince(t)</code> (List)</li>
            <li><code>getAsyncArraySince(t)</code></li>
            <li><code>getAsyncIterableSince(t)</code></li>
            <li><code>getAsyncJsonSince(t)</code></li>
            <li><code>getAsyncRawSince(t)</code></li>
        </ul>
        <ul>
            <li><code>getAsyncSince(t, limit)</code> (List)</li>
            <li><code>getAsyncArraySince(t, limit)</code></li>
            <li><code>getAsyncJsonSince(t, limit)</code></li>
            <li><code>getAsyncRawSince(t, limit)</code></li>
        </ul>
    </div>
</div>


`getIterableSince(t, limit)` and `getAsyncIterableSince(t, limit)` are not implemented since the data is 
lazily evaluated with calls to `next` on the `Iterator`.

>The asynchronous implementations simply wraps the synchronous result in a Future as any
>other database server would normally do internally. The difference is that the Peer (the "database server") 
>runs in the same process as our application code which makes it natural to do the Future-wrapping
>in Molecule as part of running our application.

### Next

[History...](/manual/time/history)