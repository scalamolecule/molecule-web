# Layer 2: Action Grants

Let a role also have update/delete capabilities on an entity and its attributes.


## Entity-level update grant

Use `with updating[Role]` to grant update capability:

```scala
trait Member extends Role with query
trait Admin extends Role with query with save with insert with update with delete

trait Post extends Member with Admin
  with updating[Member] { // Update grant - Member can update
  val content = oneString
  val title   = oneString
}

trait Comment extends Member with Admin { // No update grant - Member cannot update
  val text = oneString
}
```

**Result:**
- Post: Member has query + **update** (from grant)
- Comment: Member has query only (no grant)
- Grants are per-entity, not global

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val postId    = Post.content("Original").title("Title").save.transact(using adminConn).id
val commentId = Comment.text("Comment").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")

// Member can update Post (has update grant)
Post(postId).content("Updated").update.transact(using memberConn) // ✓

// Member cannot update Comment (no grant)
Comment(commentId).text("Updated").update.transact(using memberConn) // ✗
// Error: Access denied: Role 'Member' cannot update entity 'Comment'
```

## Entity-level delete grant

Use `with deleting[Role]` to grant delete capability:

```scala
trait Moderator extends Role with query
trait Admin extends Role with query with save with insert with update with delete

trait Comment extends Moderator with Admin
  with deleting[Moderator] {// Update grant - Moderator can delete
  val text = oneString
}

trait Reply extends Moderator with Admin {  // No delete grant - Moderator cannot delete
  val text = oneString
}
```

**Result:**
- Comment: Moderator has query + **delete** (from grant)
- Reply: Moderator has query only (no grant)
- Grants are per-entity

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val commentId = Comment.text("Comment").save.transact(using adminConn).id
val replyId = Reply.text("Reply").save.transact(using adminConn).id

val moderatorConn = baseConn.withAuth("mod1", "Moderator")

// Moderator can delete Comment (has delete grant)
Comment(commentId).delete.transact(using moderatorConn)  // ✓

// Moderator cannot delete Reply (no grant)
Reply(replyId).delete.transact(using moderatorConn)  // ✗
// Error: Access denied: Role 'Moderator' cannot delete entity 'Reply'
```

Delete grants apply to entity and all attributes. This ensures `Entity(id).delete` works correctly, as it affects all attribute values.

## Action grants to multiple roles

Grant to multiple roles using tuple syntax:

```scala
trait Article extends Member with Moderator with Admin
  with updating[(Member, Moderator)] { // two or more roles...
  val title = oneString
  val content = oneString
}
```

**Result:**
- Both Member and Moderator can update
- Admin can update (already has action)

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id1 = Article.title("Article 1").content("Content 1").save.transact(using adminConn).id
val id2 = Article.title("Article 2").content("Content 2").save.transact(using adminConn).id

val memberConn = baseConn.withAuth("member1", "Member")
val moderatorConn = baseConn.withAuth("mod1", "Moderator")

// Both can update
Article(id1).title("Updated by Member").update.transact(using memberConn)
Article(id2).title("Updated by Moderator").update.transact(using moderatorConn)
```

## Combining grants

Combine updating and deleting grants:

```scala
trait ModLog extends Moderator with Admin
  with updating[Moderator]
  with deleting[(Moderator, Admin)] {
  val action = oneString
  val timestamp = oneLong
}
```

**Result:**
- Moderator: query + **update** + **delete** (from grants)
- Admin: all actions + **delete** (redundant, already has delete)

**Example:**

```scala
val adminConn = baseConn.withAuth("admin1", "Admin")
val id = ModLog.action("Action").timestamp(123L).save.transact(using adminConn).id

val moderatorConn = baseConn.withAuth("mod1", "Moderator")

// Moderator can update (updating grant)
ModLog(id).action("Updated").update.transact(using moderatorConn)

// Moderator can delete (deleting grant)
ModLog(id).delete.transact(using moderatorConn)
```

## Explicit action grants

You can explicitly grant actions to roles that already have them. This guarantees the entity will always support that action for that role, even if the role definition changes:

```scala
trait Admin extends Role with query with save with insert with update with delete

trait UserProfile extends Member with Admin
  with updating[Member]
  with deleting[Admin] {  // Explicit: ensure Admin can always delete
  val displayName = oneString
}
```

The `deleting[Admin]` grant is explicit - Admin already has delete, but this ensures UserProfile will always support Admin delete even if the Admin role definition changes later.

## Compile-time validation

When we let sbt-molecule generate boilerplate code with `sbt moleculeGen`, the action grants are checked at compile-time.

1. **Grants must reference entity roles:**
```scala
trait Post extends Member with Admin
  with updating[Guest] {  // ✗ Error - Guest not in entity roles
  val content = oneString
}
```

2. **Grants compatible with attribute restrictions:**

If (layer 4) restrictions are applied to an attribute they take precedence over entity-level action grants.
```scala
trait Document extends Member with Admin
  with deleting[Member] {  // ✗ Error - Member can't access secretNotes
  val title = oneString
  val secretNotes = oneString.only[Admin]
}
// Error: Entity grants deleting to 'Member' but attribute 'secretNotes'
// is restricted with .only[Admin]
```

Fix: Only grant to roles that can access all attributes:
```scala
trait Document extends Member with Admin
  with deleting[Admin] {  // ✓ OK
  val title = oneString
  val secretNotes = oneString.only[Admin]
}
```

## Summary

Layer 2 adds flexibility:
- Grant update/delete to roles that lack these actions
- Grants apply to entity and all attributes
- Use tuple syntax for multiple roles
- Grants are additive and per-entity
- Compile-time validation ensures consistency

## Comparison with other systems

**Spring Security:** Method-level `@PreAuthorize` annotations. Molecule centralizes grants in the domain rather than scattering across service methods.

**GraphQL:** Action control typically implemented in resolvers. Molecule declares actions directly in the domain with compile-time validation.

**Casbin:** Action permissions in policy files like `p, role, resource, action`. Molecule integrates action grants into the domain model with type safety.
