# Comparison

Molecule provides compile-time validated, field-level authorization with zero boilerplate.

## The fundamental difference

Traditional authorization systems tie permissions to **access points** - REST endpoints, service methods, or GraphQL resolvers. This creates a rigid architecture where each query composition needs its own authorized endpoint:

```java
// Spring: separate methods for each query composition
@PreAuthorize("hasRole('MEMBER')")
public Post getPost(Long id) { ... }

@PreAuthorize("hasRole('MEMBER')")
public PostWithComments getPostWithComments(Long id) { ... }

@PreAuthorize("hasRole('MEMBER')")
public List<Post> getUserPostsWithAuthors(Long userId) { ... }
```

GraphQL provides composability but sacrifices type safety and still requires authorization logic in resolvers.

**Molecule shifts the authorization boundary from access points to data elements.** Permissions live on fields and entities in your domain model. This enables:

- **Full composability**: Write any query composition you need, anywhere in your code
- **Generic endpoints**: A few generic action endpoints can handle any authorized query composition
- **Type safety**: Scala compiler validates both queries and authorization at compile-time
- **Architectural freedom**: Build thin APIs, thick domain logic, or anything in between

```scala
// One domain definition enables infinite compositions
trait Post extends Member with Admin {
  val content = oneString.exclude[Guest]
  val secret = oneString.only[Admin]
}

// Compose freely - authorization handled transparently
Post.content.Author.name.query.get
Post.title.Comment.text.query.get
Post.content.Author.name.Comment.text.created.query.get
```

No endpoint proliferation. No authorization logic scattered across resolvers. Just compose the data you need, and Molecule enforces permissions automatically.

## The Scala ecosystem gap

Most Scala web frameworks and libraries provide authentication helpers but leave authorization as a manual implementation:

- **Play Framework**: Provides action composition and session management, but authorization requires manual checks or third-party libraries like Deadbolt (annotations) or Silhouette
- **http4s/ZIO HTTP**: Middleware for authentication, but no built-in authorization - you implement it in your business logic
- **Doobie/Slick/Quill**: Pure database libraries with no authorization mechanisms - authorization must be handled in the application layer
- **Frontend (Laminar/Tyrian)**: UI libraries with no authorization - all authorization happens on the backend

The typical approach in Scala web development:
1. Authenticate users at the controller/middleware level
2. Manually implement authorization checks scattered throughout the codebase
3. No compile-time validation of permissions
4. Authorization separate from data model, leading to potential security gaps

**Molecule fills this gap** by integrating authorization directly into the data layer with compile-time guarantees, eliminating the need for manual permission checks throughout your application.

## Quick comparison

Let's see how Molecule's authorization compares to other systems:

| System | Type | Validation | Boilerplate | Learning Curve |
|:-------|:-----|------------|-------------|----------------|
| **Molecule** | Declarative RBAC | Compile-time | ⚪ Zero | 🟢 Low |
| **GraphQL Directives** | Declarative | Runtime | 🟢 Low | 🟢 Low |
| **Spring Security** | Annotation-based | Runtime | 🟡 Medium | 🟡 Medium |
| **Casbin** | Policy language | Runtime | 🟡 Medium | 🔴 High |

## Feature comparison

| Feature | Molecule | GraphQL | Spring | Casbin |
|---------|----------|---------|--------|--------|
| **Compile-time validation** | ✅ | ❌ | ❌ | ❌ |
| **Zero boilerplate** | ✅ | ❌ | ❌ | ❌ |
| **Field-level permissions** | ✅ | ✅ | ❌ | ✅ |
| **Centralized definition** | ✅ | ✅ | ❌ | ✅ |
| **Type safety** | Full | Schema | Partial | None |
| **Automatic enforcement** | ✅ | ✅ | ❌ | ❌ |
| **Refactoring support** | Compiler | Schema | IDE | None |

## GraphQL with Directives

**Approach:** Schema directives for field-level permissions

```graphql
type Post @auth(requires: [MEMBER, ADMIN]) {
  title: String!
  content: String! @auth(requires: [MEMBER, ADMIN])
  secret: String! @auth(requires: [ADMIN])
}
```

**Molecule equivalent:**

```scala
trait Post extends Guest with Member with Admin {
  val title = oneString
  val content = oneString.exclude[Guest]
  val secret = oneString.only[Admin]
}
```

**Verdict:** Molecule provides compile-time safety with zero boilerplate, while GraphQL requires runtime checks and custom resolver logic.

## Spring Security (Java/Kotlin)

**Approach:** Annotation-based method security

```java
@Service
public class PostService {
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    public Post getPost(Long id) { ... }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateSecret(Long id, String secret) { ... }
}
```

**Molecule equivalent:**

