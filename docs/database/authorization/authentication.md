# Authentication

**Authentication** establishes the user's identity and role and is out of scope for Molecule. Your application must authenticate users and provide a role for each user.

Molecule's **authorization** system then uses this authenticated role to determine the user's permissions for role-restricted entities.


## Basic authentication (server-side)

On the server/JVM, use `withAuth` to create an authenticated connection:

```scala
// Get base connection
val baseConn = Conn(myDb)

// Authenticate as specific role
val memberConn = baseConn.withAuth(userId, "Member")

// Use authenticated connection
Post.content("Hello").save.transact(using memberConn)
```

`withAuth` takes two parameters:
1. **User identifier** - String or UUID for auditing/tracking
2. **Role name** - Must match a role defined in the domain

**Note:** `withAuth` is a server-side (JVM) method. On JavaScript/client-side, authorization is handled by the server through API endpoints - the client never directly authenticates database connections.

## Public vs role-restricted access

### Public Entities (No Authentication)

Public entities don't require authentication:

```scala
trait Article {
  val title = oneString
}
```

```scala
// No authentication needed
Article.title("Public Article").save.transact

// But authenticated users follow their role permissions
val guestConn = baseConn.withAuth("user1", "Guest")
Article.title.query.get(using guestConn)  // ✓ Guest can query
Article.title("New").save.transact(using guestConn)  // ✗ Guest lacks save action
```

### Role-Restricted Entities (Authentication Required)

Role-restricted entities require authentication:

```scala
trait Post extends Member with Admin {
  val content = oneString
}
```

```scala
// Unauthenticated access denied
Post.content.query.get  // ✗ Error: No authenticated role provided

// Must authenticate
val memberConn = baseConn.withAuth("user1", "Member")
Post.content.query.get(using memberConn)  // ✓
```

## Authentication examples

How you authenticate users depends on your application architecture. Below are common patterns showing how to extract userId and role, then create an authenticated connection with `withAuth`:

::: code-tabs#auth

@tab Password
```scala
import com.github.t3hnar.bcrypt._

case class User(id: String, username: String, hashedPassword: String, role: String)

def login(username: String, password: String): Option[Conn] = {
  // 1. Fetch user from your user store
  userRepository.findByUsername(username).flatMap { user =>
    // 2. Verify password with bcrypt
    if (password.isBcryptSafeBounded(user.hashedPassword)) {
      // 3. Create authenticated connection
      Some(baseConn.withAuth(user.id, user.role))
    } else {
      None
    }
  }
}

// Usage
login("alice", "secret123") match {
  case Some(authConn) =>
    Post.content.query.get(using authConn)
  case None =>
    // Handle failed login
}
```

@tab OAuth 2.0
```scala
import play.api.libs.oauth._

def authenticateOAuth(provider: String, code: String): Future[Option[Conn]] = {
  // 1. Exchange code for access token
  oauthClient.getAccessToken(code).flatMap { accessToken =>
    // 2. Fetch user info from OAuth provider
    oauthClient.getUserInfo(accessToken).map { userInfo =>
      // 3. Find or create user in your database
      userRepository.findOrCreate(
        providerId = userInfo.id,
        email = userInfo.email,
        name = userInfo.name
      ).map { user =>
        // 4. Create authenticated connection
        baseConn.withAuth(user.id, user.role)
      }
    }
  }
}

// Usage in controller
def callback(code: String) = Action.async {
  authenticateOAuth("google", code).map {
    case Some(authConn) =>
      // Store authConn in session
      Ok("Logged in")
    case None =>
      Unauthorized("OAuth failed")
  }
}
```

@tab JWT
```scala
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import scala.util.{Success, Failure}

def authenticateJWT(token: String): Option[Conn] = {
  // 1. Verify and decode JWT
  Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
    case Success(claims) =>
      val claimData = ujson.read(claims.content)

      // 2. Extract userId and role from claims
      val userId = claimData("sub").str
      val role = claimData("role").str

      // 3. Create authenticated connection
      Some(baseConn.withAuth(userId, role))

    case Failure(_) =>
      None // Invalid token
  }
}

// Usage in middleware
def withAuth[A](action: Conn => Action[A]): Action[A] = Action { request =>
  request.headers.get("Authorization") match {
    case Some(header) if header.startsWith("Bearer ") =>
      val token = header.substring(7)
      authenticateJWT(token) match {
        case Some(authConn) => action(authConn)
        case None => Unauthorized("Invalid token")
      }
    case _ => Unauthorized("No token provided")
  }
}
```

