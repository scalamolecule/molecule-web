# Layer 4: Attribute Updates

Let an attribute be updated by specific roles.


## Attribute-level update grant

Use `.updating[Role]` on an attribute to grant update permission:

```scala
trait Member extends Role with query
trait Admin extends Role with query with save with insert with update with delete

trait Post extends Member with Admin {
  val content = oneString  // Member can query

  val title = oneString.updating[Member]  // Member can also update this attribute
}
```

**Result:**
- Member: query on `content`, query + **update** on `title`
- Admin: all actions on both attributes

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = Post.content("Content").title("Title").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")

// Member can update title (.updating[Member])
Post(id).title("Updated Title").update.transact(using memberConn)
Post.title.query.get(using memberConn)  // List("Updated Title")

// Member cannot update content (no grant)
Post(id).content("Updated Content").update.transact(using memberConn)  // ✗
// Error: Access denied: Role 'Member' cannot update attribute 'Post.content'
```

## Multiple roles

Grant update to multiple roles using tuple syntax:

```scala
trait Article extends Member with Moderator with Admin {
  val preview = oneString

  val tags = oneString.updating[(Member, Moderator)]  // Both can update
}
```

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = Article.preview("Preview").tags("tag1").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")
val moderatorConn = baseConn.withAuth("mod1", "Moderator")

// Both can update tags
Article(id).tags("tag2").update.transact(using memberConn)
Article.tags.query.get(using memberConn)  // List("tag2")

Article(id).tags("tag3").update.transact(using moderatorConn)
Article.tags.query.get(using moderatorConn)  // List("tag3")
```

## Precedence

Attribute-level grants (Layer 4) take precedence over entity-level grants (Layer 2):

```scala
trait Post extends Member with Admin
  with updating[Member] {  // Entity-level grant: Member can update all attributes
  
  // Member can update (entity-level grant applies)
  val content = oneString  

  // Explicit grant (ensures it stays even if entity grant changes)
  val author = oneString.updating[Member]  

  // Attribute-level grant overrides entity grant
  val title = oneString.updating[Admin]  
}
```

**Precedence:**
- Entity-level: Member has update grant for all attributes (Layer 2)
- `content`: Member can update (entity grant applies)
- `author`: Member can update (explicit attribute grant - stable even if entity grant changes)
- `title`: Only Admin can update (Layer 4 overrides Layer 2)

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = Post.content("Content").title("Title").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")

// Member can update content (entity-level grant)
Post(id).content("Updated").update.transact(using memberConn)  // ✓

// Member cannot update title (attribute grant overrides entity grant)
Post(id).title("Updated Title").update.transact(using memberConn)  // ✗
// Error: Access denied: Role 'Member' cannot update attribute 'Post.title'
```

## Combining with restrictions

Attribute update grants (Layer 4) work with attribute restrictions (Layer 3):

```scala
trait UserProfile extends Member with Moderator with Admin {
  val username = oneString

  val email = oneString.only[(Moderator, Admin)]  // Restriction

  val displayName = oneString.updating[Member]  // Update grant

  val verified = oneBoolean.only[(Moderator, Admin)].updating[Moderator]  // Both
}
```

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = UserProfile.username("alice").email("alice@example.com")
  .displayName("Alice").verified(false).save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")

// Member can update displayName (update grant)
UserProfile(id).displayName("Updated").update.transact(using memberConn)

// Member cannot access email (restriction)
UserProfile(id).email("new@example.com").update.transact(using memberConn)  // ✗

val moderatorConn = baseConn.withAuth("mod1", "Moderator")

// Moderator can update verified (restriction + update grant)
UserProfile(id).verified(true).update.transact(using moderatorConn)
```

## Why only .updating[Role]?

You may notice there's no `.deleting[Role]` for attributes. In Molecule, deleting an attribute value is done via `update`:

```scala
// apply() sets the attribute value to NULL in the database!
Entity(id).attr().update.transact
```

Since attribute deletion uses the update action, only `.updating[Role]` is needed. The update grant covers both setting values and removing them (setting to null).

## Compile-time validation

When you run `sbt moleculeGen`, attribute update grants are validated at compile-time:

1. **Grants reference entity roles:**
```scala
trait Post extends Member with Admin {
  val title = oneString.updating[Guest]  // ✗ Error - Guest not in entity roles
}
```

2. **Redundant grants allowed but unnecessary:**
```scala
trait Admin extends Role with query with save with insert with update with delete

trait Post extends Member with Admin {
  val title = oneString.updating[Admin]  // OK but redundant - Admin already has update
}
```

Granting actions to roles that already have them is allowed (for explicit guarantees) but unnecessary.

## Summary

Layer 4 provides fine-grained update control:
- `.updating[Role]` grants update permission per attribute
- Independent of entity-level grants
- Can grant to read-only roles for special cases
- Works with attribute restrictions
- Use tuple syntax for multiple roles
- Compile-time validation ensures consistency

## Comparison with other systems

**Spring Security:** Field-level update security requires custom annotations and manual checks. Molecule integrates attribute update grants into the domain model.

**GraphQL:** No standard field-level update permissions - typically handled in resolvers. Molecule provides this declaratively with compile-time validation.

**Casbin:** Field update permissions require separate policy entries. Molecule declares per-attribute update grants directly in the domain.
