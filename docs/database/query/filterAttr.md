
# Filter Attribute

Molecule can filter an attribute by the value of another attribute.

Imagine for instance football players and their goals and assists: 

```scala
Player.name.goals.assists.insert(
  ("Celso", 1, 2),
  ("Messi", 3, 3),
  ("Salah", 5, 3),
).transact
```

## Equality

Now we can find players who have an equal number of goals and assists

```scala
Player.name.goals(Player.assists_).query.get ==> List(("Messi", 3))
```
Filter attributes (like `assists_`) should be tacit and without applied operations.

Make the left-hand side attribute `goals_` tacit too if you want the player name only
```scala
Player.name.goals_(Player.assists_).query.get ==> List("Messi")
```


## Comparison

We can compare two values with various comparators and simply add the filter attribute again as a mandatory attribute to get that value too:

```scala
// Different number of goals and assists
Player.name.goals.not(Player.assists_).assists.query.get ==> List(
  ("Celso", 1, 2),
  ("Salah", 5, 4),
)

// Fewer goals than assists
Player.name.goals.<(Player.assists_).assists.query.get ==> List(
  ("Celso", 1, 2),
)

// More goals than assists
Player.name.goals.>(Player.assists_).assists.query.get ==> List(
  ("Salah", 5, 4),
)

// Fewer or equally many goals as assists
Player.name.goals.<=(Player.assists_).assists.query.get ==> List(
  ("Celso", 1, 2),
  ("Messi", 3, 3),
)

// More or equally many goals as assists
Player.name.goals.>=(Player.assists_).assists.query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)
```


## 1 sub filter
 
We can further narrow the result by adding a sub filter on the filter attribute

::: code-tabs
@tab equal
```scala
// More or equally many goals than assists when assists == 3
Player.name.goals.>=(Player.assists_)
  .assists(3).query.get ==> List(
  ("Messi", 3, 3),
)

// More or equally many goals than assists when assists == 4
Player.name.goals.>=(Player.assists_)
  .assists(4).query.get ==> List(
  ("Salah", 5, 4),
)
```

@tab not
```scala
// More or equally many goals than assists when assists != 3
Player.name.goals.>=(Player.assists_)
  .assists.not(3).query.get ==> List(
  ("Salah", 5, 4),
)

// More or equally many goals than assists when assists != 4
Player.name.goals.>=(Player.assists_)
  .assists.not(4).query.get ==> List(
  ("Messi", 3, 3),
)
```

@tab <
```scala
// More or equally many goals than assists when assists < 3
Player.name.goals.>=(Player.assists_)
  .assists.<(3).query.get ==> List()

// More or equally many goals than assists when assists < 4
Player.name.goals.>=(Player.assists_)
  .assists.<(4).query.get ==> List(
  ("Messi", 3, 3),
)
```

@tab >
```scala
// More or equally many goals than assists when assists > 3
Player.name.goals.>=(Player.assists_)
  .assists.>(3).query.get ==> List(
  ("Salah", 5, 4),
)

// More or equally many goals than assists when assists > 4
Player.name.goals.>=(Player.assists_)
  .assists.>(4).query.get ==> List()
```

@tab <=
```scala
// More or equally many goals than assists when assists <= 3
Player.name.goals.>=(Player.assists_)
  .assists.<=(3).query.get ==> List(
  ("Messi", 3, 3),
)

// More or equally many goals than assists when assists <= 4
Player.name.goals.>=(Player.assists_)
  .assists.<=(4).query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)
```

@tab >=
```scala
// More or equally many goals than assists when assists >= 3
Player.name.goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)

// More or equally many goals than assists when assists >= 4
Player.name.goals.>=(Player.assists_)
  .assists.>=(4).query.get ==> List(
  ("Salah", 5, 4),
)
```
:::

Or we could add a sub filter on the calling attribute `goals` instead:

```scala
// More or equally many goals than assists when goals >= 3
Player.name.goals_.>=(3)
  .goals.>=(Player.assists_).assists.query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)

// More or equally many goals than assists when goals >= 4
Player.name.goals_.>=(4)
  .goals.>=(Player.assists_).assists.query.get ==> List(
  ("Salah", 5, 4),
)
```


## 2 sub filters

We can add sub filters to both the calling attribute `goals` and filter attribute `assists`:

::: code-tabs
@tab equal
```scala
// More or equally many goals than assists
// when goals == 3 and assists >= 3
Player.name.goals_(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
)

// More or equally many goals than assists
// when goals == 5 and assists >= 3
Player.name.goals_(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Salah", 5, 4),
)
```

