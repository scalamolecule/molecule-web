package docs.compare.setup

import scala.util.Using.Manager
import com.augustnagro.magnum.*
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

object MagnumSetup2 extends App {
  // Intentionally not closing DataSource/Connections/Container to keep the example minimal for docs — do not copy to production

  val ds = PGSimpleDataSource()
  val pg = new PostgreSQLContainer("postgres:17")
  pg.start()
  ds.setUrl(pg.getJdbcUrl)
  ds.setUser(pg.getUsername)
  ds.setPassword(pg.getPassword)
  ds.getConnection.createStatement.execute(
    """
    CREATE TABLE IF NOT EXISTS project (
      id     SERIAL PRIMARY KEY,
      name   TEXT NOT NULL,
      budget INT  NOT NULL
    );
    CREATE TABLE IF NOT EXISTS employee (
      id         SERIAL PRIMARY KEY,
      name       TEXT NOT NULL,
      salary     INT  NOT NULL,
      project_id INT  NOT NULL REFERENCES project(id) ON DELETE CASCADE
    );
    """
  )

  Transactor(ds).connect:
    sql"TRUNCATE employee, project RESTART IDENTITY".update.run()
    val p1 = sql"INSERT INTO project(name, budget) VALUES ('Site Redesign', 1500000) RETURNING id".query[Int].run().head
    sql"INSERT INTO project(name, budget) VALUES ('Internal Tooling', 300000)".update.run()
    sql"INSERT INTO employee(name, salary, project_id) VALUES ('Alice', 120000, $p1), ('Bob', 110000, $p1)".update.run()

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
