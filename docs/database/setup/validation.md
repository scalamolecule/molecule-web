
# Validation

Attributes can be defined in your domain structure with a validation lambda of type <nobr>`<baseType> => Boolean`</nobr>. Molecule will then only transact valid values for the Attribute.

```scala
val intAttr       = oneInt.validate(_ > 2) // Int => Boolean
val localDateAttr = oneLocalDate
  .validate(_.compareTo(LocalDate.of(2002, 1, 1)) > 0) // LocalDate => Boolean
// etc for all primitive types
```

Validations even works for `Seq` and `Set` types, but not `Map` types.


```scala
// All values of Set validated
val setIntAttr       = setInt.validate(_ > 2)
val setLocalDateAttr = setLocalDate
  .validate(_.compareTo(LocalDate.of(2002, 1, 1)) > 0)
// etc
```


```scala
// All values of Seq validated
val seqIntAttr       = seqInt.validate(_ > 2)
val seqLocalDateAttr = seqLocalDate
  .validate(_.compareTo(LocalDate.of(2002, 1, 1)) > 0)
// etc
```

## Save errors

Saving a value with the synchronous api that doesn't satisfy the validation lambda will throw a ValidationErrors exception containing a Map of Attribute name to Seq of error message pairs. 

For the asynchronous api, a failed Future is returned also with the ValidationsErrors. 

And for the ZIO api, ValidationErrors is returned as the error type:

::: code-tabs#coord
@tab Sync
```scala
try {
  Type.int(1).save.transact
} catch {
  // ValidationErrors thrown
  case ValidationErrors(errorMap) =>
    errorMap ==>
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
}
```

@tab Async
```scala
Type.int(1).save.transact.recover {
  // Failed Future with ValidationErrors
  case ValidationErrors(errorMap) =>
    errorMap ==>
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
}
```

@tab ZIO
```scala
Type.int(1).save.transact.flip.map {
  // ZIO error of ValidationErrors
  case ValidationErrors(errorMap) => assertTrue(
    errorMap ==
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
  )
}
```
:::

## Insert errors

Insertion is a bit more complex since multiple rows of data can be inserted in a single transaction. So errors can occur for various rows and various Attributes. Here's an example of two rows failing validation (using the asynchronous api). Note how the coordinates is returned with the `InsertErrors` exception type containing Sequences of `InsertError`s for each failed row:

```scala
for {
  _ <- Type.int.long.insert(
      (0, 0L),
      (1, 1L),
    ).transact.recover {
      case InsertErrors(errors, _) =>
        errors ==> Seq(
          (
            0, // first row
            Seq(
              InsertError(
                0, // tuple index for `int` Attribute
                "Type.int",
                Seq(
                  s"""Type.int with value `0` doesn't satisfy validation:
                     |_ > 2
                     |""".stripMargin
                ),
                Nil
              ),
              InsertError(
                1, // tuple index for `long` Attribute
                "Type.long",
                Seq(
                  s"""Type.long with value `0` doesn't satisfy validation:
                     |_ > 2L
                     |""".stripMargin
                ),
                Nil
              )
            )
          ),
          (
            1, // second row
            Seq(
              InsertError(
                0, // tuple index for `int` Attribute
                "Type.int",
                Seq(
                  s"""Type.int with value `1` doesn't satisfy validation:
                     |_ > 2
                     |""".stripMargin
                ),
                Nil
              ),
              InsertError(
                1, // tuple index for `long` Attribute
                "Type.long",
                Seq(
                  s"""Type.long with value `1` doesn't satisfy validation:
                     |_ > 2L
                     |""".stripMargin
                ),
                Nil
              )
            )
          )
        )
    }
} yield ()
```

## Update errors

Validation of updates is similar to saves:

::: code-tabs#coord
@tab Sync
```scala
try {
  Type(id).int(1).update.transact
} catch {
  // ValidationErrors thrown
  case ValidationErrors(errorMap) =>
    errorMap ==>
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
}
```

@tab Async
```scala
Type(id).int(1).update.transact.recover {
  // Failed Future with ValidationErrors
  case ValidationErrors(errorMap) =>
    errorMap ==>
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
}
```

