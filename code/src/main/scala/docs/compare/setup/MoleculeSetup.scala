package docs.compare.setup

import cats.effect.unsafe.implicits.global
import db.compare.setup.dsl.SetupExample.*
import db.compare.setup.dsl.SetupExample.metadb.SetupExample_postgresql
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

object MoleculeSetup extends App {
  // Intentionally not closing DataSource/Connections/Container to keep the example minimal for docs — do not copy to production

  // Minimal Testcontainers + DataSource setup
  val ds = new PGSimpleDataSource()
  val pg = new PostgreSQLContainer("postgres:17")
  pg.start()
  ds.setUrl(pg.getJdbcUrl)
  ds.setUser(pg.getUsername)
  ds.setPassword(pg.getPassword)

  // Create Molecule connection and reset schema for the example
  val sqlConn = ds.getConnection
  val proxy   = JdbcProxy("postgres:17", SetupExample_postgresql())
  given JdbcConn_JVM = JdbcHandler_JVM.recreateDb(proxy, sqlConn)

  // Seed projects
  val p1: Long =
    Project.name.budget.insert(
      ("Site Redesign", 1500000),
      ("Internal Tooling", 300000)
    ).transact.map(_.ids.head).unsafeRunSync()

  // Seed employees tied to project p1
  Employee.name.salary.project.insert(
    ("Alice", 120000, p1),
    ("Bob",   110000, p1)
  ).transact.unsafeRunSync()

  // Query (sorted by name for deterministic assertion)
  val rows =
    Employee.name.a1.salary.Project.name
      .budget_.>(1000000)
      .query.get.unsafeRunSync()

  val expected = List(
    ("Alice", 120000, "Site Redesign"),
    ("Bob",   110000, "Site Redesign")
  )
  assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
  println(rows)
}
