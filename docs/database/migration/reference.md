# Reference

Quick reference for all migration commands and markers.

## Attribute migrations

```scala
// Add
val email = oneString

// Remove
val age = oneInt.remove

// Rename
val name = oneString.rename("fullName")

// Add index
val email = oneString.index

// Remove index (just remove .index)
val email = oneString
```

## Entity migrations

```scala
// Remove
trait Company extends Remove {
  val name = oneString
}

// Rename
trait OldEntityName extends Rename("NewEntityName") {
  val name = oneString
}
```

## Segment migrations

```scala
// Remove
object sales extends Remove {
  trait Customer { val name = oneString }
}

// Rename
object sales extends Rename("marketing") {
  trait Customer { val name = oneString }
}
```

## Relationship migrations

```scala
// Add
val company = manyToOne[Company]

// Remove
val company = manyToOne[Company].remove

// Rename
val company = manyToOne[Company].rename("employer")

// Add cascade delete
val company = manyToOne[Company].owner

// Remove cascade delete (just remove .owner)
val company = manyToOne[Company]
```

## Resolution files (ambiguous changes)

When Molecule can't determine your intent (attribute/relationship disappeared without marker), it generates a Resolution file:

```scala
trait EntityName_migration extends EntityName with DomainStructure {
  trait EntityNameMigrations extends EntityName {
    val attrName = oneType.remove           // Choose this if removed
    val attrName = oneType.becomes(newAttr) // OR this if renamed
  }
}
```

Choose one option, delete the other, run `sbt moleculeGen` again.

**Note:** Resolution files are only for attributes/relationships. Entities and segments require explicit markers.

## Disallowed operations

These require manual migration (see [Advanced](/database/migration/advanced.html)):

- **Type changes**: `val age = oneInt` → `oneString`
- **Cardinality changes**: `val tags = manyString` → `oneString`
- **Cyclical renames**: `val a = oneString.rename("b")` + `val b = oneString.rename("a")`
- **Relationship target changes**: `manyToOne[Company]` → `manyToOne[Organization]`

## Next steps

- [Unambiguous changes](/database/migration/unambiguous-changes.html) - Learn migration markers
- [Ambiguous changes](/database/migration/ambiguous-changes.html) - Resolution files
- [Advanced](/database/migration/advanced.html) - Complex scenarios
