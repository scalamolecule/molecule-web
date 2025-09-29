# Filters

Molecule query results can be filtered by the presence of attributes and various operations on attributes.


## Not null

The simplest filter is asking for a mandatory presence of an attribute value.

Say that we have the following data set where various Persons have either a single food preference in a cardinality-one attribute, or multiple food preferences as a Set, Seq or Map:

::: code-tabs#types
@tab One
```scala
Person.name.likes_?.query.get ==> List(
  ("Jon", Some("pizza")),
  ("Ben", Some("pizza")),
  ("Ann", Some("sushi")),
  ("Gus", Some("pasta")),
  ("Liz", None),
)
```
@tab Set
```scala
Person.name.likes_?.query.get ==> List(
  ("Jon", Some(Set("pizza", "burger"))),
  ("Ben", Some(Set("pizza", "pasta"))),
  ("Ann", Some(Set("sushi", "lasagne"))),
  ("Gus", Some(Set("pasta"))),
  ("Liz", None),
)
```
@tab Seq
```scala
Person.name.likes_?.query.get ==> List(
  ("Jon", Some(List("pizza", "burger"))),
  ("Ben", Some(List("pizza", "pasta"))),
  ("Ann", Some(List("sushi", "lasagne"))),
  ("Gus", Some(List("pasta"))),
  ("Liz", None),
)
```
@tab Map
```scala
Person.name.likes_?.query.get ==> List(
  ("Jon", Some(Map("morning" -> "bread", "dinner" -> "pizza"))),
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
  ("Jon", "pizza"),
  ("Ben", "pizza"),
  ("Ann", "sushi"),
  ("Gus", "pasta"),
)
```
@tab Set
```scala
Person.name.likes.query.get ==> List(
  ("Jon", Set("pizza", "burger")),
  ("Ben", Set("pizza", "pasta")),
  ("Ann", Set("sushi", "lasagne")),
  ("Gus", Set("pasta")),
)
```
@tab Seq
```scala
Person.name.likes.query.get ==> List(
  ("Jon", List("pizza", "burger")),
  ("Ben", List("pizza", "pasta")),
  ("Ann", List("sushi", "lasagne")),
  ("Gus", List("pasta")),

)
```
@tab Map
```scala
Person.name.likes.query.get ==> List(
  ("Jon", Map("morning" -> "bread", "dinner" -> "pizza")),
  ("Ben", Map("morning" -> "bread", "dinner" -> "burger")),
  ("Ann", Map("morning" -> "yoghurt")),
  ("Gus", Map("dinner" -> "pasta")),
)
```
:::


## Null

Likewise, we can ask for entities **_not_** having an attribute value by applying empty parentheses to a tacit attribute. This translates to an SQL `where` clause <nobr>`likes is null`</nobr>:

::: code-tabs
@tab Molecule
```scala
Person.name.likes_().query.get ==> List("Liz")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes IS NULL;
```
:::

Note that we have to make the `likes_` attribute tacit with the underscore since we can't return non-existing values.


## Equality

Find names of persons liking pizza by applying the value "pizza" to the `likes` attribute:

::: code-tabs
@tab Molecule
```scala
Person.name.likes("pizza").query.get ==> List(
  ("Jon", "pizza"),
  ("Ben", "pizza")
)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes = 'pizza'
ORDER BY Person.name;
```
:::

Make `likes_` tacit to avoid returning redundant and already known filter data:

::: code-tabs
@tab Molecule
```scala
Person.name.likes_("pizza").query.get ==> List("Jon", "Ben")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes = 'pizza'
ORDER BY Person.name;
```
:::

In case we have some dynamic optional filter value, we can filter by the optional value by making the `likes_?` attribute optional:

::: code-tabs
@tab Molecule
```scala
val foodFilter = Some("pizza")
Person.name.likes_?(foodFilter).query.get ==> List(
  ("Jon", Some("pizza")),
  ("Ben", Some("pizza"))
)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes = 'pizza'
ORDER BY Person.name;
```
:::

or by using the empty `Option` value:
::: code-tabs
@tab Molecule
```scala
val foodFilter = Option.empty[String]
Person.name.likes_?(foodFilter).query.get ==> List(
  ("Liz", None)
)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes IS NULL
ORDER BY Person.name;
```
:::


## OR logic

We can apply OR-logic to find a selection of alternatives

::: code-tabs
@tab Molecule
```scala
// Likes pizza OR sushi
Person.name.likes("pizza", "sushi").query.get ==> List(
  ("Jon", "pizza"),
  ("Ben", "pizza"),
  ("Ann", "sushi"),
  // Gus not included
)

// Same as applying filter values as a List
Person.name.likes(List("pizza", "sushi")) // ...
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes IN ('pizza', 'sushi')
ORDER BY Person.name;
```
:::

Applying an empty `Seq` (or `List`) of values matches nothing

