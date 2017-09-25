package tcs.db.mysql

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource
import scalikejdbc._
import tcs.db.DatabaseSettings

import scala.collection.mutable.ListBuffer

/**
  * Created by Livio on 11/09/2017.
  */
class Table(
             val createQuery: SQL[Nothing, NoExtractor],
             val insertQuery: SQL[Nothing, NoExtractor],
             val dbSettings: DatabaseSettings,
             val bulkInsertLimit: Int = 50000
           ) {

  var buffer = ListBuffer[Seq[Any]]()

  // Initialize JDBC driver & connection pool
  Class.forName("com.mysql.cj.jdbc.Driver")

  val dataSource: DataSource = {
    val ds = new HikariDataSource()
    ds.setJdbcUrl("jdbc:mysql://localhost:3306/" + dbSettings.database + "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&rewriteBatchedStatements=true")
    ds.addDataSourceProperty("autoCommit", "false")
    ds.setMaximumPoolSize(10)
    ds.addDataSourceProperty("user", dbSettings.user)
    ds.addDataSourceProperty("password", dbSettings.psw)
    ds
  }

  ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))


  // Ad-hoc session provider on the REPL
  implicit val session = AutoSession

  // Execute queries for table creation
  using(ConnectionPool.borrow()) { db =>
    createQuery.execute.apply()
  }


  def insert(value: Seq[Any]): Unit = {
    buffer += value

    if ((buffer.size >= bulkInsertLimit)) writeValues
  }


  def flush = writeValues


  private def writeValues = {
    val batchTxParams: Seq[Seq[Any]] = buffer.toList
    using(ConnectionPool.borrow()) { db =>
      insertQuery.batch(batchTxParams: _*).apply()
    }
    buffer.clear()
  }
}
