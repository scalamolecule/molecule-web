# Layer 1: Roles

Let an entity be accessible by one or more roles.

## Role definitions

Define roles by extending `Role` with action marker traits:

```scala
trait Guest extends Role with query
trait Member extends Role with query with save
trait Admin extends Role with query with save with insert with update with delete
```

Available actions:
- `query` - read data via queries
- `save` - create single entity
- `insert` - batch insert multiple entities
- `update` - modify existing data
- `delete` - remove data

## Public entities

Entities that don't extend any roles are public:

```scala
trait Article {
  val title = oneString
  val preview = oneString
}
```

**Access:**
- Unauthenticated users: all 5 actions available
- Authenticated users: must follow their role's action permissions

**Example:**

```scala
// Unauthenticated access
Article.title("Article").save.transact

// Guest with query-only role cannot save public entity
val guestConn = baseConn.withAuth("user1", "Guest")
Article.title("New").save.transact(using guestConn)  // Access denied
```

This follows industry standards where authentication adds identity/accountability, not extra permissions. For example, AWS S3 public buckets remain accessible to authenticated IAM users, and PostgreSQL public tables remain accessible to authenticated roles.

## All core actions must be available

All 5 core actions must be available across the combined roles:

```scala
trait Member extends Role with query
trait Admin extends Role with query with save with insert with update with delete

trait UserProfile extends Member with Admin {
  val displayName = oneString
  val bio = oneString
}
```

Member alone would only provide query - no way to enter data! Admin provides all 5 actions (query, save, insert, update, delete), satisfying the requirement.

## Role action permissions

Each role's actions determine what they can do:

```scala
trait Guest extends Role with query
// Guest can only query entities they have access to

trait Member extends Role with query with save
// Member can query and save entities they have access to

trait Admin extends Role with query with save with insert with update with delete
// Admin can perform all operations on entities they have access to
```

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")

// Admin can save (has save action)
val id = UserProfile.displayName("Admin").bio("Bio").save.transact(using adminConn).id

// Admin can query (has query action)
UserProfile.displayName.query.get(using adminConn)  // List("Admin")

// Admin can update (has update action)
UserProfile(id).displayName("Updated").update.transact(using adminConn)

// Admin can delete (has delete action)
UserProfile(id).delete.transact(using adminConn)
```

## Compile-time validation

When you run `sbt moleculeGen` to generate boilerplate code from your domain definition, Molecule validates that role-restricted entities have all 5 actions available:

```scala
trait Member extends Role with query  // Only query

trait Post extends Member {  // ✗ Compile error
  val content = oneString
}
// Error: Entity 'Post' doesn't have all 5 actions available
// Missing: save, insert, update, delete
```

**Fix:** Add a role with missing actions:

```scala
trait Admin extends Role with query with save with insert with update with delete

trait Post extends Member with Admin {  // ✓ OK
  val content = oneString
}
```

Or make the entity public:

```scala
trait Post {  // ✓ OK - public entities have all actions
  val content = oneString
}
```

## Summary

Layer 1 establishes:
- Which roles can access which entities
- What actions each role can perform
- Public vs. role-restricted access

This forms the baseline. The next layers add fine-grained control through action grants and attribute restrictions.

## Comparison with other systems

**Spring Security:** Roles defined via annotations on methods. Molecule defines roles once in the domain with compile-time validation.

**GraphQL:** Type-level directives like `@auth(requires: ROLE)`. Similar approach but runtime validation vs. Molecule's compile-time checks.

**Casbin:** Roles defined in separate policy files. Molecule integrates roles directly into the domain model with compile-time validation.
