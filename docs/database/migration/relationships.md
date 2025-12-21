# Relationships

Molecule fully supports migrations for foreign key relationships (`manyToOne`), including adding, removing, renaming relationships, and changing cascade behavior.

## Adding relationships

Simply add a `manyToOne` relationship to your entity:

```scala
trait Person {
  val name = oneString
  val company = manyToOne[Company]  // New relationship
}

trait Company {
  val name = oneString
}
```

**Generated SQL:**
```sql
ALTER TABLE Person ADD COLUMN company BIGINT;
ALTER TABLE Person ADD CONSTRAINT _company
  FOREIGN KEY (company) REFERENCES Company (id);
CREATE INDEX IF NOT EXISTS _Person_company ON Person (company);
```

The migration creates:
1. The foreign key column
2. The foreign key constraint
3. An index on the foreign key column (for query performance)

## Removing relationships

Mark relationships for removal with `.remove`:

```scala
trait Person {
  val name = oneString
  val company = manyToOne[Company].remove  // Mark for removal
}
```

**Generated SQL:**
```sql
DROP INDEX IF EXISTS _Person_company;
ALTER TABLE Person DROP CONSTRAINT _company;
ALTER TABLE Person DROP COLUMN company;
```

Operations occur in the correct order:
1. Drop the index first
2. Drop the foreign key constraint
3. Finally drop the column

After SQL generation succeeds, Molecule automatically removes the entire relationship line from your domain structure using type-safe ScalaMeta AST transformations:

```scala
trait Person {
  val name = oneString
}
```


## Renaming relationships

Mark renames with `.rename("newName")`:

```scala
trait Person {
  val name = oneString
  val company = manyToOne[Company].rename("employer")  // Rename to employer
}
```

**Generated SQL:**
```sql
DROP INDEX IF EXISTS _Person_company;
ALTER TABLE Person DROP CONSTRAINT _company;
ALTER TABLE Person RENAME COLUMN company TO employer;
ALTER TABLE Person ADD CONSTRAINT _employer
  FOREIGN KEY (employer) REFERENCES Company (id);
CREATE INDEX IF NOT EXISTS _Person_employer ON Person (employer);
```

After SQL generation succeeds, Molecule automatically removes the `.rename()` marker from your domain structure using type-safe ScalaMeta AST transformations. You then manually complete the rename:
```scala
trait Person {
  val name = oneString
  val employer = manyToOne[Company]  // Renamed from 'company'
}
```

Optionally, you can accept automatic renaming of relationship usages across your entire codebase (see [automatic cleanup](/database/migration/cleanup.html)).

## Cascade behavior with `.owner`

The `.owner` option on relationships controls cascade delete behavior.

### Adding `.owner`

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

With `.owner`, when a `Company` is deleted, all associated `Person` records are automatically deleted.

### Removing `.owner`

```scala
trait Person {
  val company = manyToOne[Company]  // Remove .owner option
}
```

**Generated SQL:**
```sql
ALTER TABLE Person DROP CONSTRAINT _company;
ALTER TABLE Person ADD CONSTRAINT _company
  FOREIGN KEY (company) REFERENCES Company (id);
```

Without `.owner`, deleting a `Company` will fail if any `Person` records reference it.

## Changing target entity type

Changing the target entity type is **not allowed**:

```scala
// Before
trait Person {
  val company = manyToOne[Company]
}

// After - DISALLOWED!
trait Person {
  val company = manyToOne[Organization]  // Different target type
}
```

**Error:**
```
-- ERROR: Attribute type/cardinality changes are not allowed.

The following attributes have changed their type or cardinality:
  - Person.company: manyToOne[Company] → manyToOne[Organization]

For relationships, changing the target entity requires:
1. Create new relationship: val companyNew = manyToOne[Organization]
2. Migrate FK references with custom mapping logic
3. Verify migrations
4. Remove old: val company = manyToOne[Company].remove
5. Rename: val companyNew = manyToOne[Organization].rename("company")
```

Target type changes fundamentally alter the referential integrity semantics and require manual data migration.

#### Manual target type change workflow

1. **Add new relationship** with a different name:
```scala
trait Person {
  val company = manyToOne[Company]
  val employer = manyToOne[Organization]  // New relationship
}
```

2. **Run migration** to add the new foreign key column

3. **Write data migration logic** to populate the new foreign key. This requires your application-specific mapping logic:
```scala
// Example: Map Company IDs to Organization IDs
Person.company.query.get.foreach { case (personId, companyId) =>
  val orgId = mapCompanyToOrganization(companyId)  // Your mapping logic
  Person(personId).employer(orgId).update.transact
}
```

4. **Verify** all data is migrated correctly

5. **Remove old relationship**:
```scala
trait Person {
  val company = manyToOne[Company].remove
  val employer = manyToOne[Organization]
}
```

6. **Run migration**, then optionally **rename**:
```scala
trait Person {
  val employer = manyToOne[Organization].rename("company")
}
```

This approach gives you full control over the foreign key mapping logic while ensuring data integrity.

## Combining relationship operations

You can perform multiple relationship migrations together:

```scala
trait Person {
  val name = oneString
  val company = manyToOne[Company].remove      // Remove this one
  val employer = manyToOne[Organization].owner  // Add new one with cascade
  val manager = manyToOne[Person]               // Add self-reference
}
```

All SQL operations are generated in the correct dependency order.

## Ambiguous relationship changes

If you remove a relationship without `.remove`, Molecule generates a Resolution file:

```scala
// Before
trait Person {
  val company = manyToOne[Company]
}

// After (forgot .remove marker!)
trait Person {
  val name = oneString
}
```

**Generated Resolution file:**
```scala
trait Person_migration extends Person with DomainStructure {

  trait PersonMigrations extends Person {
    val company = manyToOne[Company].remove // if removed
    val company = manyToOne[Company].becomes() // if renamed
  }
}
```

Choose the correct option in the Resolution file, then regenerate. See [Ambiguous changes](/database/migration/ambiguous-changes.html) for details.

## Next steps

- [Advanced](/database/migration/advanced.html) - Complex scenarios
- [Reference](/database/migration/reference.html) - Complete API reference
