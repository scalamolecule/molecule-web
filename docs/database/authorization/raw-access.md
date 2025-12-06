# Raw Access

Raw SQL actions provide fallback methods for advanced use cases not covered by Molecule's query API. They operate **outside** the 4-layer authorization model.

## Raw actions

```scala
trait rawQuery extends Action    // Raw SQL SELECT queries (read-only)
trait rawTransact extends Action // Raw SQL mutations (dangerous!)
```

**Key difference from core actions:**
- Core actions (query, save, insert, update, delete) check entity and attribute permissions
- Raw SQL actions check **ONLY** the role's action permissions - no entity/attribute restrictions apply

## rawQuery - Read-Only SQL

Allows running arbitrary SELECT queries where you need specialized SQL features not supported by Molecule, like for instance:

- Window functions (OVER, PARTITION BY, ROW_NUMBER, etc.)
- Subqueries (correlated and non-correlated)
- Common Table Expressions (CTEs/WITH clauses)
- UNION/INTERSECT/EXCEPT set operations
- Recursive queries
- Database-specific functions (PostgreSQL array operations, JSON functions, full-text search, etc.)
- Advanced aggregations (ROLLUP, CUBE, GROUPING SETS)
- Lateral joins
- Pivoting/unpivoting data

**Warning:** Raw queries can read ANY data in the database, bypassing all entity/attribute restrictions!

### Role Definition

Extend `rawQuery` to let a role run raw SQL queries:
```scala
trait Analyst extends Role with query with rawQuery
```

### Example: Window Function

```scala
val analystConn = baseConn.withAuth("analyst1", "Analyst")

// Use window function to calculate running total
val results = conn.rawQuery(
  """
    |SELECT
    |  saleDate,
    |  amount,
    |  SUM(amount) OVER (ORDER BY saleDate) as running_total
    |FROM Sale
    |ORDER BY saleDate
  """.stripMargin
)(using analystConn)
```

This is legitimate use - Molecule doesn't have built-in window functions, so raw SQL is necessary.

### Access Denied

Roles without `rawQuery` are denied:

```scala
trait Guest extends Role with query  // No rawQuery

val guestConn = baseConn.withAuth("guest1", "Guest")

conn.rawQuery("SELECT * FROM Sale")(using guestConn)
// Error: Access denied: Role 'Guest' does not have rawQuery access
```

### Return type

`rawQuery` returns `List[List[Any]]` - each row is a `List[Any]` where you need to cast the values yourself:

```scala
val results: List[List[Any]] = conn.rawQuery(
  "SELECT saleDate, amount FROM Sale WHERE amount > 100"
)(using analystConn)

// Cast each row's values
results.foreach { row =>
  val date = row(0).asInstanceOf[LocalDate]
  val amount = row(1).asInstanceOf[Double]
  println(s"Sale on $date: $amount")
}
```

This is the trade-off for raw SQL flexibility - you lose Molecule's type safety and must handle casting manually.

## rawTransact - SQL Mutations

Allows running arbitrary SQL mutations (INSERT, UPDATE, DELETE, DDL). Extremely dangerous because it:
- Bypasses ALL entity/attribute-level authorization
- Can modify any table in the database
- Can escalate privileges (e.g., change user roles)
- Can perform DDL operations (ALTER, DROP tables)

**_Should only be granted to trusted admin/DBA roles!_**

### Role Definition

```scala
trait Admin extends Role with query with save with insert with update with delete
  with rawQuery with rawTransact
```

### Example: Complex Bulk Update

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")

// Complex bulk update with CASE logic
conn.rawTransact(
  """
    |UPDATE Sale
    |SET amount = CASE
    |  WHEN region = 'North' AND amount > 150 THEN amount * 0.9
    |  WHEN region = 'South' AND saleDate < '2024-01-03' THEN amount * 0.85
    |  WHEN region = 'East' THEN amount * 0.95
    |  ELSE amount
    |END
  """.stripMargin
)(using adminConn)
```

This level of conditional logic is not possible to express in Molecule's API, so raw SQL is necessary.

## Dangers of raw SQL

### Bypasses Attribute Restrictions

```scala
trait Account extends Member with Admin {
  val username = oneString
  val privateNotes = oneString.only[Admin]  // Only Admin should see this
}
```

Even though only Admin should access `privateNotes`, raw SQL can modify it and thereby bypass the restriction:

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")

// DANGER: Raw SQL bypasses .only[Admin] restriction!
conn.rawTransact(
  """
    |UPDATE Account
    |SET privateNotes = 'HACKED! Raw SQL bypasses authorization'
    |WHERE username = 'alice123'
  """.stripMargin
)(using adminConn)
```

