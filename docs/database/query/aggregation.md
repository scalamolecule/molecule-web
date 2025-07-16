
# Aggregation

Molecule implements common aggregation functions that work for all supported databases.

Aggregations only work on cardinality-one attributes (scalars).

## All types

The following aggregation functions work for all attribute types:

```scala
Person.firstName.lastName.age.insert(
  ("Bob", "Johnson", 23),
  ("Liz", "Benson", 24),
  ("Liz", "Murray", 24),
  ("Liz", "Taylor", 25)
).transact
```


### count

Count all entities with an `age` value:

```scala
Person.firstName.age(count).query.get ==> List(
  ("Bob", 1),
  ("Liz", 3), // 24, 24, 25
)
Person.age(count).query.get ==> List(4)
```

### countDistinct

Count entities with distinct `age` values:

```scala
Person.firstName.age(countDistinct).query.get ==> List(
  ("Bob", 1),
  ("Liz", 2), // 24, 25
)
Person.age(countDistinct).query.get ==> List(3)
```

### distinct

Return distinct values in a `Set`

```scala
// Distinct ages by firstName
Person.firstName.age(distinct).query.get ==> List(
  ("Bob", Set(23)),
  ("Liz", Set(24, 25))
)

// Distinct ages
Person.age(distinct).query.get ==> List(
  Set(23, 24, 25)
)
```
Notice how applying the `distinct` keyword changes the return type from `Int` to `Set[Int]`, or more generally from `<basetype` to `Set[<basetype>]`.



### min/max

Apply `min` or `max` to any (cardinality-one) attribute to return its lowest/highest value.

```scala
Person.age(min).query.get.head ==> 23
Person.age(max).query.get.head ==> 25

Person.lastName(min).query.get.head ==> "Benson"
Person.lastName(max).query.get.head ==> "Taylor"
```

Get min/max n values:

```scala
// 2 lowest/highest ages
Person.age(min(2)).query.get.head ==> Set(23, 24)
Person.age(max(2)).query.get.head ==> Set(24, 25)

// 2 firstly/lastly ordered lastNames
Person.lastName(min).query.get.head ==> Set("Benson", "Johnson")
Person.lastName(max).query.get.head ==> Set("Murray", "Taylor")
```

Notice how the return type changes from `<basetype` to `Set[<basetype>]`.


### sample

Return a random saved value

```scala
// Multiple invocations, random value
Person.age(sample).query.get.head ==> 24
Person.age(sample).query.get.head ==> 23
Person.age(sample).query.get.head ==> 24
```

Return n random values
```scala
Person.age(sample(2)).query.get.head ==> Set(24, 25)
Person.age(sample(2)).query.get.head ==> Set(25, 23)
Person.age(sample(2)).query.get.head ==> Set(23, 24)
```


## Numbers

### sum

```scala
Person.firstName.age(sum).query.get ==> List(
  ("Bob", 23),
  ("Liz", 24 + 24 + 25),
)
Person.age(sum).query.get.head ==> (23 + 24 + 24 + 25)
```

### avg

```scala
Person.firstName.age(sum).query.get ==> List(
  ("Bob", 23),
  ("Liz", (24 + 24 + 25) / 3),
)
Person.age(sum).query.get.head ==> (23 + 24 + 24 + 25) / 4
```

### median

```scala
Person.firstName.age(avg).query.get ==> List(
  ("Bob", 23),
  ("Liz", 24),
)
Person.age(sum).query.get.head ==> 24
```

### variance

```scala
Person.age(variance) // variance of ages
```

### stddev

```scala
Person.age(stddev) // standard deviation of ages
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Aggregation compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/aggregation)
