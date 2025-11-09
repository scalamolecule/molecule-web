package docs.compare.setup

import cats.effect.{IO, Resource}
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

object SharedSetup:

  // Common DDL used across examples
  val ddl: String =
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

  // Testcontainers Postgres as a managed Resource
  val containerR: Resource[IO, PostgreSQLContainer[?]] =
    Resource.make {
      IO.blocking {
        val c = new PostgreSQLContainer("postgres:17")
        c.start()
        c
      }
    }(c => IO.blocking(c.stop()).void)

  // Configured PG DataSource derived from a running container
  def dataSourceR(c: PostgreSQLContainer[?]): Resource[IO, PGSimpleDataSource] =
    Resource.eval(IO.blocking {
      val ds = new PGSimpleDataSource()
      ds.setUrl(c.getJdbcUrl)
      ds.setUser(c.getUsername)
      ds.setPassword(c.getPassword)
      ds
    })

  // Apply DDL using plain JDBC on the provided DataSource
  def migrate(ds: PGSimpleDataSource): IO[Unit] =
    IO.blocking {
      val con = ds.getConnection
      try
        val st = con.createStatement
        try st.execute(ddl)
        finally st.close()
      finally con.close()
    }
