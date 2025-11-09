package docs.compare.setup

import cats.effect.{IO, IOApp, Resource}
import doobie._
import doobie.implicits._
import org.testcontainers.containers.PostgreSQLContainer

object DoobieSetup extends IOApp.Simple {

  // Doobie Transactor backed by Testcontainers with DDL applied up-front
  private val transactorR: Resource[IO, Transactor[IO]] =
    for
      container <- SharedSetup.containerR
      ds        <- SharedSetup.dataSourceR(container)
      _         <- Resource.eval(SharedSetup.migrate(ds))
      xa        <- Resource.eval(IO.pure(
                     Transactor.fromDriverManager[IO](
                       driver = "org.postgresql.Driver",
                       url    = container.getJdbcUrl,
                       user   = container.getUsername,
                       pass   = container.getPassword
                     )
                   ))
    yield xa

  // Seed data
  private val seed: ConnectionIO[Int] =
    for {
      _  <- sql"TRUNCATE employee, project RESTART IDENTITY".update.run
      p1 <- sql"INSERT INTO project(name, budget) VALUES ('Site Redesign', 1500000)".update.withUniqueGeneratedKeys[Int]("id")
      _  <- sql"INSERT INTO project(name, budget) VALUES ('Internal Tooling', 300000)".update.run
      _  <- sql"INSERT INTO employee(name, salary, project_id) VALUES ('Alice', 120000, $p1), ('Bob', 110000, $p1)".update.run
    } yield 1

  // Example query: employees working on projects with budget > 1M
  private val q: Query0[(String, Int, String)] = sql"""
    SELECT e.name, e.salary, p.name
    FROM employee e
    JOIN project p ON e.project_id = p.id
    WHERE p.budget > 1000000
    ORDER BY e.name
  """.query[(String, Int, String)]

  def run: IO[Unit] =
    transactorR.use { xa =>
      for {
        _    <- seed.transact(xa)
        rows <- q.to[List].transact(xa)
        _    <- IO {
                  val expected = List(
                    ("Alice", 120000, "Site Redesign"),
                    ("Bob", 110000, "Site Redesign")
                  )
                  assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
                }
        _    <- IO.println(rows)
      } yield ()
    }
}
