# Pagination

Molecule offers both Offset pagination and Cursor pagination.

## Offset

Offset pagination is easy to understand and use. But it comes with a performance penalty the bigger the offset is on big data sets since the whole data set is scanned each time. But for smaller data sets it works fine.

We'll use the following minimal example data set as a testing ground:

```scala
Gamer.rank.query.get ==> List(1, 2, 3)
```

Let's first look att `limit` and `offset` separately:

### Limit

Assuming that `rank` is sorted, we can return `n` rows with `limit(n)`:

```scala
Gamer.rank.a1.query.limit(1).get ==> List(1)
Gamer.rank.a1.query.limit(2).get ==> List(1, 2)
```

It's like `take(n)` on a `List`. But applying a negative number changes the semantics and returns `n` rows from the end instead:

```scala
Gamer.rank.a1.query.limit(-1).get ==> List(3)
Gamer.rank.a1.query.limit(-2).get ==> List(2, 3)
```


### Offset

When using an offset, the return type changes to include

- data,
- total count of rows and
- whether there are more rows to fetch

```scala
val normal: List[(Int, String)] =
  Gamer.rank.name.query.get

val offset: (List[(Int, String)], Int, Boolean) =
  Gamer.rank.name.query.offset(1).get
```

Again assuming that our numbers are in order, we can return rows starting from `n` with `offset(n)`:

```scala
// Starting from beginning (all)
Gamer.rank.a1.query.offset(0).get ==> (List(1, 2, 3), 3, false) // all

// Starting from second row (0-based indexing)
Gamer.rank.a1.query.offset(1).get ==> (List(2, 3), 3, false)

// Etc..
Gamer.rank.a1.query.offset(2).get ==> (List(3), 3, false)
Gamer.rank.a1.query.offset(3).get ==> (List(), 3, false)
```

As with `limit` we can acquire an offset from the end with a negative `n`:

```scala
// Before end (all)
Gamer.rank.a1.query.offset(0).get ==> (List(1, 2, 3), 3, false)

// Before last row
Gamer.rank.a1.query.offset(-1).get ==> (List(1, 2), 3, false)

// Before second last row, etc.
Gamer.rank.a1.query.offset(-2).get ==> (List(1), 3, false)
Gamer.rank.a1.query.offset(-3).get ==> (List(), 3, false)
```


### Paginate

Offset pagination uses `limit` and `offset` in tandem. Going forward we can get page 1 and 2 like this:

```scala
// First page - more pages after...
Gamer.rank.a1.query.limit(2).offset(0).get ==> (List(1, 2), 3, true)

// Next page - is last page (no more afer)
Gamer.rank.a1.query.limit(2).offset(2).get ==> (List(3), 3, false)
```

`offset` and `limit` can be added in any order

```scala
Gamer.rank.a1.query.offset(0).limit(2).get ==> (List(1, 2), 3, true)
Gamer.rank.a1.query.offset(2).limit(2).get ==> (List(3), 3, false)
```

Going backwards from the end:

```scala
// Last page - more pages before...
Gamer.rank.a1.query.limit(-2).offset(0).get ==> (List(2, 3), 3, true)

// Previous page -  is first page (no more before)
Gamer.rank.a1.query.limit(-2).offset(-2).get ==> (List(1), 3, false)
```

#### Reverse trick

Backwards offset pagination uses `offset = totalCount-limit` which on large data sets become inefficient. So instead of using the negative number (which is otherwise fine for small data sets), you can reverse the ordering, take the first `n` rows and then reverse the result:

```scala
// Slower
Gamer.rank.a1.query.limit(-2).offset(0).get._1 ==> List(2, 3)
// Faster
Gamer.rank.d1.query.limit(2).offset(0).get._1.reverse ==> List(2, 3)
```


## Cursor

Molecule uses "Cursor pagination" as a broad term for fetching `n` rows or a "page" after/before the last shown row.

To identify where a next page should start from, Molecule needs a cursor String applied to `from(<cursor>)`. Initially for the first page we simply give `from` an empty String. A new cursor String is then returned that we can use to fetch the next page:

```scala
// First page
val (page1, cursor1, hasMore1) = Gamer.rank.a1.query.from("").limit(2).get
page1 ==> List(1, 2)
hasMore1 ==> true // more pages

// Next page using cursor1 from first page
val (page2, cursor2, hasMore2) = Gamer.rank.a1.query.from(cursor1).limit(2).get
page2 ==> List(3)
hasMore2 ==> false // no more pages
```

