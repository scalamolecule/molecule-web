# Advanced

This page covers complex migration scenarios and edge cases that require special handling.

## Type changes are disallowed

Changing an attribute's type is **not allowed** because it requires data transformation logic:

```scala
// Before
trait Person {
  val age = oneInt  // Integer age
}

// After - DISALLOWED!
trait Person {
  val age = oneString  // String age
}
```

**Error:**
```
-- ERROR: Attribute type/cardinality changes are not allowed.

The following attributes have changed their type or cardinality:
  - Person.age: oneInt → oneString

Type/cardinality changes require manual data migration:
1. Create new attribute: val ageNew = oneString
2. Write data conversion logic (e.g., age.toString)
3. Verify all data is migrated correctly
4. Remove old attribute: val age = oneInt.remove
5. Optionally rename: val ageNew = oneString.rename("age")
```

### Manual type change workflow

1. **Add new attribute** with a different name:
```scala
trait Person {
  val age = oneInt
  val ageStr = oneString  // New attribute
}
```

2. **Run migration** to add the column

3. **Transform data** using Molecule's query/update API:
```scala
Person.id.age.query.get.foreach { case (id, age) =>
  Person(id).ageStr(age.toString).update.transact
}
```

4. **Verify** all data is converted

5. **Remove old attribute**:
```scala
trait Person {
  val age = oneInt.remove
  val ageStr = oneString
}
```

6. **Run migration**, then optionally **rename**:
```scala
trait Person {
  val ageStr = oneString.rename("age")
}
```

## Cardinality changes are disallowed

Similar to type changes, cardinality changes require manual handling:

```scala
// Before
trait Person {
  val tags = manyString
}

// After - DISALLOWED!
trait Person {
  val tags = oneString  // Changed from many to one
}
```

**Why?** Going from `manyString` to `oneString` requires logic to:
- Select which value to keep
- Handle records with no values
- Handle records with multiple values

This cannot be safely automated.

## Cyclical renames

Molecule detects circular rename patterns that would cause data corruption.

### Two-way swaps (name swaps)

```scala
trait Person {
  val email = oneString.rename("phone")
  val phone = oneString.rename("email")
}
```

**Error:**
```
-- ERROR: Cyclical rename detected.

The following attribute pairs are being swapped:
  Person.email ↔ Person.phone

This would cause data corruption. To swap attributes:
1. Rename first attribute to temporary name:
   val email = oneString.rename("email_temp")
2. Run migration
3. Rename second attribute:
   val phone = oneString.rename("email")
4. Run migration
5. Rename temporary attribute:
   val email_temp = oneString.rename("phone")
6. Run migration
```

### N-way cycles

Longer cycles are also detected:

```scala
trait Person {
  val a = oneString.rename("b")
  val b = oneString.rename("c")
  val c = oneString.rename("a")  // A→B→C→A cycle
}
```

**Error:**
```
-- ERROR: Cyclical rename detected.

The following attributes form a rename cycle:
  Person.a → Person.b → Person.c → Person.a

Break the cycle using a temporary name for one attribute.
```

### Valid rename chains

Linear rename chains are allowed:

```scala
trait Person {
  val a = oneString.rename("b")  // OK: A→B (no cycle)
}
```

## Multi-level cascading migrations

When you rename segments, entities, and attributes together, SQL is generated in the correct order:

```scala
object sales extends Rename("marketing") {  // Will become "marketing"
  trait Customer extends Rename("Client") {  // Will become "Client"
    val name = oneString.rename("fullName")
  }
}
```

**Generated SQL (correct order):**
```sql
-- Step 1: Rename segment (table prefix)
ALTER TABLE sales_Customer RENAME TO marketing_Customer;

-- Step 2: Rename entity (within renamed segment)
ALTER TABLE marketing_Customer RENAME TO marketing_Client;

-- Step 3: Rename attribute (within renamed entity)
ALTER TABLE marketing_Client RENAME COLUMN name TO fullName;
```

The migration system automatically orders operations to prevent broken references.

## Combining multiple operations

You can perform many operations in a single migration:

```scala
object Marketing {  // Renamed from Sales
  
  trait Client {  // Renamed from Customer
    val fullName = oneString  // Renamed from name
    val email = oneString.index  // Added index
    val age = oneInt.remove  // Removed
    val phone = oneString  // Added
    val company = manyToOne[Organization].owner  // Added with cascade
  }
  
  trait Organization {  // New entity
    val name = oneString
  }
  
  trait Vendor extends Remove {  // Removed
    val name = oneString
  }
}
```

All migrations are applied in the correct dependency order.

## Complex relationship scenarios

### Self-references

```scala
trait Person {
  val name = oneString
  val manager = manyToOne[Person]  // Self-reference
}
```

Self-referencing relationships work just like regular relationships.

### Multiple relationships to same entity

```scala
trait Order {
  val customer = manyToOne[Person]
  val billingContact = manyToOne[Person]
  val shippingContact = manyToOne[Person]
}
```

All three relationships are independent and can be migrated separately.

## Data transformation during migration

For complex data transformations (like splitting or merging columns), use Molecule's query/update API:

### Example: Splitting a column

```scala
// Step 1: Add new columns
trait Person {
  val fullName = oneString
  val firstName = oneString  // New
  val lastName = oneString   // New
}
```

Run migration to add columns.

```scala
// Step 2: Transform data
Person.id.fullName.query.get.foreach { case (id, fullName) =>
  val parts = fullName.split(" ", 2)
  Person(id)
    .firstName(parts(0))
    .lastName(parts.lift(1).getOrElse(""))
    .update.transact
}
```

```scala
// Step 3: Remove old column
trait Person {
  val fullName = oneString.remove
  val firstName = oneString
  val lastName = oneString
}
```

Run final migration.

## Next steps

- [Reference](/database/migration/reference.html) - Complete API reference
- [Automatic cleanup](/database/migration/cleanup.html) - AST-based cleanup
