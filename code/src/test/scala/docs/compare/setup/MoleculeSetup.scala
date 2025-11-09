package docs.compare.setup

import cats.effect.{IO, IOApp, Resource}
import db.compare.setup.dsl.SetupExample.*
import db.compare.setup.dsl.SetupExample.metadb.SetupExample_postgresql
import molecule.DomainStructure
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.common.spi.Conn
import molecule.db.postgresql.io.*
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer


object MoleculeSetup extends IOApp.Simple {

  // Resource that manages a Postgres Testcontainer and a Molecule connection
  val dataSource = new PGSimpleDataSource()
  val connR: Resource[IO, JdbcConn_JVM] = {
    val containerR: Resource[IO, PostgreSQLContainer[?]] =
      Resource.make {
        IO {
          val c = new PostgreSQLContainer("postgres:17")
          c.start()
          c
        }
      }(c => IO(c.stop()).void)

    containerR.flatMap { container =>
      dataSource.setURL(container.getJdbcUrl)
      dataSource.setDatabaseName(container.getDatabaseName)
      dataSource.setUser(container.getUsername)
      dataSource.setPassword(container.getPassword)
      dataSource.setPreparedStatementCacheQueries(0)
      Resource.make(IO{
        val sqlConn = dataSource.getConnection
        val proxy   = JdbcProxy("postgres:17", SetupExample_postgresql())
        JdbcHandler_JVM.recreateDb(proxy, sqlConn)
      })(conn => IO(conn.close()))
    }
  }

  import db.compare.setup.dsl.SetupExample.*

  def run: IO[Unit] =
    connR.use { conn =>
      given JdbcConn_JVM = conn
      for {
        // Create schema from domain
//        _ <- recreateSchema().transact

        // Seed projects
        p1 <- Project.name.budget.insert(
          ("Site Redesign", 1500000),
          ("Internal Tooling", 300000)
        ).transact.map(_.ids.head)

        // Seed employees tied to project p1
        _ <- Employee.name.salary.project.insert(
          ("Alice", 120000, p1),
          ("Bob",   110000, p1)
        ).transact

        rows <- Employee.name.a1.salary.Project.name.budget_.>(1000000).query.get

        // Assert and print
        _ <- IO {
               val expected = List(
                 ("Alice", 120000, "Site Redesign"),
                 ("Bob",   110000, "Site Redesign")
               )
               assert(rows == expected, s"Unexpected rows: $rows, expected: $expected")
             }
        _ <- IO.println(rows)
      } yield ()
    }
}