@tab ZIO
```scala
Type(id).int(1).update.transact.flip.map {
  // ZIO error of ValidationErrors
  case ValidationErrors(errorMap) => assertTrue(
    errorMap ==
      Map(
        "Type.int" -> Seq(
          s"""Type.int with value `1` doesn't satisfy validation:
             |_ > 2
             |""".stripMargin
        )
      )
  )
}
```
:::


## Custom errors

Custom error messages can be returned if validation fails
```scala
val withMsg = oneInt.validate(
  _ > 2,
  "One-line error msg"
)
```

`$v` can be used in error messages  and will be replaced by Molecule with the failed value.
```scala
val withValue = oneInt.validate(
  _ > 2,
  "One-line error msg. Found $v"
) 
// One-line error msg. Found 1
```

```scala
val multilineMsgWithValue = oneInt.validate((v: Int) => v.>(2),
  """Validation failed:
    |Input value $v is not bigger than 2.""".stripMargin
)
// Validation failed:
// Input value 1 is not bigger than 2.
```


## Validation logic

A validation body with multiple lines can be used
```scala
val multiLine  = oneInt.validate { v =>
  val data   = 22
  val result = data % 10
  v > result
}

```
And together with a custom error message too
```scala
val multiLine2 = oneInt.validate(
  { v =>
    val data   = 22
    val result = {
      data % 10
    }
    v > result
  },
  "One-line error msg. Failed value: $v"
)
```


## Multiple validations

Multiple validations and error messages can be added as a partial function:
```scala
val multipleErrors = oneInt.validate(
  {
    case v if v > 2  => "Test 1: Number must be bigger than 2. Found: $v"
    case v if v < 10 => "Test 2: Number must be smaller than 10. Found: $v"
    case v if v != 7 => "Test 3: Number must not be 7"
    case v if {
      // Comments in validation code blocks are transferred to boilerplate code
      val divider = 2
      v % divider == 1
    }                =>
      """Test 4: Number must
        |be odd. Found: $v""".stripMargin
  }: PartialFunction[Int, String] // (not needed in Scala 2.13 and 3.x)
)
```


## Attr references

Values of other Attributes can be used in validation logic:
```scala
val int        = oneInt
val noErrorMsg = oneInt.validate(_ > int.value)

val int1     = oneInt
val errorMsg = oneInt.validate(
  _ > int1.value,
  "One-line error msg"
)

val int2              = oneInt
val errorMsgWithValue = oneInt.validate(
  _ > int2.value,
  "One-line error msg. Found $v"
)

val int3         = oneInt
val multilineMsg = oneInt.validate((v: Int) => v.>(int3.value),
  """Validation failed:
    |Input value `$v` is not bigger than `int3` value `$int3`.""".stripMargin
)

val int4      = oneInt
val multiLine = oneInt.validate { v =>
  val data   = 22
  val result = data % int4.value
  v > result
}

val int5       = oneInt
val multiLine2 = oneInt.validate(
  { v =>
    val data   = 22
    val result = {
      data % int5.value
    }
    v > result
  },
  "One-line error msg"
)

val int6       = oneInt
val multiLine3 = oneInt.validate({ v =>
  val data   = 22
  val result = data % int6.value
  v > result
},
  """Long error explanation
    |with multiple lines""".stripMargin
)

val int7  = oneInt
val logic = oneInt.validate(
  v => v >= 3 && v <= 9 && v != int7.value && v % 2 == 1,
  "Value must be an odd number between 3 and 9 but not `int7` value `$int7`"
)

val int8           = oneInt
val str            = oneString
val intSet         = setInt
val strs           = setString
val multipleErrors = oneInt.validate(
  {
    case v if v > 4 =>
      "Test 1: Number must be bigger than 4. Found: $v"

    case v if v > int8.value =>
      "Test 2: Number must be bigger than `int8` value `$int8`. Found: $v"

    case v if v < str.value.length * 2 =>
      "Test 3: Number must be smaller than `str` value `$str` length `${str.length}` * 2. Found: $v"

    case v if {
      v != intSet.value.head - 3
    } => "Test 4: Number must not be `intSet` head value `${intSet.head}` minus 3. Found: $v"

    case v if {
      val divider = strs.value.size
      v % divider == 1
    } =>
      """Test 5: Number must
        |be odd. Found: $v""".stripMargin
  }: PartialFunction[Int, String] // Not needed in Scala 2.13 and 3.x
)
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Validation compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/validation)