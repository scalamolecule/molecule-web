# Authorization overview

Molecule provides declarative, compile-time validated authorization integrated directly into your domain model. Define authorization rules once in the domain structure definition and they're automatically enforced at runtime.

## The 4 authorization layers

Authorization in Molecule is built on 4 distinct layers that work together:

1. **Roles** - Which roles have access to entities
2. **Action Grants** - Action grants at entity level
3. **Attribute Restrictions** - Attribute role restrictions
4. **Attribute Updates** - Attribute update grants

These layers build on each other, providing progressively finer control.

**Note:** [Raw SQL actions](/database/authorization/raw-access.html) (rawQuery, rawTransact) operate outside these 4 layers and only check role-level permissions.

## Core principles

1. **Public entities (no roles) are unrestricted** - Entities with no roles can be accessed by anyone with all 5 actions (query, save, insert, update, delete) without authentication
2. **Roles define baseline capabilities** via action traits (Layer 1)
3. **Action grants ADD capabilities** to specific roles (Layer 2)
4. **Attribute restrictions NARROW access** from entity baseline (Layer 3)
5. **Attribute update grants ADD capabilities** to specific roles for specific fields (Layer 4)
6. **All 5 actions must be available** - For role-restricted entities, combined roles and grants must provide access to all 5 actions
7. **User authenticates as ONE role** at a time (for role-restricted entities)
8. **No magic roles** - all roles follow same rules
9. **One entity = one database table** (no entity variations)

## Action traits

### Core Actions (CRUD)

Scala marker traits are used to define actions that a role can perform on an entity:
```scala
trait query extends Action      // Read data via queries
trait save extends Action       // Create single entity
trait insert extends Action     // Batch insert multiple entities
trait update extends Action     // Modify existing data
trait delete extends Action     // Remove data
```

### Raw SQL Actions (Advanced)

```scala
trait rawQuery extends Action    // Raw SQL SELECT queries (read-only)
trait rawTransact extends Action // Raw SQL mutations (dangerous!)
```

The raw SQL actions provide fallback methods for advanced use cases not covered by Molecule's query API. They operate **outside** the 4-layer authorization model and should be granted sparingly.

## Role definition

Roles are defined by extending `Role` and its allowed actions:

```scala
trait RoleName extends Role with action1 with action2 ...
```

**Example:**

Define some roles in your domain structure definition and let entities extend their allowed roles:
```scala
import molecule.DomainStructure

trait MyDomain extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with query with save
  trait Moderator extends Role with query with save with insert with update with delete
  trait Admin extends Role with query with save with insert with update with delete

  trait Post extends Member with Admin {
    val content = oneString
  }
  // More entity definitions ...
}
```

**Limit:** Up to 32 different roles per domain.

## Entity-level authorization

### Public entities

Entities that don't extend any roles are public:

```scala
trait Article {
  val title = oneString
}
```

- Anyone can access without authentication
- All 5 actions available (query, save, insert, update, delete)

### Layer 1: Role-restricted entities

```scala
trait Post extends Member with Admin { // ADD roles to entity
  val content = oneString
}
```

- Only Member and Admin can access
- Each role uses their own action access

### Layer 2: With action grants

```scala
trait Post extends Member with Admin 
  with updating[Member] { // ADD update capability to Member
  val content = oneString
}
```

- Member gets update capability added
- Admin unaffected (already has update)

## Attribute-level authorization

### Layer 3: Restrictions

Allow an attribute to be accessed by specific roles:
```scala
val attr = oneType                 // All entity roles (baseline)
val attr = oneType.only[Role]      // ONLY these roles (replacement)
val attr = oneType.exclude[Role]   // All EXCEPT these roles (subtraction)
```

### Layer 4: Update grants

Allow an attribute to be updated by specific roles (when they lack update capability):
```scala
val attr = oneType.updating[Role]  // ADD update capability to this role
```

## Compile-time validation

When boilerplate code is generated from your domain structure definition (with `sbt moleculeGen`), Molecule validates the authorization rules at compile time:

1. All 5 actions must be available for role-restricted entities
2. At least one role can query
3. At least one role can save
4. Action grants reference roles in entity baseline
5. Action grants compatible with attribute restrictions
6. Attribute `.only/.exclude` reference roles in entity baseline
7. Attribute `.updating` references roles in entity baseline

## Runtime usage

### Authentication

User authentication (validating credentials, managing sessions) is handled by your application. Molecule only requires you to provide the authenticated user's ID and role:

```scala
// After your app authenticates the user (password, OAuth, etc.)
// provide userId and role to Molecule
implicit val conn = baseConn.withAuth(userId, "Member")
```

Once authenticated, the connection enforces all authorization rules automatically.

### Automatic Authorization

```scala
// All operations check authorization automatically in .transact
Post.title.query.get                      // ✓ or ✗ based on role
Post.title("Hi").save.transact            // ✓ or ✗ based on role
Post(id).title("New").update.transact     // ✓ or ✗ based on role
Post(id).delete.transact                  // ✓ or ✗ based on role
```

### Error Messages

When authorization fails:

```scala
Post(id).title("New").update.transact
// ModelError: Access denied: Role 'Member' cannot update attribute 'Post.title'

Post(id).delete.transact
// ModelError: Access denied: Role 'Member' cannot delete entity 'Post'
```

## Advantages

- **Compile-time safety** - catch errors at build time
- **Zero boilerplate** - no middleware, decorators, or resolver code
- **Centralized** - all authorization in domain definition
- **Flexible** - additive grants allow fine-grained control
- **Natural syntax** - reads like English
- **Type-safe** - full Scala type system backing
- **Fast runtime checks** - uses highly optimized bit masking for minimal overhead (no string comparisons)
