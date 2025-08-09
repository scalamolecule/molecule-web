# Aggregation

Molecule implements common aggregation functions that work for all supported databases.

## Available aggregate functions

All cardinality-one attribute types can be called with the following aggregation functions that either return a single value or a set of values:

```
Single value:         Multiple values:
-------------         -----------------------------------------
min                   min(n)     // returns n lowest values
max                   max(n)     // returns n highest values
sample                samples(n) // returns n random values 
count                 distinct   // returns all distinct values
countDistinct             
```

Two exceptions exist:

- `Enum` type not supported to be aggregated.
- `Boolean` values can be aggregatd with `count` and `countDistinct` only.

Furthermore, numbers can be aggregated with the following standard math functions:

```
sum
avg
median
variance
stddev
```

Numbers types:

```scala
Byte     Short
Int      Float
Long     Double
BigInt   BigDecimal
```

## 6 operations on aggregated values

Aggregated values can be compared to a constant value using the following operators:

```scala
Customer.id(count)(5).country     // Countries with 5 customers (equality)
Customer.id(count).not(5).country // Countries not with 5 customers (negation)
Customer.id(count).<(5).country   // Countries with less than 5 customers
Customer.id(count).>(5).country   // Countries with more than 5 customers
Customer.id(count).<=(5).country  // Countries with maximum 5 customers
Customer.id(count).>=(5).country  // Countries with minimum 5 customers
```

### Sorting of aggregated values

Aggregated value can be sorted ascending (`a1`) or descending (`d1`):

```scala
// (sorting a matching single value doesn't make sense)

// Countries not with 5 customers, descending customer count
Customer.id(count).not(5).d1.country

// Countries with less than 5 customers, ascending customer count
Customer.id(count).<(5).a1.country

// Countries with more than 5 customers, descending customer count
Customer.id(count).>(5).d1.country

// Countries with maximum 5 customers, ascending customer count
Customer.id(count).<=(5).a1.country

// Countries with minimum 5 customers, descending customer count
Customer.id(count).>=(5).d1.country 
```

## For all types

Let's examine the aggregate functions one by one using the following data set:

```scala
Person.firstName.lastName.age.insert(
  ("Bob", "Johnson", 23),
  ("Liz", "Benson", 24),
  ("Liz", "Murray", 24),
  ("Liz", "Taylor", 25)
).transact
```

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
Person.lastName(min(2)).query.get.head ==> Set("Benson", "Johnson")
Person.lastName(max(2)).query.get.head ==> Set("Murray", "Taylor")
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

### count

Count all persons with an `age` value:

```scala
Person.age(count).query.get ==> List(4)
```

Ages per person name:

```scala
Person.firstName.age(count).query.get ==> List(
  ("Bob", 1),
  ("Liz", 3), // 24, 24, 25
)
```

Ages per person name if more than 1 age:

```scala
Person.firstName.age(count).>(1).query.get ==> List(
  ("Liz", 3), // 24, 24, 25
)
```

Occurences of each age:

```scala
Person.age.age(count).query.get ==> List(
  (23, 1),
  (24, 2),
  (25, 1),
)
```

### countDistinct

Count all persons with a distinct `age` value:

```scala
Person.age(count).query.get ==> List(4)
```

Distinct ages per person name:

```scala
Person.firstName.age(countDistinct).query.get ==> List(
  ("Bob", 1),
  ("Liz", 2), // 24, 25
)
```

Distinct ages per person name if more than 1 age:

```scala
Person.firstName.age(countDistinct).>(1).query.get ==> List(
  ("Liz", 2), // 24, 25
)
```

Distinct cccurences of each age (will always be 1):

```scala
Person.age.age(countDistinct).query.get ==> List(
  (23, 1),
  (24, 1), // 24 and 24 coalesced to one distinct value
  (25, 1),
)
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


## For numbers

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
Person.firstName.age(avg).query.get ==> List(
  ("Bob", 23),
  ("Liz", (24 + 24 + 25) / 3.0), // compare double value
)
Person.age(avg).query.get.head ==> (23 + 24 + 24 + 25) / 4.0
```

### median

```scala
Person.firstName.age(median).query.get ==> List(
  ("Bob", 23),
  ("Liz", 24),
)
Person.age(median).query.get.head ==> 24
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
