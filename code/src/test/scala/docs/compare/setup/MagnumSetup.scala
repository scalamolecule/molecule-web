package docs.compare.setup

import scala.util.Using.Manager
import com.augustnagro.magnum.*
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

object MagnumSetup extends App {

  // Build a Transactor backed by a Testcontainers Postgres and inline DDLs
  def transactor(): Transactor =
    val ds = PGSimpleDataSource()
    val pg = new PostgreSQLContainer("postgres:17")
    try
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

      Transactor(ds)
    catch
      case t: Throwable =>
        // Ensure the container is stopped if initialization fails
        try pg.stop() catch case _: Throwable => ()
        throw t
  end transactor

  // Use implicit DbCon provided by connect
  transactor().connect:
    // Reset and seed
    sql"TRUNCATE employee, project RESTART IDENTITY".update.run()
    val p1 = sql"INSERT INTO project(name, budget) VALUES ('Site Redesign', 1500000) RETURNING id".query[Int].run().head
    sql"INSERT INTO project(name, budget) VALUES ('Internal Tooling', 300000)".update.run()
    sql"INSERT INTO employee(name, salary, project_id) VALUES ('Alice', 120000, $p1), ('Bob', 110000, $p1)".update.run()

    // Query (sorted by name for deterministic assertion)
    val rows =
      sql"""
            SELECT
              e.name   AS name,
              e.salary AS salary,
              p.name   AS project
            FROM employee e
            JOIN project p ON e.project_id = p.id
            WHERE p.budget > 1000000
            ORDER BY e.name
          """.query[(String, Int, String)].run()

    val expected = List(
      ("Alice", 120000, "Site Redesign"),
      ("Bob", 110000, "Site Redesign")
    )
    assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
    println(rows)
}