@tab not
```scala
// More or equally many goals than assists
// when goals != 3 and assists >= 3
Player.name.goals_.not(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Salah", 5, 4),
)

// More or equally many goals than assists
// when goals != 5 and assists >= 3
Player.name.goals_.not(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
)
```

@tab <
```scala
// More or equally many goals than assists
// when goals < 3 and assists >= 3
Player.name.goals_.<(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List()

// More or equally many goals than assists
// when goals < 5 and assists >= 3
Player.name.goals_.<(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
)
```

@tab >
```scala
// More or equally many goals than assists
// when goals > 3 and assists >= 3
Player.name.goals_.>(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Salah", 5, 4),
)

// More or equally many goals than assists
// when goals > 5 and assists >= 3
Player.name.goals_.>(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List()
```

@tab <=
```scala
// More or equally many goals than assists
// when goals <= 3 and assists >= 3
Player.name.goals_.<=(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
)

// More or equally many goals than assists
// when goals <= 5 and assists >= 3
Player.name.goals_.<=(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)
```

@tab >=
```scala
// More or equally many goals than assists
// when goals >= 3 and assists >= 3
Player.name.goals_.>=(3)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Messi", 3, 3),
  ("Salah", 5, 4),
)

// More or equally many goals than assists
// when goals >= 5 and assists >= 3
Player.name.goals_.>=(5)
  .goals.>=(Player.assists_)
  .assists.>=(3).query.get ==> List(
  ("Salah", 5, 4),
)
```
:::


## Cross ns

So far we have compared attributes of the same namespace. We can also compare an attribute with a filter attribute from a referenced namespace. Let's expand our example data a bit by adding a relationship:

```scala
Player.name.goals
  .Team.name.goalsToBonus.bonus.query.get ==> List(
  ("Celso", 1, "Champions", 3, 1000000),
  ("Messi", 3, "Champions", 3, 1000000),
  ("Salah", 5, "Galactico", 4, 5000000),
)
```
Now we can ask questions across namespaces like "which players of what teams scored enough goals to get a bonus?"
```scala
Player.name.goals.>=(Team.goalsToBonus_)
  .Team.name.goalsToBonus.bonus.query.get ==> List(
  ("Messi", 3, "Champions", 3, 1000000),
  ("Salah", 5, "Galactico", 4, 5000000),
)
```
Or we can ask the other way around "which teams have players getting a bonus?"
```scala
Player.name.goals
  .Team.name.goalsToBonus.<=(Player.goals_).bonus.query.get ==> List(
  ("Messi", 3, "Champions", 3, 1000000),
  ("Salah", 5, "Galactico", 4, 5000000),
)
```

The attribute in the filter expression has to be tacit, and it has to be present and mandatory in the ref namespace. Otherwise, Molecule will warn you:

```scala
intercept[ModelError] {
  Player.name.goals.>=(Team.goalsToBonus_)
    .Team.name.bonus // goalsToBonus missing
    .query.get
}.msg ==> 
  "Please add missing filter attribute Team.goalsToBonus"
```



## Nested

We can also compare attributes across nested data. If we had for instance instead modelled a `Team` with a cardinality-many relationship to `Player`s we could a list of teams each with a list of players:

```scala
Team.name.goalsToBonus.bonus
  .Players.*(Player.name.goals).query.get ==> List(
  ("Champions", 3, 1000000, List(
    ("Celso", 1),
    ("Messi", 3),
  )),
  ("Galactico", 4, 5000000, List(
    ("Salah", 5),
    ("Yamal", 2),
  )),
)
```
As before, but now with nested data, we can ask "which players of what teams scored enough goals to get a bonus?"
```scala
Team.name.goalsToBonus.<=(Player.goals_).bonus
  .Players.*(Player.name.goals).query.get ==> List(
  ("Champions", 3, 1000000, List(
    ("Messi", 3),
  )),
  ("Galactico", 4, 5000000, List(
    ("Salah", 5),
  )),
)
```
Or the other way around "which teams have players getting a bonus?"
```scala
Team.name.goalsToBonus.bonus
  .Players.*(Player.name.goals.>=(Team.goalsToBonus_))
  .query.get ==> List(
  ("Champions", 3, 1000000, List(
    ("Messi", 3),
  )),
  ("Galactico", 4, 5000000, List(
    ("Salah", 5),
  )),
)
```
and "which teams have players scoring more goals than goalsToBonus?", and "Champions" is no longer included:
```scala
Team.name.goalsToBonus.bonus
  .Players.*(Player.name.goals.>(Team.goalsToBonus_))
  .query.get ==> List(
  ("Galactico", 4, 5000000, List(
    ("Salah", 5),
  )),
)
```

Filter attributes works the same with optional nested data (using `*?` instead of `*`).


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Filter attribute compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/filterAttr)
