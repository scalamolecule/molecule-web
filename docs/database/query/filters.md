# Filters

Molecule query results can be filtered by the presence of attributes and various operations on attributes.


## Not null

The simplest filter is asking for a mandatory presence of an attribute value. 

Say that we have the following data set where various Persons have either a single food preference in a cardinality-one attribute, or multiple food preferences as a Set, Seq or Map:

::: code-tabs#types
@tab One

```scala
Person.name.likes_?.query.get ==> List(
  ("John", Some("pizza")),
  ("Ben", Some("pizza")),
  ("Ann", Some("sushi")),
  ("Gus", Some("pasta")),
  ("Liz", None),
)
```
Notice that 

@tab Set

```scala
Person.name.likes_?.query.get ==> List(
  ("John", Some(Set("pizza", "burger"))),
  ("Ben", Some(Set("pizza", "pasta"))),
  ("Ann", Some(Set("sushi", "lasagne"))),
  ("Gus", Some(Set("pasta"))),
  ("Liz", None),
)
```

@tab Seq

```scala
Person.name.likes_?.query.get ==> List(
  ("John", Some(List("pizza", "burger"))),
  ("Ben", Some(List("pizza", "pasta"))),
  ("Ann", Some(List("sushi", "lasagne"))),
  ("Gus", Some(List("pasta"))),
  ("Liz", None),
)
```

@tab Map

```scala
Person.name.likes_?.query.get ==> List(
  ("John", Some(Map("morning" -> "bread", "dinner" -> "pizza"))),
  ("Ben", Some(Map("morning" -> "bread", "dinner" -> "burger"))),
  ("Ann", Some(Map("morning" -> "yoghurt"))),
  ("Gus", Some(Map("dinner" -> "pasta"))),
  ("Liz", None),
)
```

:::

Then we can ask for only persons with a food preference by adding the attribute as-is, as a mandatory attribute. Like in SQL saying <nobr>`likes is not null`</nobr>. And we will get a list without Liz since no `likes` value is set for her:

::: code-tabs#types
@tab One

```scala
Person.name.likes.query.get ==> List(
  ("John", "pizza"),
  ("Ben", "pizza"),
  ("Ann", "sushi"),
  ("Gus", "pasta"),
)
```

@tab Set

```scala
Person.name.likes.query.get ==> List(
  ("John", Set("pizza", "burger")),
  ("Ben", Set("pizza", "pasta")),
  ("Ann", Set("sushi", "lasagne")),
  ("Gus", Set("pasta")),
)
```

@tab Seq

```scala
Person.name.likes.query.get ==> List(
  ("John", List("pizza", "burger")),
  ("Ben", List("pizza", "pasta")),
  ("Ann", List("sushi", "lasagne")),
  ("Gus", List("pasta")),

)
```

@tab Map

```scala
Person.name.likes.query.get ==> List(
  ("John", Map("morning" -> "bread", "dinner" -> "pizza")),
  ("Ben", Map("morning" -> "bread", "dinner" -> "burger")),
  ("Ann", Map("morning" -> "yoghurt")),
  ("Gus", Map("dinner" -> "pasta")),
)
```

:::

## Null

Likewise, we can ask for entities **_not_** having an attribute value by applying empty parentheses to a tacit attribute. This translates to an SQL `where` clause <nobr>`likes is null`</nobr>:

```scala
Person.name.likes_().query.get ==> List("Liz")
```

Note that we have to make the `likes_` attribute tacit with the underscore since we can't return non-existing values.

## Equality

Find names of persons liking pizza by applying the value "pizza" to the `likes` attribute:

```scala
Person.name.likes("pizza").query.get ==> List(
  ("John", "pizza"),
  ("Ben", "pizza")
)
```

Make `likes_` tacit to avoid returning redundant and already known filter data:

```scala
Person.name.likes_("pizza").query.get ==> List("John", "Ben")
```

In case we have some dynamic optional filter value, we can filter by the optional value by making the `likes_?` attribute optional:

```scala
val foodFilter = Some("pizza")
Person.name.likes_?(foodFilter).query.get ==> List(
  ("John", Some("pizza")),
  ("Ben", Some("pizza"))
)
// or
val foodFilter = Option.empty[String]
Person.name.likes_?(foodFilter).query.get ==> List(
  ("Liz", None)
)
```

#### OR logic

We can apply OR-logic to find a selection of alternatives

```scala
// Likes pizza OR sushi
Person.name.likes("pizza", "sushi").query.get ==> List(
  ("John", "pizza"),
  ("Ben", "pizza"),
  ("Ann", "sushi"),
  // Gus not included
)

// Same as applying filter values as a List
Person.name.likes(List("pizza", "sushi")) // ...
```

