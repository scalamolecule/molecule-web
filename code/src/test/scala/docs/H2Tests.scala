package docs

import java.sql.DriverManager
import molecule.db.common.api.MetaDb_h2
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.common.spi.Conn
import utest.*
import scala.util.Random
import scala.util.Using.Manager

trait H2Tests extends TestSuite {

  def h2[T](metaDb: MetaDb_h2)(test: Conn ?=> T): T = {
    val url   = "jdbc:h2:mem:test" + Random.nextInt().abs
    val proxy = JdbcProxy(url, metaDb)
    Class.forName("org.h2.Driver")
    val sqlConn = DriverManager.getConnection(proxy.url)
    given JdbcConn_JVM = JdbcHandler_JVM.recreateDb(proxy, sqlConn)
    test
  }
}
