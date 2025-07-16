# Inspection

Molecule queries are translated to raw query strings for each database and we can inspect what a molecule translates into with the command `inspect` called on `query`:

```scala
Person.name.age.query.inspect // (returns Unit)
```

This will print the MetaModel of the molecule and the produced raw query for the database to the console without querying for the data.

Here, we call it when using the H2 database, and we see the produced raw SQL query: 

```
========================================
QUERY:
AttrOneManString("Person", "name", V, Seq(), None, None, Nil, Nil, None, None, Seq(0, 1))
AttrOneManInt("Person", "age", V, Seq(), None, None, Nil, Nil, None, None, Seq(0, 2))

SELECT DISTINCT
  Person.name,
  Person.age
FROM Person
WHERE
  Person.name IS NOT NULL AND
  Person.age  IS NOT NULL;
----------------------------------------
```


## `i`

When working on molecules we might want to do a quick check of the raw query produced without having to change our code. Then we can simply add `i` (for "inspect") to `query` and apart from returning the result also print the same info as shown above.

```scala
Person.name.age.query.i.get ==> List(
  ("Bob", 42),
  ("Liz", 38),
)
```



##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Inspection compliance test (H2)](https://github.com/scalamolecule/molecule/blob/main/db/h2/shared/src/test/scala/molecule/db/h2/compliance/inspection/Inspect.scala)