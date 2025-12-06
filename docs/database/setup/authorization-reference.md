# Authorization Reference

You can define authorization rules directly in your domain model. This page shows the basic syntax - see the [Authorization section](/authorization/overview) for complete details.

## Basic Example

```scala
object MyDomain extends DomainStructure {

  // Define roles with action permissions
  trait Guest extends Role with query
  trait Member extends Role with query with save
  trait Admin extends Role with query with save with insert with update with delete

  // Public entity (no roles)
  trait Article {
    val title = oneString
  }

  // Role-restricted entity
  trait Post extends Member with Admin {
    val content = oneString
  }
}
```

## Roles

Roles define action permissions:

```scala
trait RoleName extends Role with action1 with action2 ...
```

Available actions:
- `query` - read data
- `save` - create single entity
- `insert` - batch insert
- `update` - modify data
- `delete` - remove data
- `rawQuery` - raw SQL SELECT (advanced)
- `rawTransact` - raw SQL mutations (dangerous)

## Entity-Level Authorization

```scala
// Public entity
trait Article {
  val title = oneString
}

// Role-restricted entity
trait Post extends Member with Admin {
  val content = oneString
}

// With action grants
trait Comment extends Member with Admin
  with updating[Member]    // Grant update to Member
  with deleting[Admin] {   // Grant delete to Admin
  val text = oneString
}
```

## Attribute-Level Authorization

```scala
trait Document extends Member with Admin {
  val title = oneString  // All entity roles

  val content = oneString.only[Admin]  // Only Admin

  val summary = oneString.exclude[Guest]  // All except Guest

  val tags = oneString.updating[Member]  // Member can update
}
```

## Runtime Usage

```scala
// Authenticate with role
val conn = baseConn.withAuth(userId, "Member")

// All operations check authorization automatically
Post.content.query.get(using conn)
Post.content("Hello").save.transact(using conn)
Post(id).content("Updated").update.transact(using conn)
```

## Learn More

This covers the basics of defining authorization in your domain model. For complete details on:

- The 4 authorization layers
- Action grants and restrictions
- Compile-time validation
- Raw SQL access
- Authentication patterns
- Comparison with other systems

See the [Authorization section](/authorization/overview).
