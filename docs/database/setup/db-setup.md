---
next: /docs/transact/save
---

# Db setup


Once you have defined your [Domain Structure](/database/setup/data-model), run `sbt moleculeGen` and compiled your project, you're ready to use molecules to transact and fetch data from databases.

Each setup requires that you

1. Import preferred database and Scala API,
2. Import generated boilerplate code (DSL) and
3. Offer an implicit connection to the database


## 1. Choose db and API

Choose a combination of your preferred database and Scala API with a single import:

::: code-tabs#coord
@tab Sync
```scala
// Choose one of
import molecule.db.postgres.sync.*
import molecule.db.sqlite.sync.*
import molecule.db.mysql.sync.*
import molecule.db.mariadb.sync.*
import molecule.db.h2.sync.*
```

@tab Async
```scala
// Choose one of
import molecule.db.postgres.async.*
import molecule.db.sqlite.async.*
import molecule.db.mysql.async.*
import molecule.db.mariadb.async.*
import molecule.db.h2.async.*
```

@tab ZIO
```scala
// Choose one of
import molecule.db.postgres.Zio.*
import molecule.db.sqlite.Zio.*
import molecule.db.mysql.Zio.*
import molecule.db.mariadb.Zio.*
import molecule.db.h2.Zio.*
```

@tab IO
```scala
// Choose one of
import molecule.db.postgres.io.*
import molecule.db.sqlite.io.*
import molecule.db.mysql.io.*
import molecule.db.mariadb.io.*
import molecule.db.h2.io.*
```
:::

This brings in the expressions and actions for your molecules.


## 2. Import DSL

If you defined your Data Model in `app.Community` you can import the generated boilerplate code with

```scala
import app.dsl.Community.*
```
This brings in all the Namespaces and Attributes of your domain.


## 3. Connect to db



Here are some simple examples of how to connect to the databases either in-memory or running in a test-container. For connecting to a persisted live database, please consult the documentation for that database.

When Molecule generates boilerplate code, it also create some meta information for each database that Molecule uses to handle this database. An example is `Community_MetaDb_h2` that for instance contains the path to the generated SQL schema file in the resources directory. This allow the JdbcProxy to find the SQL schema creation code to create the database.

::: tabs#coord

