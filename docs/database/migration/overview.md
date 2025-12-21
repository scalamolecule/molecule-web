# Migration overview


Molecule provides automatic, type-safe database migration integration with Flyway. 


## Development

During early development we want to freely experiment with our domain structure and database schema. Molecule generates an SQL schema from your domain structure each time you run that you can use to test your ideas:

```bash
sbt moleculeGen
```
The SQL schema files are saved in `resources/db/schema/` for each domain and database dialect.

## Production

Then, when your domain has stabilized and you're ready to deploy to production, Molecule allows you to turn on migration handling with:

```bash
sbt moleculeGen --init-migrations
```
From now on Molecule will also generate Flyway migration files for each domain and database dialect in `resources/db/migration/`.

The current SQL schema files continue to be generated in `resources/db/schema/` for testing purposes.

## Why Molecule migrations?

Most Scala projects use migration tools like Flyway or Liquibase, where you manually write SQL migration files for each schema change. This works well but requires:
- Writing SQL by hand for every change
- Keeping migration SQL in sync with your domain structure
- Maintaining separate migration files per database dialect

#### Molecule's approach

_**Let your Scala domain structure be your type-safe source of truth!**_ 

This allows type-safe schema evolution without having to write manual SQL (although you can always do that too if you want, for edge cases for instance).



## How it works

1. **Make domain changes** - Modify your domain structure
2. **Run `sbt moleculeGen`** - Molecule compares your new domain structure with your previous structure
3. **SQL is generated** - Flyway migration file is automatically created
4. **Apply with Flyway** - Run Flyway migration to update the database

This allows you to evolve your domain structure without manually writing SQL.

## Managing migrations

### Initialize migrations

Enable migration tracking for your domain(s):

```bash
# Initialize for all domains
sbt moleculeGen --init-migrations

# Initialize for the Company domain only
sbt moleculeGen --init-migrations:Company

# Initialize for multiple domains
sbt moleculeGen --init-migrations:Company,Social
```

This creates the initial migration file (V1__initial_schema.sql) and starts tracking future changes.

### Check migration status

See which domains have migration handling enabled:

```bash
sbt moleculeMigrationStatus
```

```
Active migrations:
  app.domain.Company - latest: V2__molecule_1_change.sql
  app.domain.Social  - latest: V1__initial_schema.sql
```


### Disable migrations

Remove migration tracking (back to development mode):

```bash
# Remove all migration tracking
sbt moleculeGen --delete-migrations

# Remove for the Company domain only
sbt moleculeGen --delete-migrations:Company

# Remove for multiple domains
sbt moleculeGen --delete-migrations:Company,Social
```

## What can be migrated

Molecule tracks changes at all levels:

**Segments** - Add, remove, or rename segments of entities

**Entities** - Add, remove, or rename entities

**Attributes** - Add, remove, or rename attributes, plus manage `.index` option

**Relationships** - Add, remove, or rename foreign keys, plus manage `.owner` cascade behavior

## Two migration paths

### Unambiguous migrations
When you explicitly mark your intent with migration commands, Molecule can generate migration SQL immediately:

```scala
// Mark that 'name' should be renamed to 'fullName'
val name = oneString.rename("fullName")
```

Run `sbt moleculeGen` and migration SQL is generated.

### Ambiguous migrations
When Molecule detects changes but can't infer your intent, it generates a **Resolution file**:

```scala
// You removed 'name' and added 'fullName' - but was it renamed or replaced?
trait PersonMigrations extends Person {
  val name = oneString.remove          // if removed
  val name = oneString.becomes(fullName) // if renamed
}
```
By commenting out or deleting one of the two options for an attribute, you tell Molecule which migration path you intended to take. Once resolved, you can then run `sbt moleculeGen` again to unambiguously produce the migration file. 

This guarantees that your database schema is always in sync with your domain structure in a type-safe and unambiguous manner.

## Type safety

The migration system prevents dangerous operations:

- **Type changes** are disallowed (e.g., `oneInt` → `oneString`)
- **Cardinality changes** are disallowed (e.g., `oneString` → `manyString`)
- **Cyclical renames** are detected (e.g., swapping `email` and `phone`, or A→B→C→A)

These scenarios require manual data transformation and cannot be safely automated.

## Database support

Flyway migration files are generated for all database dialects supported by Molecule:

- H2
- PostgreSQL
- MySQL
- MariaDB
- SQLite (requires version 3.35.0+ for full support)

Molecule automatically handles all database-specific details including SQL syntax variations, reserved word conflicts (by appending `_`), type mappings, and foreign key constraints. You don't need to worry about database differences—Molecule generates the correct SQL for your target database.

## Next steps

- [Unambiguous changes](/database/migration/unambiguous-changes.html) - Learn the migration markers
- [Ambiguous changes](/database/migration/ambiguous-changes.html) - Resolve unclear scenarios
- [Automatic cleanup](/database/migration/cleanup.html) - Optional AST-based cleanup of migration markers
- [Relationships](/database/migration/relationships.html) - Migrate foreign keys
