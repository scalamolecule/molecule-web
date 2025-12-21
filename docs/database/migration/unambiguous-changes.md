# Unambiguous Changes

When you explicitly mark your migration intent using Molecule's migration commands, the system can immediately generate the correct SQL without any ambiguity.

## Adding attributes

Adding attributes can't cause any ambiguity, so it's safe to simply add them:

```scala
trait Person {
  val name = oneString
  val age  = oneInt // New attribute added
}
```
After running `sbt moleculeGen`, Molecule automatically generates the following SQL in the Flyway migration file:

```sql
ALTER TABLE Person ADD COLUMN age INTEGER;
```
Flyway files are automatically generated in `resources/db/migration/` and are named idiomatically with a version number in the beginning, e.g., `V1__initial_schema.sql`, `V2__molecule_1_change.sql`, etc.

## Removing attributes

To avoid ambiguity, mark attributes for removal with `.remove`:

```scala
trait Person {
  val name = oneString
  val age  = oneInt.remove // Mark for removal
}
```

**Generated SQL:**
```sql
ALTER TABLE Person DROP COLUMN age;
```

After SQL generation succeeds, Molecule automatically removes the entire attribute line from your domain structure using type-safe ScalaMeta AST transformations.

## Renaming attributes

Mark renames with `.rename("newName")`:

```scala
trait Person {
  val name = oneString.rename("fullName")  // Rename to fullName
}
```

**Generated SQL:**
```sql
ALTER TABLE Person RENAME COLUMN name TO fullName;
```

After SQL generation succeeds, Molecule automatically removes the `.rename()` marker from your domain structure using type-safe ScalaMeta AST transformations:
```scala
trait Person {
  val fullName = oneString
}
```

#### automatic cleanup
With renames, Molecule also prompts you if you want to automatically rename attribute usages across your entire codebase with type-safe ScalaMeta AST transformations (see [automatic cleanup](/database/migration/automatic-cleanup.html)).

## Adding entities (tables)

Adding entities (tables) can't cause any ambiguity, so it's safe to simply add them:

```scala
trait Person {
  val name = oneString
}

trait Company { // New entity
  val name = oneString
}
```

**Generated SQL:**
```sql
CREATE TABLE Company (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(255)
);
```

## Removing entities (tables)

Extend `Remove` trait:

```scala
trait Person {
  val name = oneString
}

trait Company extends Remove { // Mark for removal
  val name = oneString
}
```

**Generated SQL:**
```sql
DROP TABLE Company;
```

After SQL generation, Molecule automatically removes the entire entity definition from your domain structure using type-safe ScalaMeta AST transformations.

```scala
trait Person { 
  val name = oneString
}
```

## Renaming entities (tables)

Extend `Rename` traits:

```scala
trait User extends Rename("Visitor") { // Mark for rename
  val fullName = oneString
}
```

**Generated SQL:**
```sql
ALTER TABLE User RENAME TO Visitor;
```

After SQL generation, Molecule automatically removes the `Rename` marker from your domain structure using type-safe ScalaMeta AST transformations:
```scala
trait Visitor { 
  val fullName = oneString
}
```


#### automatic cleanup
With renames, Molecule also prompts you if you want to automatically rename attribute usages across your entire codebase with type-safe ScalaMeta AST transformations (see [automatic cleanup](/database/migration/automatic-cleanup.html)).


## Adding segments

Adding segments can't cause any ambiguity, so it's safe to simply add them:
Add new segment objects - no migration marker needed:

```scala
object social {
  trait User {
    val name = oneString
  }
}
object sales {  // New segment
  trait Customer {
    val name = oneString
  }
  trait Product {
    val name = oneString
  }
}
```

**Generated SQL** - the segment name is used as prefix for all tables in the segment:
```sql
CREATE TABLE sales_Customer (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(255)
);
CREATE TABLE sales_Product (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(255)
);
```

## Removing segments

Extend `Remove` marker trait for all entities in the segment:

```scala
object social {
  trait User {
    val name = oneString
  }
}
object sales extends Remove {  // Mark for removal
  trait Customer {
    val name = oneString
  }
  trait Product {
    val name = oneString
  }
}
```

**Generated SQL:**
```sql
DROP TABLE sales_Customer;
DROP TABLE sales_Product;
```

After SQL generation, Molecule automatically removes the entire segment object from your domain structure using type-safe ScalaMeta AST transformations.
```scala
object social {
  trait User {
    val name = oneString
  }
}
```

## Renaming segments

Extend `Rename` trait with the new segment name:

```scala
object sales extends Rename("company") {  // Mark for rename
  trait Customer {
    val name = oneString
  }
  trait Product {
    val name = oneString
  }
}
```

**Generated SQL:**
```sql
ALTER TABLE sales_Customer RENAME TO company_Customer;
ALTER TABLE sales_Product RENAME TO company_Product;
```

After SQL generation, Molecule automatically removes the `Rename` marker from your domain structure using type-safe ScalaMeta AST transformations:

```scala
object company {
  trait Customer {
    val name = oneString
  }
  trait Product {
    val name = oneString
  }
}
```


#### automatic cleanup
With renames, Molecule also prompts you if you want to automatically rename attribute usages across your entire codebase with type-safe ScalaMeta AST transformations (see [automatic cleanup](/database/migration/automatic-cleanup.html)).


## Changing attribute options

### Adding/removing indexes

```scala
trait Person {
  val email = oneString.index  // Add index
}
```

**Generated SQL:**
```sql
CREATE INDEX IF NOT EXISTS _Person_email ON Person (email);
```

Simply remove `.index` to drop it, and the index will be dropped with SQL:
```sql
DROP INDEX IF EXISTS _Person_email;
```

### Changing cascade behavior

Add or remove `.owner` on relationships:

```scala
trait Person {
  val company = manyToOne[Company].owner  // Add cascade delete
}
```

**Generated SQL:**
```sql
ALTER TABLE Person DROP CONSTRAINT _company;
ALTER TABLE Person ADD CONSTRAINT _company
  FOREIGN KEY (company) REFERENCES Company (id) ON DELETE CASCADE;
```

See [Relationships](/database/migration/relationships.html) for more details.

## Automatic cleanup

After successful SQL generation, Molecule automatically removes migration markers from your domain structure using type-safe ScalaMeta AST transformations. For renames, you are then prompted whether to automatically rename attribute usages across your entire codebase.

See [Automatic cleanup](/database/migration/cleanup.html) for full details.

## Workflow summary

1. Make changes in your domain structure with migration markers
2. Run `sbt moleculeGen`
3. Flyway migration files are generated in `resources/db/migration/...`
4. Migration markers are automatically removed from domain structure
5. For renames: optionally accept automatic renaming of attribute usages across your codebase
6. Optionally review the generated Flyway migration file
7. Apply migrations with Flyway
8. Use newly generated Molecule boilerplate code to compose molecules with the new schema

## Next steps

- [Ambiguous changes](/database/migration/ambiguous-changes.html) - Handle unclear scenarios
- [Relationships](/database/migration/relationships.html) - Migrate foreign keys
- [Reference](/database/migration/reference.html) - Complete API reference
