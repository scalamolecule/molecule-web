# Raw Query

Molecule queries are translated to raw query strings for each database.



## `rawQuery`

If we want to access the database directly we can call `rawQuery` with a raw query String. We could for instance tweak a raw query from some inspection, here we simply use the above raw query as-is:

```scala
val rawResult: List[List[Any]] = rawQuery(
  """SELECT DISTINCT
    |  Person.name,
    |  Person.age
    |FROM Person
    |WHERE
    |  Person.name IS NOT NULL AND
    |  Person.age  IS NOT NULL;""".stripMargin
)

rawResult ==> List(
  List("Bob", 42),
  List("Liz", 38)
)
```
Notice the return type of `List[List[Any]]` from `rawQuery`. We then need to cast it ourselves if we want to use the raw result.

Set the second `debug` parameter of `rawQuery` to `true` to output debug information too:


```scala
rawQuery(
  """SELECT DISTINCT
    |  Person.name,
    |  Person.age
    |FROM Person
    |WHERE
    |  Person.name IS NOT NULL AND
    |  Person.age  IS NOT NULL;""".stripMargin,
  true // print debug info
)
```
3 groups of information is then printed to the console:

- Raw query (as supplied)
- The raw result, a `List[List[Any]]`
- Raw type information received from the database

```
=============================================================================
SELECT DISTINCT
  Person.name,
  Person.age
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;
  
List(
  List(Bob, 42),
  List(Liz, 38)
)

Column    Raw type                   Db type
------------------------------------------------------
NAME      class java.lang.String     CHARACTER VARYING
AGE       class java.lang.Integer    INTEGER
```

Currently, input parameters using `?` placeholders in the query is not supported by `rawQuery`. You would then need to use raw JDBC code.

See also [raw transact](/docs/transact/raw-transact).