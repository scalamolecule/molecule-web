---
title: "Aggregates"
weight: 50
menu:
  main:
    parent: attributes
up: /manual/attributes
prev: /manual/attributes/expressions
next: /manual/attributes/parameterized
down: /manual/entities
---

# Aggregates

[Core tests](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/expression/Aggregates.scala) | 
[Example tests](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/Aggregates.scala)

Molecule wraps Datomic's native aggregate functions by applying special aggregate keyword objects to the attribute we want to 
aggregate on. The keyword objects are made available with the `molecule._` import.

Aggregate functions either return a single value or a collection of values:


## Aggregates returning a single value

#### min/max
Applying the `min` or `max` aggregate keyword object as a value to the `age` attribute returns the lowest/highest ages.
```scala
Person.age(min) // lowest age
Person.age(max) // highest age
```
`min`/`max` supports all attribute types.


#### sum

```scala
Person.age(sum) // sum of all ages
```


#### count

Not to be confused with `sum`. `count` counts the total number of entities with an asserted `age` value
```scala
Person.age(count) // count of all persons with an age (not the sum of ages)
```

#### countDistinct

Not to be confused with `sum`. `count` counts the total number of entities with asserted _unique_ `age` values
```scala
Person.age(countDistinct)  // count of unique ages
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


## Aggregates returning collections of values

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
Person.age(rand(3)) // 3 random persons (with potential for duplicates)
```

#### sample(n)

```scala
Person.age(sample(3)) // 3 sample persons (without duplicates)
```



### Next

[Parameterized attributes...](/manual/attributes/parameterized)