Molecule's normal API would enforce the restriction, but raw SQL does not.

### Can Escalate Privileges

```scala
trait Person {
  val name = oneString
  val accessRole = oneString  // User's role
}
```

Raw SQL can change anyone's role:

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")

// EXTREME DANGER: Escalate Guest to Admin!
conn.rawTransact(
  """
    |UPDATE Person
    |SET accessRole = 'Admin'
    |WHERE name = 'Charlie'
  """.stripMargin
)(using adminConn)
```

Charlie is now an Admin, completely bypassing the authorization model.

### Can Corrupt Data

```scala
// DANGER: Set all customer IDs to same value
conn.rawTransact(
  """
    |UPDATE Sale
    |SET customerId = 999
  """.stripMargin
)(using adminConn)
```

This corrupts relational integrity - all sales now point to the same customer.

## When to use raw SQL

### Legitimate rawQuery Use Cases

1. **Window functions:**
```sql
SELECT amount, SUM(amount) OVER (ORDER BY date) as running_total
FROM Sale
```

2. **Complex aggregations:**
```sql
SELECT region,
       AVG(amount) as avg_sale,
       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY amount) as median
FROM Sale
GROUP BY region
```

3. **Database-specific features:**
```sql
SELECT * FROM Sale
WHERE tsv @@ to_tsquery('postgres & fulltext')  -- PostgreSQL full-text search
```

### Legitimate rawTransact Use Cases

1. **Bulk operations with complex logic:**
```sql
UPDATE Sale
SET amount = CASE
  WHEN ... THEN ...
  ELSE ...
END
```

2. **Database maintenance:**
```sql
VACUUM ANALYZE Sale;
REINDEX TABLE Sale;
```

3. **Schema migrations (admin only):**
```sql
ALTER TABLE Sale ADD COLUMN notes TEXT;
```

## Best practices

1. **Grant sparingly:**
   - `rawQuery` only to analyst/reporting roles
   - `rawTransact` only to trusted admin/DBA roles

2. **Prefer Molecule API:**
   - Use raw SQL only when Molecule doesn't support the operation
   - Most CRUD operations should use Molecule's API

3. **Audit raw SQL usage:**
   - Log all raw SQL queries/mutations
   - Track which users have raw access
   - Monitor for privilege escalation attempts

4. **Validate inputs:**
   - Never concatenate user input directly into raw SQL
   - Use parameterized queries when possible

5. **Test thoroughly:**
   - Raw SQL bypasses compile-time validation
   - Requires runtime testing for correctness

## Role access matrix

Example permissions matrix:

| Role | query | save | insert | update | delete | rawQuery | rawTransact |
|:-----|:-----:|:----:|:------:|:------:|:------:|:--------:|:-----------:|
| Guest | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Member | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Analyst | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Admin | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

## Summary

Raw SQL actions:
- Operate outside the 4-layer authorization model
- Check ONLY role-level permissions
- Bypass all entity/attribute restrictions
- `rawQuery` is safer (read-only) but can leak sensitive data
- `rawTransact` is dangerous and should be heavily restricted
- Use only when Molecule's API is insufficient
- Grant sparingly and audit usage

## Comparison with other systems

Most ORMs and query libraries provide raw SQL fallbacks, but differ in type safety and authorization:

| Library | Raw SQL API | Return Type | Authorization |
|:--------|:------------|:------------|:--------------|
| **Molecule** | `rawQuery` / `rawTransact` | `List[List[Any]]` (untyped) | ✅ Role-level checks |
| **Doobie** | `Fragment` API | Untyped, needs casting | ❌ Manual implementation |
| **Slick** | `sql"..."` interpolation | Typed with `GetResult` | ❌ Manual implementation |
| **Quill** | `infix"..."` | Macro-based, some type safety | ❌ Manual implementation |
| **jOOQ** | Plain SQL execution | Typed with result mapping | ❌ Database roles or app logic |
| **Hibernate/JPA** | `createNativeQuery()` | `Object[]` or mapped | ❌ Manual filtering |

**Key difference:** Molecule is unique in checking role-level permissions even for raw SQL, providing a baseline of security when bypassing the type-safe API. Other libraries require you to implement authorization manually around raw queries.