```scala
trait Post extends Guest with Member with Admin
  with updating[Member] {
  val title = oneString
  val content = oneString.exclude[Guest]
  val secret = oneString.only[Admin]
}

// Usage - authorization automatic
Post.title.query.get  // Checks automatically
```

**Verdict:** Spring Security requires annotations on every method and runtime checks. Molecule centralizes authorization in the domain with compile-time validation.

## Casbin

**Approach:** Policy language with separate policy files

```ini
# policy.csv
p, member, post, read
p, member, post, write
p, admin, post, read
p, admin, post, write
p, admin, post_secret, read
p, admin, post_secret, write
```

```java
// In application code - manual checks
Enforcer enforcer = new Enforcer("model.conf", "policy.csv");

if (enforcer.enforce(user, "post", "read")) {
    // Allow reading post
}

if (enforcer.enforce(user, "post_secret", "read")) {
    // Allow reading secret field
}
```

**Molecule equivalent:**

```scala
trait Post extends Guest with Member with Admin
  with updating[Member] {
  val title = oneString
  val content = oneString.exclude[Guest]
  val secret = oneString.only[Admin]
}

// Usage - authorization transparent
Post.title.query.get  // Checks automatically
```

**Verdict:** Casbin is flexible and powerful but requires separate policy files and manual enforcement checks. Molecule centralizes authorization in the domain with automatic enforcement.

## Code example: blog post

**Molecule (15 lines):**

```scala
trait Guest extends Role with query
trait Member extends Role with query with save
trait Moderator extends Role with query with save with insert with update with delete
trait Admin extends Role with query with save with insert with update with delete

trait BlogPost extends Guest with Member with Moderator with Admin
  with updating[Member]
  with deleting[Moderator] {
  val title = oneString
  val content = oneString.exclude[Guest]
  val draft = oneBoolean.updating[Guest]
  val flagged = oneBoolean.only[(Moderator, Admin)]
  val featured = oneBoolean.only[Admin]
}

// Usage - zero boilerplate
BlogPost.title.query.get
BlogPost(id).content("new").update.transact
```

**Spring Security (~80 lines):**

```java
@Entity
public class BlogPost {
    private String title;
    private String content;
    private Boolean draft;
    private Boolean flagged;
    private Boolean featured;
}

@Service
public class BlogPostService {
    @PreAuthorize("isAuthenticated()")
    public BlogPost getPost(Long id) { ... }

    @PreAuthorize("hasAnyRole('MEMBER', 'MODERATOR', 'ADMIN')")
    public String getContent(Long id) { ... }

    @PreAuthorize("hasRole('MEMBER')")
    public void updateContent(Long id, String content) { ... }

    // ... many more methods
}

@RestController
public class BlogPostController {
    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogPostDTO> getPost(@PathVariable Long id) {
        BlogPost post = service.getPost(id);
        BlogPostDTO dto = new BlogPostDTO();
        dto.setTitle(post.getTitle());

        try {
            dto.setContent(service.getContent(id));
        } catch (AccessDeniedException e) {
            dto.setContent(null);
        }

        // Manual field filtering based on role...
        return ResponseEntity.ok(dto);
    }
}
```

## Why Molecule?

### Advantages

1. **Compile-time validation** - authorization rules validated at compile-time, impossible to bypass checks
2. **Zero boilerplate** - no annotations, decorators, or manual checks
3. **Centralized** - all authorization in domain model, not scattered across codebase
4. **Type-safe** - full Scala type system backing
5. **Automatic** - authorization enforced automatically, no way to forget checks
6. **Refactoring** - compiler helps with refactoring, not just grep
7. **DRY** - define authorization once, used everywhere
8. **Natural syntax** - reads like English with Scala's `with` keyword

### When Other Systems Might Be Better

- **GraphQL systems** - if you're already heavily invested in GraphQL
- **Existing codebases** - migration cost may be high for mature applications
- **Polyglot teams** - if team doesn't know Scala
- **Dynamic requirements** - if authorization logic changes frequently at runtime

### Molecule's Sweet Spot

- New Scala projects
- Domain-driven design
- Type-safe applications
- Teams that value compile-time safety
- Applications where authorization is complex but mostly static
- Teams that want to minimize boilerplate

## Summary

Molecule's authorization model is unique in providing:

1. **Compile-time validation** - authorization rules validated at compile-time, enforced at runtime
2. **Zero boilerplate** - no manual checks, annotations, or decorators
3. **Full type safety** - leverages Scala's type system
4. **Declarative syntax** - natural, readable permission definitions
5. **Automatic enforcement** - no way to forget authorization checks

While other systems require manual enforcement and scattered authorization logic, Molecule centralizes everything in the domain model with compile-time validation and automatic runtime enforcement.