Applying an empty `Seq` (or `List`) of values matches nothing

```scala
Person.name.likes(List.empty[String]).query.get ==> Nil
```

## Negation

Exclude entities with a certain attribute value by calling `not(<value>)` on the attribute:

```scala
Person.name.likes.not("pizza").query.get ==> List(
  ("Ann", "sushi"),
  ("Gus", "pasta"),
)
```

#### NOR logic

Exclude entities by negating multiple values for an attribute

```scala
// Likes NEITHER pizza NOR sushi (using varargs)
Person.name.likes.not("pizza", "sushi").query.get ==> List(
  ("Gus", "pasta"),
)

// Likes NEITHER pizza NOR burger (using List)
Person.name.likes.not(List("pizza", "burger")).query.get ==> List(
  ("Ann", "sushi"),
  ("Gus", "pasta"),
)
```

## Comparison

We can filter attribute values that satisfy comparison expressions.

```scala
Person.age.<(18)
Person.age.<=(18)
Person.age.>(18)
Person.age.>=(18)
```

Comparison of all types are performed with java's `compareTo` method. So we can for instance also compare text strings:

```scala
Person.likes.<("pi").query.get ==> List("pasta")
Person.likes.<=("pi").query.get ==> List("pasta", "pizza")
Person.likes.>("pi").query.get ==> List("sushi")
Person.likes.>=("pi").query.get ==> List("pizza", "sushi")
```

## String

Case-sensitive String filters can be applied

```scala
Person.likes.startsWith("p").query.get ==> List("pasta", "pizza")
Person.likes.endsWith("a").query.get ==> List("pasta", "pizza")
Person.likes.contains("s").query.get ==> List("pasta", "sushi")
```

Or regex matches

```scala
// Starting with any letter r-z
Person.likes.matches("^[r-z].*").query.get ==> List("sushi")
```

## Number

#### modulus

Use `%(divider, remainder)` to calculate the modulus of a number attribute. Divide attribute number values by the divider and match the remainder.

```scala
Game.numbers.insert(1 to 10).transact

// Use 
Game.numbers.%(3, 0).query.get ==> List(3, 6, 9)
Game.numbers.%(3, 1).query.get ==> List(1, 4, 7, 10)
Game.numbers.%(3, 2).query.get ==> List(2, 5, 8)

// Odd
Game.numbers.%(2, 1).query.get ==> List(1, 3, 5, 7, 9)
// Even
Game.numbers.%(2, 0).query.get ==> List(2, 4, 6, 8, 10)
```

#### `odd` `even`

Since odd/even filters are common, they have their own methods:

```scala
Game.numbers.odd.query.get ==> List(1, 3, 5, 7, 9)
Game.numbers.even.query.get ==> List(2, 4, 6, 8, 10)
```

## Set/Seq

Matching equality of a specific Set/Seq of values is not supported. But we can match a subselection with `has` or `hasNo`:

#### `has(value)`

Check for presence of one or more values in a `Set` or `Seq` (without returning the scores themselves by using tacit `scores_` attribute):

```scala
Person.name.scores.insert(
  ("Bob", List(7, 6, 7, 4)),
  ("Liz", List(9, 3, 6)),
).transact

Person.name.scores_.has(6).query.get ==> List("Bob", "Liz")
Person.name.scores_.has(7).query.get ==> List("Bob")
Person.name.scores_.has(8).query.get ==> List()

// "Has any of"
Person.name.scores_.has(1, 2, 3).query.get ==> List("Liz")
// same as
Person.name.scores_.has(Seq(1, 2, 3)).query.get ==> List("Liz")
```

#### `hasNo(value)`

Check for absence of one or more values in a `Set` or `Seq`:

```scala
Person.name.scores_.hasNo(6).query.get ==> List()
Person.name.scores_.hasNo(7).query.get ==> List("Liz")
Person.name.scores_.hasNo(8).query.get ==> List("Bob", "Liz")

// "Has none of"
Person.name.scores_.hasNo(1, 2, 3).query.get ==> List("Bob")
// same as
Person.name.scores_.hasNo(Seq(1, 2, 3)).query.get ==> List("Bob")
```

## Map

Map attributes can be filtered by key or value.

We'll use the following example of a Map attribute capturing Shostakovich's name in different languages and see how we can use the operations available.

```scala
val namesMap = Map(
  "en" -> "Shostakovich",
  "de" -> "Schostakowitsch",
  "fr" -> "Chostakovitch",
)
Person.name("Shostakovich").langNames(namesMap).transact
```

