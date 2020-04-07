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
aggregate on.

Aggregate functions either return a single value or a collection of values:


## Aggregates returning a single value

#### min/max
Applying the `min` or `max` aggregate keyword object as a value to the `age` attribute returns the lowest/highest ages.
```
Person.age(min) // lowest age
Person.age(max) // highest age
```
`min`/`max` supports all attribute types.


#### sum

```
Person.age(sum) // sum of all ages
```


#### count

Count the total number of entities with an asserted `age` value (not to be confused with `sum`).
```
Person.age(count) // count of all persons with an age (not the sum of ages)
```

#### countDistinct

Count the total number of entities with asserted _unique_ `age` values (not to be confused with `sum`).
```
Person.age(countDistinct)  // count of unique ages
```

#### avg

```
Person.age(avg) // average of all ages
```

#### median

```
Person.age(median) // median of all ages
```

#### variance

```
Person.age(variance) // variance of all ages
```

#### stddev

```
Person.age(stddev) // standard deviation of all ages
```


## Aggregates returning collections of values

#### distinct

```
Person.age(distinct) // distinct ages
```

#### min(n)

```
Person.age(min(3)) // 3 lowest ages
```

#### max(n)

```
Person.age(max(3)) // 3 highest ages
```

#### rand(n)

```
Person.age(rand(3)) // 3 random persons (with potential for duplicates)
```

#### sample(n)

```
Person.age(sample(3)) // 3 sample persons (without duplicates)
```



### Next

[Parameterized attributes...](/manual/attributes/parameterized)