package docs.compare.setup

import scala.util.Using.Manager
import cats.effect.{IO, IOApp}
import doobie._
import doobie.implicits._
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

object DoobieSetup extends IOApp.Simple {

  // Build a Transactor backed by a Testcontainers Postgres and inline DDLs
  def transactor(): Transactor[IO] =
    val ds = PGSimpleDataSource()
    val pg = new PostgreSQLContainer("postgres:17")
    pg.start()
    ds.setUrl(pg.getJdbcUrl)
    ds.setUser(pg.getUsername)
    ds.setPassword(pg.getPassword)

    // Inline DDL
    val tableDDLs = Vector(
      """
      CREATE TABLE IF NOT EXISTS project (
        id     SERIAL PRIMARY KEY,
        name   TEXT NOT NULL,
        budget INT  NOT NULL
      );
      """,
      """
      CREATE TABLE IF NOT EXISTS employee (
        id         SERIAL PRIMARY KEY,
        name       TEXT NOT NULL,
        salary     INT  NOT NULL,
        project_id INT  NOT NULL REFERENCES project(id) ON DELETE CASCADE
      );
      """
    )

    // Create schema once up-front
    Manager(use =>
      val con  = use(ds.getConnection)
      val stmt = use(con.createStatement)
      for ddl <- tableDDLs do stmt.execute(ddl)
    ).get

    // Doobie transactor using same JDBC settings
    val props = new java.util.Properties()
    props.setProperty("user", pg.getUsername)
    props.setProperty("password", pg.getPassword)
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = pg.getJdbcUrl,
      info = props,
      logHandler = None
    )
  end transactor

  // Seed data
  private val seed: ConnectionIO[Int] =
    for {
      _ <- sql"TRUNCATE employee, project RESTART IDENTITY".update.run
      p1 <- sql"INSERT INTO project(name, budget) VALUES ('Site Redesign', 1500000)".update.withUniqueGeneratedKeys[Int]("id")
      _ <- sql"INSERT INTO project(name, budget) VALUES ('Internal Tooling', 300000)".update.run
      _ <- sql"INSERT INTO employee(name, salary, project_id) VALUES ('Alice', 120000, $p1), ('Bob', 110000, $p1)".update.run
    } yield 1

  // Example query: employees working on projects with budget > 1M
  private val q: Query0[(String, Int, String)] = sql"""
    SELECT e.name, e.salary, p.name
    FROM employee e
    JOIN project p ON e.project_id = p.id
    WHERE p.budget > 1000000
    ORDER BY e.name
  """.query[(String, Int, String)]

  def run: IO[Unit] = for {
    tx <- IO(transactor())
    _ <- seed.transact(tx)
    rows <- q.to[List].transact(tx)
    _ <- IO {
      val expected = List(
        ("Alice", 120000, "Site Redesign"),
        ("Bob", 110000, "Site Redesign")
      )
      assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
    }
    _ <- IO.println(rows)
  } yield ()
}