### Filter by key

#### `attr(key)`

Retrieve a value by applying a key to a mandatory Map attribute:

```scala
// Get German spelling of Shostakovich 
Person.langNames("de").query.get ==> List("Schostakowitsch")
```

Equivalent to calling `apply` on a Scala Map

```scala
namesMap("de") ==> "Schostakowitsch"
```

Looking up a non-existing key simply returns an empty result

```scala
Person.langNames("xx").query.get ==> Nil
```

#### `attr_(key)`

Ensure a certain key is present by applying the key to a tacit Map attribute:

```scala
// Get (English) name having a German spelling 
Person.name.langNames_("de").query.get ==> List("Shostakovich")
Person.name.langNames_("xx").query.get ==> Nil
```

#### `attr_?(key)`

Retrieve an optional value by applying a key to an optional Map attribute:

```scala
Person.langNames_?("de").get.head ==> Some("Schostakowitsch")
Person.langNames_?("xx").get.head ==> None
```

Equivalent to calling `get` on a Scala Map

```scala
namesMap.get("de") ==> Some("Schostakowitsch")
namesMap.get("xx") ==> None
```

#### `attr.not(keys)`

Get Maps **_not_** having a certain key by applying the key to `not` of a mandatory Map attribute:

```scala
// Get langNames maps without a Spanish spelling
Person.langNames.not("es").get ==> List(namesMap)

// Get langNames maps without an English spelling
Person.langNames.not("en").get ==> Nil
```

Multiple keys kan be applied as varargs or a `Seq`

```scala
// Get langNames maps without Spanish or Chinese spelling
Person.langNames.not("es", "cn").get ==> List(namesMap)
Person.langNames.not(Seq("es", "cn")).get ==> List(namesMap)

// One of the keys exists, so no match 
Person.langNames.not(List("es", "en")).get ==> Nil
```

#### `attr_.not(keys)`

Match Maps **_not_** having a certain key by applying the key to `not` of a tacit Map attribute:

```scala
// Match langNames maps without a Spanish spelling
Person.name.langNames_.not("es").get ==> List("Shostakovich")

// Match langNames maps without an English spelling
Person.name.langNames_.not("en").get ==> Nil
```

Multiple keys kan be applied as varargs or a `Seq`

```scala
// Match langNames maps without Spanish or Chinese spelling
Person.name.langNames_.not("es", "cn").get ==> List("Shostakovich")
Person.name.langNames_.not(Seq("es", "cn")).get ==> List("Shostakovich")

// One of the keys exists, so no match 
Person.name.langNames_.not(List("es", "en")).get ==> Nil
```

### Filter by value

#### `attr.has(values)`

Return Maps that have certain values with `has` on a mandatory Map attribute:

```scala
// Get map if it has a spelling value of "Chostakovitch"
Person.langNames.has("Chostakovitch").get ==> List(namesMap)

// Get map if it has a spelling 
// value of "Chostakovitch" or "Sjostakovitj"
Person.langNames.has("Chostakovitch", "Sjostakovitj").get ==> List(namesMap)
```

#### `attr_.has(values)`

Match Maps that have certain values with `has` on a tacit Map attribute:

```scala
// Match map if it has a spelling value of "Chostakovitch"
Person.name.langNames_.has("Chostakovitch").get ==> List("Shostakovich")

Person.langNames_.has("Chostakovitch", "Sjostakovitj").get ==> List("Shostakovich")
```

Likewise we can ask for Map attributes **_without_** certain values:

#### `attr.hasNo(values)`

Return Maps **_without_** certain values using `hasNo` on a mandatory Map attribute:

```scala
// Get map if it doesn't have a spelling of "Sjostakovitj"
Person.langNames.hasNo("Sjostakovitj").get ==> List(namesMap)

// Get map if it doesn't have a spelling 
// value of "Chostakovitch" or "Sjostakovitj"
Person.langNames.hasNo("Chostakovitch", "Sjostakovitj").get ==> Nil
```

#### `attr_.hasNo(values)`

Match Maps **_without_** certain values using `hasNo` on a tacit Map attribute:

```scala
// Match map if it doesn't have a spelling value of "Sjostakovitj"
Person.name.langNames_.hasNo("Sjostakovitj").get ==> List("Shostakovich")

// Match map if it doesn't have a spelling 
// value of "Chostakovitch" or "Sjostakovitj"
Person.langNames_.hasNo("Chostakovitch", "Sjostakovitj").get ==> Nil
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Filter compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/filter)