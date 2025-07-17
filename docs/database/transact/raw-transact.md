---
next: /docs/query/attributes
---

# Raw Transact

Molecule transactions are translated to prepared statements for SQL databases (with input placeholders).

## `inspect`

[//]: # (`inspect` a transaction molecule without transacting any data: )

Inspect a query molecule without returning data by adding `inspect` after `<action>`:

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 1")
  .save.inspect // (returns Unit)
```

This will print the MetaModel of the molecule and for SQL, the produced prepared statements for each table:

```
========================================
SAVE:
AttrOneManString("Person", "name", Eq, Seq("Bob"), None, None, Nil, Nil, None, None, Seq(0, 1))
AttrOneManInt("Person", "age", Eq, Seq(42), None, None, Nil, Nil, None, None, Seq(0, 4))
Ref("Person", "home", "Address", CardOne, false, Seq(0, 6, 1))
AttrOneManString("Address", "street", Eq, Seq("Main st. 1"), None, None, Nil, Nil, None, None, Seq(1, 12))

Save(
  Ns(
    RefOne(
      INSERT INTO Address (
        street
      ) VALUES (?)
    )
    ---------------------------
    INSERT INTO Person (
      name,
      age,
      home
    ) VALUES (?, ?, ?)
  )
)
----------------------------------------
```


## `i`

When working on molecules we might want to do a quick check of the prepared statements produced without having to change our code. Then we can simply add `i` (for "inspect") to `transact` and apart from transacting the data also print the same info as shown above.

```scala
Person.name("Bob").age(42)
  .Home.street("Main st. 1")
  .save.i.transact // adding `i` to also inspect
```


## `rawTransact`

For SQL databases we could insert raw data with an insert:

```scala
rawTransact(
  """INSERT INTO Person (
    |  name,
    |  age
    |) VALUES ('Bob', 42)""".stripMargin
)
```
We can confirm if the data has been inserted:

```scala
Person.name.age.query.get ==> List(
  ("Bob", 42),
)
```

Likewise, we could update or delete raw data using raw transaction strings for the selected database.


See also [raw query](/database/query/raw-query).


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Raw transaction compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/validation)