We can also go backwards from the last page by using a negative `limit` number:

```scala
// Last page
val (page1, cursor1, hasMore1) = Gamer.rank.a1.query.from("").limit(-2).get
page1 ==> List(2, 3)
hasMore1 ==> true // more pages

// Previous page using cursor1 from last page
val (page2, cursor2, hasMore2) = Gamer.rank.a1.query.from(cursor1).limit(-2).get
page2 ==> List(1)
hasMore2 ==> false // no more pages
```

The cursor String returned contains various information encoded with Base64 that Molecule uses to identify the next page rows.

### Primary unique

The examples above work well since we have a result set sorted by the attribute `rank` that has been defined in our Data Model to be `unique`:

```scala
val rank = oneInt.unique
```

This guarantees that we can reliably filter out previously shown data for each next page - data onwards from the last shown row.

#### Id

The default auto-incremented `id` attribute that Molecule manages internally is a unique attribute too, that we can use to paginate rows in chronological order:

```scala
// Ids automatically inserted
Gamer.username.insert("Blaze", "Venom", "Phoenix").transact

// Use ids for pagination
val (page1, cursor1, _) = Gamer.id.a1.username.query.from("").limit(2).get
page1 ==> List(
  (1, "Blaze"),
  (2, "Venom"),
)

val (page2, _, _) = Gamer.id.a1.username.query.from(cursor1).limit(2).get
page2 ==> List(
  (3, "Phoenix"),
)
```

#### Date

If you want to sort and paginate by a `java.util.Date` attribute you might in rare cases get inconsistent paging results if multiple rows have dates within the same millisecond (the smallest unit of `Date`). The `Date` type is an example of what is often referred to as having "near-uniqueness" which normally works fine unless you have massive numbers of transactions going on that would increase the chances of occasional millisecond "collisions".


#### Deleted rows

What happens if rows are deleted before calling the next page?! Especially if the deleted rows are on the edge between this and the next page. Normally in most other systems, the guarantees of consistent pagination don't account for this situation and are therefore not supported.

Molecule on the other hand has some resilience built in that takes into account that one or a few rows can be deleted in between page calls and still deliver consistent results. Let's look at an examples of this:

```scala
val ids        = Gamer.rank.insert(1, 2, 3, 4, 5, 6, 7).transact.ids
val (id2, id5) = (ids(1), ids(4))

// First page
val (page1, cursor1, _) = Gamer.rank.a1.query.from("").limit(2).get
page1 ==> List(1, 2)

// Last row of previous page is deleted
Gamer(id2).delete.transact

// Next page unaffected
val (page2, cursor2, _) = Gamer.rank.a1.query.from(cursor1).limit(2).get
page2 ==> List(3, 4)

// First row of next page is deleted
Gamer(id5).delete.transact

// Next page simply skips deleted row
val (page3, _, _) = Gamer.rank.a1.query.from(cursor2).limit(2).get
page3 ==> List(6, 7) // deleted 5 is skipped
```

As you can see, even if rows are deleted before or after the page edge, consistent expected next pages are returned.

Molecule takes 3 rows on each side of the edge between pages into account for possible deletions. The same goes for updates which semantically is a deletion followed by an addition.

This is all something that you don't need to think about. Molecule transparently apply the most reliable strategy possible for each next page call.

### Secondary unique

Sometimes we want to sort primarily by a non-unique attribute and secondly by a unique attribute. Molecule can't then guarantee the same degree of pagination consistency but will most often be able to deliver correct pages:

```scala
Gamer.category.rank.insert(
  ("Arcade", 1),
  ("Arcade", 2),
  ("Action", 3),
).transact

val (page1, cursor1, _) = Gamer.category.a1.rank.a2.query.from("").limit(2).get
page1 ==> List(
  ("Action", 3), // "Action" sorted first
  ("Arcade", 1),
)

val (page2, _, _) = Gamer.category.a1.rank.a2.query.from(cursor1).limit(2).get
page2 ==> List(
  ("Arcade", 2),
)
```

### No unique


If the query is not sorted by any unique attribute, Molecule will still try to return as correct page results as possible. It does this by encoding hashed concatenated row values in the cursor String to fall back to as new identifiers to locate next pages. It's maybe not terribly performant, but it allows for unrestrained pagination that we haven't seen other systems offer and that gives you freedom to model and paginate as you like.



##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Pagination compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/pagination)