@tab h2
Dependencies in `build.sbt`:
```scala
"org.scalamolecule" %% "molecule-db-h2" % "0.24.0",
// already imports
// "com.h2database" % "h2" % "2.3.232"
```
Example H2 in-memory setup:
```scala
import java.sql.DriverManager
import app.dsl.Community.metadb.Community_MetaDb_h2 // generated 
import molecule.db.common.facade.JdbcHandler_JVM
import molecule.db.common.marshalling.JdbcProxy

trait H2Setup {
  def getConnection = {
  }
    val url = "jdbc:h2:mem:"
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, Community_MetaDb_h2())
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
}
```
See more [H2 setup examples](https://github.com/scalamolecule/molecule/blob/main/db/h2/jvm/src/test/scala/molecule/db/h2/setup/DbConnection_h2.scala) from the molecule compliance test setup.



@tab sqlite
Dependencies in `build.sbt`:
```scala
"org.scalamolecule" %% "molecule-db-sqlite" % "0.24.0",
// already imports
// "org.xerial" % "sqlite-jdbc" % "3.49.1.0"
```
Example SQlite in-memory setup:
```scala
import java.sql.DriverManager
import app.dsl.Community.metadb.Community_MetaDb_sqlite // generated 
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.sqlite.facade.JdbcHandlerSQlite_JVM

trait SQliteSetup {
  def getConnection = {
    val proxy   = JdbcProxy("jdbc:sqlite::memory:", Community_MetaDb_sqlite())
    val sqlConn = DriverManager.getConnection(proxy.url)
    JdbcHandlerSQlite_JVM.recreateDb(proxy, sqlConn, true) 
  }
}
```
See more [SQlite setup examples](https://github.com/scalamolecule/molecule/blob/main/db/sqlite/jvm/src/test/scala/molecule/db/sqlite/setup/DbConnection_sqlite.scala) from the molecule compliance test setup.


@tab postgres
Dependencies in `build.sbt`:
```scala
"org.scalamolecule" %% "molecule-db-postgres" % "0.24.0",
// already imports
// "org.testcontainers" % "postgresql" % "1.20.6",
// "org.postgresql" % "postgresql" % "42.7.5",
```
Example PostgreSQL test container setup:
```scala
import app.dsl.Community.metadb.Community_MetaDb_postgres 
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.common.facade.*
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

trait PostgresSetup {
  private val baseUrl = "postgres:17"

  println(s"Starting $baseUrl docker container...")
  val container = new PostgreSQLContainer(baseUrl)
  container.start()
  println("Postgres docker container started")

  private val dataSource = new PGSimpleDataSource()
  dataSource.setURL(container.getJdbcUrl)
  dataSource.setDatabaseName(container.getDatabaseName)
  dataSource.setUser(container.getUsername)
  dataSource.setPassword(container.getPassword)
  dataSource.setPreparedStatementCacheQueries(0)

  // Re-use connection for multiple tests
  private val reusedSqlConn = dataSource.getConnection

  private val resetDb =
    s"""DROP SCHEMA IF EXISTS public CASCADE;
       |CREATE SCHEMA public;
       |""".stripMargin

  def getConnection = {
    val proxy = JdbcProxy(baseUrl, Community_MetaDb_postgres(), resetDb)
    JdbcHandler_JVM.recreateDb(proxy, reusedSqlConn)
  }
}
```

@tab mysql
Dependencies in `build.sbt`:
```scala
"org.scalamolecule" %% "molecule-db-mysql" % "0.24.0",
// already imports
// "org.testcontainers" % "mysql" % "1.20.6",
// "com.mysql" % "mysql-connector-j" % "9.2.0",
```
Example MySQL test container setup:
```scala
import app.dsl.Community.metadb.Community_MetaDb_mysql
import com.mysql.cj.jdbc.MysqlDataSource
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.common.facade.*
import org.testcontainers.containers.MySQLContainer

trait MysqlSetup {
  private val baseUrl = "mysql:9.0.0"

  println(s"Starting $baseUrl docker container...")
  val container = new MySQLContainer(baseUrl)
  container.start()
  println("Mysql docker container started")

  private val dataSource = new MysqlDataSource()
  dataSource.setURL(container.getJdbcUrl)
  dataSource.setDatabaseName(container.getDatabaseName)
  dataSource.setUser(container.getUsername)
  dataSource.setPassword(container.getPassword)
  dataSource.setAllowMultiQueries(true)
  dataSource.setAutoReconnect(true)

  // Re-use connection for multiple tests
  private val reusedSqlConn = dataSource.getConnection

  private val resetDb =
    s"""DROP DATABASE IF EXISTS test;
       |CREATE DATABASE test;
       |USE test;
       |""".stripMargin

  def getConnection = {
    val proxy = JdbcProxy(baseUrl, Community_MetaDb_mysql(), resetDb)

    // Not closing the connection since we re-use it
    JdbcHandler_JVM.recreateDb(proxy, reusedSqlConn)
  }
}
```


@tab mariadb
Dependencies in `build.sbt`:
```scala
"org.scalamolecule" %% "molecule-db-mariadb" % "0.24.0",
// already imports
// "com.dimafeng" %% "testcontainers-scala-mariadb" % "0.43.0",
// "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.1",
```
Example MariaDB test container setup:
```scala
import java.sql.DriverManager
import app.dsl.Community.metadb.Community_MetaDb_mariadb
import com.dimafeng.testcontainers.MariaDBContainer
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.common.facade.*

trait MariaDBSetup {
  private val url = s"jdbc:tc:mariadb:latest:///test" +
    s"?allowMultiQueries=true" +
    s"&autoReconnect=true" +
    s"&user=root" +
    s"&password="

  // Using dimafeng container for MariaDB to be able to config through url
  println(s"Starting mariadb:latest docker container...")
  private val container = MariaDBContainer()
  Class.forName(container.driverClassName)
  println("MariaDB docker container started")

  // Re-use connection for multiple tests
  private val reusedSqlConn = DriverManager.getConnection(url)

  private val resetDb =
    s"""DROP DATABASE IF EXISTS test;
       |CREATE DATABASE test;
       |USE test;
       |""".stripMargin

  def getConnection = {
    val proxy = JdbcProxy(url, Community_MetaDb_mariadb(), resetDb)

    // Not closing the connection since we re-use it
    JdbcHandler_JVM.recreateDb(proxy, reusedSqlConn)
  }
}
```
:::

Molecule just provides the basic means to connect to a database and is not intended to be an administrative db tool or a facade to a Database api. That is outside the scope of Molecule. There are plenty of excellent tools to administrate SQL databases and we suggest that you consult their documentation for smooth setup and administration.

## 4. Implicit connection

Now we are ready to put things together to start interacting with the database:
```scala
// Database and API
import molecule.db.h2.sync.*

// Generated DSL to use
import app.dsl.Community.*

// Implicit connection to the database
implicit val conn = getConnection

// Execute molecules
Person.name("Bob").age(42).save.transact
assert(Person.name.age.query.get == List(("Bob", 42)) 
```
