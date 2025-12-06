# Layer 3: Attribute Restrictions

Let an attribute be accessible by specific roles.


## .only[Role]

Restrict an attribute to specific roles:

```scala
trait Post extends Guest with Member with Admin {
  val title = oneString  // All entity roles (Guest, Member, Admin)

  val content = oneString.only[(Member, Admin)]  // Only Member and Admin

  val secret = oneString.only[Admin]  // Only Admin
}
```

**Result:**
- `title` - all entity roles can access
- `content` - only Member and Admin can access
- `secret` - only Admin can access

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
Post.title("Title").content("Content").secret("Secret").save.transact(using adminConn)

val memberConn = baseConn.withAuth("member1", "Member")

// Member can access title (all entity roles)
Post.title.query.get(using memberConn)  // List("Title")

// Member can access content (.only[(Member, Admin)])
Post.content.query.get(using memberConn)  // List("Content")

// Member cannot access secret (.only[Admin])
Post.secret.query.get(using memberConn)  // ✗ Access denied
// Error: Access denied: Role 'Member' cannot query attribute 'Post.secret'

val guestConn = baseConn.withAuth("guest1", "Guest")

// Guest can access title
Post.title.query.get(using guestConn)  // List("Title")

// Guest cannot access content (.only[(Member, Admin)])
Post.content.query.get(using guestConn)  // ✗ Access denied
```

## .exclude[Role]

Exclude specific roles from accessing an attribute:

```scala
trait Article extends Guest with Member with Moderator with Admin {
  val title = oneString  // All entity roles

  val fullText = oneString.exclude[Guest]  // All except Guest

  val editHistory = oneString.exclude[(Guest, Member, Moderator)]  // Only Admin
}
```

**Result:**
- `title` - all roles can access
- `fullText` - Member, Moderator, Admin can access (Guest excluded)
- `editHistory` - only Admin can access (Guest, Member, Moderator excluded)

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
Article.title("Title").fullText("Full text").editHistory("History")
  .save.transact(using adminConn)

val memberConn = baseConn.withAuth("member1", "Member")

// Member can access fullText (.exclude[Guest])
Article.fullText.query.get(using memberConn)  // List("Full text")

// Member cannot access editHistory (.exclude[(Guest, Member, Moderator)])
Article.editHistory.query.get(using memberConn)  // ✗ Access denied

val guestConn = baseConn.withAuth("guest1", "Guest")

// Guest cannot access fullText (.exclude[Guest])
Article.fullText.query.get(using guestConn)  // ✗ Access denied
```

## Precedence

Attribute restrictions override entity-level action grants. A role can have an update grant but still be blocked by attribute restrictions:

```scala
trait Post extends Guest with Member with Admin
  with updating[Member] {  // Entity-level update grant
  val title = oneString
  val secret = oneString.only[Admin]  // Attribute restriction
}
```

**Precedence:**
- Member has entity-level update grant (Layer 2)
- But `secret` attribute restriction blocks Member (Layer 3 overrides Layer 2)
- Member can update `title`, but not `secret`

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = Post.title("Title").secret("Secret").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")

// Member can update title (has grant, no restriction)
Post(id).title("Updated").update.transact(using memberConn)  // ✓

// Member cannot update secret (restriction overrides grant)
Post(id).secret("Updated").update.transact(using memberConn)  // ✗
```

## Compile-time validation

When you run `sbt moleculeGen`, attribute restrictions are validated at compile-time:

1. **Restrictions reference entity roles:**
```scala
trait Post extends Member with Admin {
  val secret = oneString.only[Guest]  // ✗ Error - Guest not in entity roles
}
```

2. **Action grants must be compatible with restrictions:**
```scala
trait Document extends Member with Admin
  with updating[Member] {  // ✗ Error - Member can't access secret
  val title = oneString
  val secret = oneString.only[Admin]
}
// Error: Entity grants updating to 'Member' but attribute 'secret'
// is restricted with .only[Admin]
```

**Fix option 1:** Only grant to roles that can access all attributes:
```scala
trait Document extends Member with Admin
  with updating[Admin] {  // ✓ OK - Admin can access all attributes
  val title = oneString
  val secret = oneString.only[Admin]
}
```

**Fix option 2:** Adjust restriction to include the granted role:
```scala
trait Document extends Member with Admin
  with updating[Member] {  // ✓ OK - Member now included in restriction
  val title = oneString
  val secret = oneString.only[(Member, Admin)]
}
```

## Summary

Layer 3 provides fine-grained attribute control:
- `.only[Role]` restricts to specific roles (replacement)
- `.exclude[Role]` excludes specific roles (subtraction)
- Restrictions override entity baseline
- Apply to all operations (query, save, update)
- Checked before action grants
- Compile-time validation ensures consistency

## Comparison with other systems

**GraphQL:** Field-level `@auth` directives. Molecule provides the same capability with compile-time validation and cleaner syntax.

**Spring Security:** Field-level security requires custom annotations and manual checks. Molecule enforces automatically based on domain definition.

**Casbin:** Field-level permissions require explicit policy entries. Molecule declares attribute restrictions directly in the domain with automatic enforcement.
