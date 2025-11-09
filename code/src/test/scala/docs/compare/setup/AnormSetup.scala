package docs.compare.setup

import cats.effect.{IO, IOApp}
import anorm._
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

import javax.sql.DataSource
import java.sql.Connection
import scala.util.Using.Manager

object AnormSetup extends IOApp.Simple {

  // Build a DataSource backed by a Testcontainers Postgres and inline DDLs
  def dataSource(): DataSource = {
    val ds = new PGSimpleDataSource()
    val pg = new PostgreSQLContainer("postgres:17")
    pg.start()
    ds.setURL(pg.getJdbcUrl)
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
    Manager { use =>
      val con  = use(ds.getConnection)
      val stmt = use(con.createStatement)
      for (ddl <- tableDDLs) stmt.execute(ddl)
    }.get

    ds
  }

  // Row parser for (name, salary, project)
  private val tupleParser: RowParser[(String, Int, String)] =
    (SqlParser.str("name") ~ SqlParser.int("salary") ~ SqlParser.str("project")).map {
      case n ~ s ~ p => (n, s, p)
    }

  def run: IO[Unit] =
    IO {
      val ds = dataSource()

      Manager { use =>
        implicit val c: Connection = use(ds.getConnection)

        // Reset and seed
        SQL"TRUNCATE employee, project RESTART IDENTITY".execute()

        val p1: Int = SQL"""
          INSERT INTO project(name, budget) VALUES ('Site Redesign', 1500000)
        """.executeInsert()
          .map(_.toInt)
          .getOrElse(sys.error("No id returned for inserted project"))

        SQL"""
          INSERT INTO project(name, budget) VALUES ('Internal Tooling', 300000)
        """.execute()

        SQL"""
          INSERT INTO employee(name, salary, project_id) VALUES ('Alice', 120000, $p1),
                                                                ('Bob',   110000, $p1)
        """.execute()

        // Query (sorted by name for deterministic assertion)
        val rows: List[(String, Int, String)] =
          SQL"""
            SELECT
              e.name   AS name,
              e.salary AS salary,
              p.name   AS project
            FROM employee e
            JOIN project p ON e.project_id = p.id
            WHERE p.budget > 1000000
            ORDER BY e.name
          """.as(tupleParser.*)

        val expected = List(
          ("Alice", 120000, "Site Redesign"),
          ("Bob",   110000, "Site Redesign")
        )
        assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
        println(rows)
      }.get
    }
}
