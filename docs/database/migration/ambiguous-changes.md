# Ambiguous Changes

When running `sbt moleculeGen` Molecule compares your domain structure with the previous structure to detect changes. If it cannot determine your intent of a change, it throws an error and generates a **Resolution file** to guide you in resolving the ambiguity.

## When ambiguity occurs

Ambiguous scenarios happen when an **attribute or relationship** has _**disappeared**_ from your domain structure without explicit migration markers.

Molecule cannot then know if you intended to **remove** or **rename** the disappeared element and generates a Resolution file to help you resolve the ambiguity.

**Important:** For **entities and segments**, Molecule requires explicit markers (`extends Remove` or `extends Rename("newName")`) directly in your domain file—no Resolution files are generated. This design ensures intentional, well-considered changes at the structural level.

## Example: Ambiguous attribute change

### The scenario

```scala
// Before (previous schema)
trait Person {
  val name = oneString
}

// After (your changes - forgot migration marker!)
trait Person {
  val fullName = oneString
}
```

You run `sbt moleculeGen` and get an error:

```
-- ERROR: Schema changes detected but explicit migration commands are missing.

The following attributes have been removed without calling `.remove` or `.rename("newName")`:
  Person.name
```

### Generated Resolution file

Molecule can't determine if you intended to delete or rename the attribute and therefore generates a Resolution file `Person_migration.scala` with **both options** to help you resolve the ambiguity:


```scala
trait Social_migration extends Social with DomainStructure {

  // Please choose intended migration commands:
  // (comment-out or delete unwanted option lines)

  trait PersonMigrations extends Person {
    val name = oneString.remove // if removed
    val name = oneString.becomes() // if renamed
  }
}
```
You then comment out or delete the unintended option line. Since both options can't compile together, you have to resolve the ambiguity before continuing. This guarantees that ambiguities are not left unresolved.

## Resolving ambiguity

### Option 1: You intended to remove

Comment out or delete the `.becomes()` line:

```scala
trait Social_migration extends Social with DomainStructure {

  trait PersonMigrations extends Person {
    val name = oneString.remove // Keep this
    // val name = oneString.becomes() // Delete or comment out
  }
}
```

Run `sbt moleculeGen` again. Generated SQL:

```sql
ALTER TABLE Person DROP COLUMN name;
ALTER TABLE Person ADD COLUMN fullName VARCHAR(255);
```

### Option 2: You intended to rename

Comment out or delete the `.remove` line and fill in the target attribute (we extend `Person` to be able to type-safely access `fullName` instead of writing the name as a text string):

```scala
trait Social_migration extends Social with DomainStructure {

  trait PersonMigrations extends Person {
    // val name = oneString.remove // Delete or comment out
    val name = oneString.becomes(fullName) // Keep this and add the target attribute
  }
}
```

Run `sbt moleculeGen` again. Generated SQL:

```sql
ALTER TABLE Person RENAME COLUMN name TO fullName;
```

#### automatic cleanup
With renames, Molecule also prompts you if you want to automatically rename attribute usages across your entire codebase with type-safe ScalaMeta AST transformations (see [automatic cleanup](/database/migration/automatic-cleanup.html)).


## After resolution

Once migration SQL is generated successfully:

1. **Apply the migration** with Flyway
2. Molecule automatically deletes the resolution file (`Person_migration.scala`)
3. Molecule prompts you if you want it to automatically rename attributes across you application code with type-safe ScalaMeta AST transformation


## Multiple ambiguities

If multiple elements are ambiguous, the Resolution file includes all of them:

```scala
trait Social_migration extends Social with DomainStructure {

  trait PersonMigrations extends Person {
    val name = oneString.remove
    val name = oneString.becomes()

    val age = oneInt.remove
    val age = oneInt.becomes()
  }
}
```

Resolve each pair independently by choosing the correct option for each.

## Entity and segment changes

For entities and segments, Molecule **does not generate Resolution files**. Instead, you must explicitly add migration markers directly to your domain file.

### Entity disappears

```scala
// Before
trait Company {
  val name = oneString
}

// After (forgot marker!)
trait Organization {
  val name = oneString
}
```

**Error:**
```
-- ERROR: Schema changes detected but explicit migration commands are missing.

The following entities have been removed without extending `Remove` or `Rename("newName")`:
  Company
```

**Solution:** Add the marker directly in your domain file:

**To remove:**
```scala
trait Company extends Remove {
  val name = oneString
}
```

**To rename:**
```scala
trait Company extends Rename("Organization") {
  val name = oneString
}
```

Then run `sbt moleculeGen` again.

### Segment disappears

```scala
// Before
object sales {
  trait Customer { 
    val name = oneString 
  }
}

// After (forgot marker!)
object marketing {
  trait Customer { 
    val name = oneString 
  }
}
```

**Error:**
```
-- ERROR: Schema changes detected but explicit migration commands are missing.

The following segments have been removed without extending `Remove` or `Rename("newName")`:
  sales
```

**Solution:** Add the marker directly in your domain file:

**To remove:**
```scala
object sales extends Remove {
  trait Customer { 
    val name = oneString 
  }
}
```

**To rename:**
```scala
object sales extends Rename("marketing") {
  trait Customer {
    val name = oneString 
  }
}
```

Then run `sbt moleculeGen` again.

**Why no Resolution files for entities/segments?** Entities and segments affect entire tables or table groups - a significant structural change. Requiring explicit markers in your domain file ensures intentional, well-considered decisions at this level.

## Best practice: Avoid ambiguity

Always use explicit migration markers.



## Next steps

- [Unambiguous changes](/database/migration/unambiguous-changes.html) - Learn the migration markers
- [Advanced](/database/migration/advanced.html) - Type changes, cyclical renames