@tab Session
```scala
import play.api.mvc._

case class SessionData(userId: String, role: String)

def authenticateSession(implicit request: Request[_]): Option[Conn] = {
  for {
    // 1. Extract session data
    userId <- request.session.get("userId")
    role <- request.session.get("role")

    // 2. Optionally verify session is still valid
    if sessionStore.isValid(userId, request.session.get("sessionId"))

    // 3. Create authenticated connection
  } yield baseConn.withAuth(userId, role)
}

// Usage in controller
def getPosts = Action { implicit request =>
  authenticateSession match {
    case Some(authConn) =>
      val posts = Post.content.query.get(using authConn)
      Ok(Json.toJson(posts))
    case None =>
      Unauthorized("Not logged in")
  }
}

// Login endpoint creates session
def login = Action { request =>
  val (username, password) = extractCredentials(request)

  validateCredentials(username, password) match {
    case Some(user) =>
      Ok("Logged in").withSession(
        "userId" -> user.id,
        "role" -> user.role,
        "sessionId" -> generateSessionId()
      )
    case None =>
      Unauthorized("Invalid credentials")
  }
}
```

@tab API Key
```scala
case class ApiKey(key: String, userId: String, role: String, permissions: Set[String])

def authenticateApiKey(apiKey: String): Option[Conn] = {
  // 1. Lookup API key in your key store
  apiKeyRepository.findByKey(apiKey).flatMap { keyData =>
    // 2. Verify key is active and not expired
    if (keyData.isActive && !keyData.isExpired) {
      // 3. Create authenticated connection
      Some(baseConn.withAuth(keyData.userId, keyData.role))
    } else {
      None
    }
  }
}

// Usage in API endpoint
def apiEndpoint = Action { request =>
  request.headers.get("X-API-Key") match {
    case Some(apiKey) =>
      authenticateApiKey(apiKey) match {
        case Some(authConn) =>
          val data = Post.content.query.get(using authConn)
          Ok(Json.toJson(data))
        case None =>
          Forbidden("Invalid or expired API key")
      }
    case None =>
      Unauthorized("API key required")
  }
}
```

:::

**Best practice:** Follow this pattern for all authentication methods:
1. Validate credentials/token/session
2. Extract `userId` and `role`
3. Create authenticated connection: `baseConn.withAuth(userId, role)`
4. Store connection (in session, implicit scope, etc.)
5. Write Molecule queries directly where needed in your domain/UI code

## Single role per connection

Each connection authenticates as **one role**:

```scala
// User authenticates as Member
val memberConn = baseConn.withAuth("user1", "Member")

// Operations use Member permissions
Post.content.query.get(using memberConn)  // Member capabilities
```

To use different roles, create separate connections:

```scala
val memberConn = baseConn.withAuth("user1", "Member")
val adminConn = baseConn.withAuth("admin1", "Admin")

// Different capabilities
Post.content.query.get(using memberConn)  // Member permissions
Post(id).delete.transact(using adminConn)  // Admin permissions
```

## Session management

Once a user is authenticated, you'll want to maintain their authenticated connection throughout their session. This section shows patterns for storing and reusing authenticated connections so you don't need to re-authenticate on every request.

### Web Application Pattern

```scala
case class Session(userId: String, role: String, conn: Conn)

def createSession(username: String, password: String): Option[Session] = {
  login(username, password).map { userInfo =>
    val authConn = baseConn.withAuth(userInfo.userId, userInfo.role)
    Session(userInfo.userId, userInfo.role, authConn)
  }
}

// In request handler - use Molecule queries directly
def handleRequest(session: Session) = {
  implicit val conn = session.conn

  // Write Molecule queries directly in your domain/UI code
  val posts = Post.content.query.get
  val recentPosts = Post.content.createdAt.>(yesterday).query.get
  // Compose queries freely as needed
}
```

### Connection Pooling

For connection pooling, authenticate per request:

```scala
def handleRequest(userId: String, role: String) = {
  val conn = pool.getConnection()
  val authConn = conn.withAuth(userId, role)

  try {
    // Write Molecule queries directly as needed
    Post.content.query.get(using authConn)
    Post.content.author(userId).query.get(using authConn)
  } finally {
    pool.returnConnection(conn)
  }
}
```

## Role validation

Molecule validates that the role exists at runtime. Available roles are defined in you domain structure and passed to the generated boilerplate code:

```scala
val conn = baseConn.withAuth("user1", "InvalidRole")
// Error: Role 'InvalidRole' not found in domain

val conn = baseConn.withAuth("user1", "Member")  // ✓ OK
```

## Error messages

### No Authentication

```scala
Post.content.query.get
// Error: Access denied: No authenticated role provided
```

### Wrong Role

```scala
val guestConn = baseConn.withAuth("user1", "Guest")
Post.content.query.get(using guestConn)
// Error: Access denied: Role 'Guest' cannot query entity 'Post'
```

### Insufficient Permissions

```scala
val memberConn = baseConn.withAuth("user1", "Member")
Post(id).delete.transact(using memberConn)
// Error: Access denied: Role 'Member' cannot delete entity 'Post'
```

## Summary

Authentication in Molecule:
- Use `withAuth(userId, role)` to authenticate
- Public entities don't require authentication
- Role-restricted entities require authentication
- One role per connection
- Store authenticated connection in session
- Validate role exists at runtime
- Use implicit connections for cleaner code
