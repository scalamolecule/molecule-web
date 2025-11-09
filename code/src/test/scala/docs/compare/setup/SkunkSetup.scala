package docs.compare.setup

import cats.effect.{IO, IOApp, Resource}
import natchez.Trace.Implicits.noop
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import org.testcontainers.containers.PostgreSQLContainer

object SkunkSetup extends IOApp.Simple {

  // Resource that manages a Postgres Testcontainer and exposes a Skunk Session
  private val sessionR: Resource[IO, Session[IO]] = {
    val containerR: Resource[IO, PostgreSQLContainer[?]] =
      Resource.make {
        IO {
          val c = new PostgreSQLContainer("postgres:17")
          c.start()
          c
        }
      }(c => IO(c.stop()).void)

    containerR.flatMap { c =>
      val host = c.getHost
      val port = c.getMappedPort(5432)
      val user = c.getUsername
      val db   = c.getDatabaseName
      val pass = Some(c.getPassword)
      Session.single(host = host, port = port, user = user, database = db, password = pass)
    }
  }

  // DDL commands
  private val ddlProject: Command[Void] = sql"""
    CREATE TABLE IF NOT EXISTS project (
      id     SERIAL PRIMARY KEY,
      name   TEXT NOT NULL,
      budget INT  NOT NULL
    )
  """.command

  private val ddlEmployee: Command[Void] = sql"""
    CREATE TABLE IF NOT EXISTS employee (
      id         SERIAL PRIMARY KEY,
      name       TEXT NOT NULL,
      salary     INT  NOT NULL,
      project_id INT  NOT NULL REFERENCES project(id) ON DELETE CASCADE
    )
  """.command

  private val truncateAll: Command[Void] =
    sql"TRUNCATE employee, project RESTART IDENTITY".command

  // Inserts
  private val insProjectReturningId: Query[String ~ Int, Int] =
    sql"INSERT INTO project(name, budget) VALUES ($text, $int4) RETURNING id".query(int4)

  private val insProject: Command[String ~ Int] =
    sql"INSERT INTO project(name, budget) VALUES ($text, $int4)".command

  private val insEmployee: Command[(String, Int, Int)] =
    sql"INSERT INTO employee(name, salary, project_id) VALUES ($text, $int4, $int4)".command

  // Final query returning tuples (name, salary, project), sorted for deterministic assertion
  private val selectTuples: Query[Void, (String, Int, String)] =
    sql"""
      SELECT e.name, e.salary, p.name
      FROM employee e
      JOIN project p ON e.project_id = p.id
      WHERE p.budget > 1000000
      ORDER BY e.name
    """.query(text ~ int4 ~ text).map { case n ~ s ~ p => (n, s, p) }

  def run: IO[Unit] =
    sessionR.use { s =>
      for {
        // Create schema
        _   <- s.execute(ddlProject)
        _   <- s.execute(ddlEmployee)
        // Seed
        _   <- s.execute(truncateAll)
        p1  <- s.prepare(insProjectReturningId).flatMap(_.unique("Site Redesign" ~ 1500000))
        _   <- s.prepare(insProject).flatMap(_.execute("Internal Tooling" ~ 300000)).void
        _   <- s.prepare(insEmployee).flatMap(_.execute(("Alice", 120000, p1))).void
        _   <- s.prepare(insEmployee).flatMap(_.execute(("Bob",   110000, p1))).void
        // Query and assert
        rows <- s.execute(selectTuples)
        _    <- IO {
                  val expected = List(
                    ("Alice", 120000, "Site Redesign"),
                    ("Bob",   110000, "Site Redesign")
                  )
                  assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
                }
        _    <- IO.println(rows)
      } yield ()
    }
}