::: code-tabs
@tab Molecule
```scala
Person.name.likes(List.empty[String]).query.get ==> Nil
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name IS NOT NULL AND
  FALSE       ;
```
:::


## Negation

Exclude entities with a certain attribute value by calling `not(<value>)` on the attribute:

::: code-tabs
@tab Molecule
```scala
Person.name.likes.not("pizza").query.get ==> List(
  ("Ann", "sushi"),
  ("Gus", "pasta"),
)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes <> 'pizza'
ORDER BY Person.name;
```
:::


## NOR logic

Exclude entities by negating multiple values for an attribute

::: code-tabs
@tab Molecule
```scala
// Likes NEITHER pizza NOR sushi (using varargs)
Person.name.likes.not("pizza", "sushi").query.get ==> List(
  ("Gus", "pasta"),
)

// Same as
Person.name.likes.not(List("pizza", "sushi")).query.get ==> List(
  ("Gus", "pasta"),
)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name,
  Person.likes
FROM Person
WHERE
  Person.name  IS NOT NULL AND
  Person.likes NOT IN ('pizza', 'sushi')
ORDER BY Person.name;
```
:::


## Comparison

We can filter attribute values that satisfy comparison expressions.

::: code-tabs
@tab Molecule
```scala
Person.name.a1.age_.<(18).query.i.get ==> List("Ann")
Person.name.a1.age_.<=(18).query.i.get ==> List("Ann", "Ben")
Person.name.a1.age_.>(18).query.i.get ==> List("Gus", "Jon")
Person.name.a1.age_.>=(18).query.i.get ==> List("Ben", "Gus", "Jon")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  < 18
ORDER BY Person.name;
----------------------------------
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  <= 18
ORDER BY Person.name;
----------------------------------
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  > 18
ORDER BY Person.name;
----------------------------------
SELECT DISTINCT
  Person.name
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  >= 18
ORDER BY Person.name;
```
:::


Comparison of all types are performed with java's `compareTo` method. So we can for instance also compare text strings:

::: code-tabs
@tab Molecule
```scala
Person.likes.<("pizza").query.i.get ==> List("pasta")
Person.likes.<=("pizza").query.i.get ==> List("pasta", "pizza")
Person.likes.>("pizza").query.i.get ==> List("sushi")
Person.likes.>=("pizza").query.i.get ==> List("pizza", "sushi")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes < 'pizza';
----------------------------------
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes <= 'pizza';
----------------------------------
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes > 'pizza';
----------------------------------
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes >= 'pizza';
```
:::


## String

Case-sensitive String filters can be applied

::: code-tabs
@tab Molecule
```scala
Person.likes.startsWith("p").query.get ==> List("pasta", "pizza")
Person.likes.endsWith("a").query.get ==> List("pasta", "pizza")
Person.likes.contains("s").query.get ==> List("pasta", "sushi")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes LIKE 'p%';
----------------------------------  
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes LIKE '%a';
----------------------------------  
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes LIKE '%s%';    
```
:::

Or regex matches

::: code-tabs
@tab Molecule
```scala
// Starting with any letter r-z
Person.likes.matches("^[r-z].*").query.get ==> List("sushi")
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Person.likes
FROM Person
WHERE
  Person.likes ~ '^[r-z].*';
```
:::


## Number

#### modulus

Use `%(divider, remainder)` to calculate the modulus of a number attribute. Divide attribute number values by the divider and match the remainder.

::: code-tabs
@tab Molecule
```scala
Gamer.rank.insert(-4 to 4).transact

Gamer.rank.%(3, 0).query.get ==> List(-3, 0, 3)
Gamer.rank.%(3, 1).query.get ==> List(-4, -1, 1, 4)
Gamer.rank.%(3, 2).query.get ==> List(-2, 2)
```
@tab SQL (H2)
```sql
SELECT DISTINCT
  Gamer.rank
FROM Gamer
WHERE
  Gamer.rank % 3 = 0;
----------------------------------
SELECT DISTINCT
  Gamer.rank
FROM Gamer
WHERE
  Gamer.rank % 3 IN (1, -1);
----------------------------------  
SELECT DISTINCT
  Gamer.rank
FROM Gamer
WHERE
  Gamer.rank % 3 IN (2, -2);
```
:::

#### `odd` `even`

Since odd/even filters are common, they have their own methods:

::: code-tabs
@tab Molecule
```scala
Gamer.rank.odd.query.get ==> List(-3, -1, 1, 3)
Gamer.rank.even.query.get ==> List(-4, -2, 0, 2, 4)
```
@tab SQL (H2)
```sql
-- odd
SELECT DISTINCT
  Gamer.rank
FROM Gamer
WHERE
  Gamer.rank % 2 IN (1, -1);
  
-- even
SELECT DISTINCT
  Gamer.rank
FROM Gamer
WHERE
  Gamer.rank % 2 = 0;
```
:::


## Set

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

## Seq

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