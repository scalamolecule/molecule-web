---
date: 2015-01-02T22:06:44+01:00
title: "Aggregates"
weight: 40
menu:
  main:
    parent: query
---

# Aggregates

(See [aggregates tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala))

Datomic offers some built-in aggregate functions to aggregate attribute values. 

### Aggregates returning a single value

#### min/max
In Molecule you simply apply the aggregate function name as a keyword to your attribute.
```scala
Person.age(min) // lowest age
Person.age(max) // highest age
```
Supports all types.

#### count

Not to be confused with `sum` in that `count` counts the entities having attribute with some value
```scala
Person.age(count) // count of all persons with an age (not the sum of ages)
```

#### countDistinct

Not to be confused with `sum` in that `count` counts the entities having attribute with some value
```scala
Person.age(countDistinct)  // count of unique ages
```

#### sum

```scala
Person.age(sum) // sum of all ages
```

#### avg

```scala
Person.age(avg) // average of all ages
```

#### median

```scala
Person.age(median) // median of all ages
```

#### variance

```scala
Person.age(variance) // variance of all ages
```

#### stddev

```scala
Person.age(stddev) // standard deviation of all ages
```


### Aggregates returning collections of values

#### distinct

```scala
Person.age(distinct) // distinct ages
```

#### min(n)

```scala
Person.age(min(3)) // 3 lowest ages
```

#### max(n)

```scala
Person.age(max(3)) // 3 highest ages
```

#### rand(n)

```scala
Person.age(rand(3)) // 3 random persons (with potential for duplicates!)
```

#### sample(n)

```scala
Person.age(sample(3)) // 3 sample persons (without duplicates!)
```
