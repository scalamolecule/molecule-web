# Sorting

Sort molecule data by adding one or more sort markers to the attributes that you want to sort by. Markers can be any of the following were the number assigns the index/priority of the ordering and the dynamic variants take a number from the environment:

| Ascending | Descending | Dynamic asc | Dynamic desc |
|:---------:|:----------:|-------------|--------------|
|   `a1`    |    `d1`    | `sort(1)`   | `sort(-1)`    |
|   `a2`    |    `d2`    | `sort(2)`   | `sort(-2)`    |
|   `a3`    |    `d3`    | `sort(3)`   | `sort(-3)`    |
|   `a4`    |    `d4`    | `sort(4)`   | `sort(-4)`    |
|   `a5`    |    `d5`    | `sort(5)`   | `sort(-5)`    |

If we have the following data

```scala
Product.name.price.stars.query.get ==> List(
  ("Bread", 20, 3),
  ("Knife", 10, 4),
  ("Socks", 10, 3),
)
```

we can sort by product name

```scala
// Ascending (a1)
Product.name.a1.query.get ==> List(
  "Bread",
  "Knife",
  "Socks",
)

// Descending (d1)
Product.name.d1.query.get ==> List(
  "Socks",
  "Knife",
  "Bread",
)
```

## Multiple

When sorting by multiple attributes we can control the priority of ordering between the attributes:

```scala
Product.name.price.a1.stars.a2.query.get ==> List(
  ("Bread", 10, 3),
  ("Socks", 10, 4),
  ("Knife", 20, 3),
)

Product.name.price.a1.stars.d2.query.get ==> List(
  ("Socks", 10, 4),
  ("Bread", 10, 3),
  ("Knife", 20, 3),
)

Product.name.price.d1.stars.a2.query.get ==> List(
  ("Knife", 20, 3),
  ("Bread", 10, 3),
  ("Socks", 10, 4),
)

Product.name.price.d1.stars.d2.query.get ==> List(
  ("Knife", 20, 3),
  ("Socks", 10, 4),
  ("Bread", 10, 3),
)


Product.name.price.a2.stars.a1.query.get ==> List(
  ("Bread", 10, 3),
  ("Knife", 20, 3),
  ("Socks", 10, 4),
)

Product.name.price.d2.stars.a1.query.get ==> List(
  ("Knife", 20, 3),
  ("Bread", 10, 3),
  ("Socks", 10, 4),
)

Product.name.price.a2.stars.d1.query.get ==> List(
  ("Socks", 10, 4),
  ("Bread", 10, 3),
  ("Knife", 20, 3),
)

Product.name.price.d2.stars.d1.query.get ==> List(
  ("Socks", 10, 4),
  ("Knife", 20, 3),
  ("Bread", 10, 3),
)
```

Etc for 3, 4 and 5 sort markers.

## Optional

If we had a Paper product without any rating yet, we could ask for and sort by optional `stars`. `None` comes first.

```scala
Product.name.price.d1.stars_?.a2.query.get ==> List(
  ("Knife", 20, Some(3)),
  ("Paper", 10, None),
  ("Bread", 10, Some(3)),
  ("Socks", 10, Some(4)),
)

Product.name.price.a1.stars_?.d2.query.get ==> List(
  ("Socks", 10, Some(4)),
  ("Bread", 10, Some(3)),
  ("Paper", 10, None),
  ("Knife", 20, Some(3)),
)
```

## Indexes

Sort indexes of sort markers should:

- Start from 1
- Be a continuous range
- Be unique

```scala
intercept[ModelError] {
  Product.name.a2.query.get
}.msg ==>
  "Sort index 1 should be present and additional indexes " +
    "continuously increase (in any order). " +
    "Found non-unique sort index(es): 2"


intercept[ModelError] {
  Product.name.a1.price.d3.query.get
}.msg ==>
  "Sort index 1 should be present and additional indexes " +
    "continuously increase (in any order). " +
    "Found non-unique sort index(es): 1, 3"


intercept[ModelError] {
  Product.name.a1.price.d1.query.get
}.msg ==>
  "Sort index 1 should be present and additional indexes " +
    "continuously increase (in any order). " +
    "Found non-unique sort index(es): 1, 1"
```

## Aggregates

Aggregated attribute values can be sorted too:

```scala
// Prices with best reviews 
Product.price.stars(avg).d1.query.get ==> List(
  (10, 3.5),
  (20, 3.0),
)
```

## Nested

Aggregated attribute can be sorted independently on each nested level. The sort index rules therefore also apply independently on each level.

```scala
Category.ordering.a1.name
  .Products.*(Product.name.a2.price.a1) // a1 again on nested level ok
  .query.get ==> List(
  (1, "Home", List(
    ("Lamp", 25),
    ("Poster", 25),
    ("Couch", 370),
  )),
  (2, "Outdoor", List(
    ("Hose", 50),
    ("Sun screen", 70),
  )),
)
```

## Dynamic sorting

To avoid hard-coding some sort order, you can add the dynamic sort marker `sort()` and apply either a positive (for ascending) or negative (for descending) number. 

A common User action is to change the sort order of some column in a product catalogue:

```scala
val dynamicQuery = (priceOrder: Int) => Category.ordering.a1.name
  .Products.*(Product.name.a2.price.sort(priceOrder)).query

// Descending price
dynamicQuery(-1).get ==> List(
  (1, "Home", List(
    ("Couch", 370), // first
    ("Lamp", 25),
    ("Poster", 25),
  )),
  (2, "Outdoor", List(
    ("Sun screen", 70), // first
    ("Hose", 50),
  )),
)

// Ascending price
dynamicQuery(1).get ==> List(
  (1, "Home", List(
    ("Lamp", 25),
    ("Poster", 25),
    ("Couch", 370), // last
  )),
  (2, "Outdoor", List(
    ("Hose", 50),
    ("Sun screen", 70), // last
  )),
)
```
As you can see, we can mix hard-coded and dynamic sort markers.


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Sorting compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/sorting)