package it.unica.blockchain.db.sql

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource
import scalikejdbc._
import it.unica.blockchain.db.{DatabaseSettings, PostgreSQL}

import scala.collection.mutable.ListBuffer

/**
  * Created by Livio on 11/09/2017.
  */

object Table{
  private var writeTime = 0l

  def getWriteTime = writeTime
}

class Table(
             val createQuery: SQL[Nothing, NoExtractor],
             val insertQuery: SQL[Nothing, NoExtractor],
             val dbSettings: DatabaseSettings,
             val bulkInsertLimit: Int = 50000
           ) {

  var buffer = ListBuffer[Seq[Any]]()

  // Initialize JDBC driver & connection pool
  var dataSource: DataSource = {
    val ds = new HikariDataSource()
    ds.setJdbcUrl("jdbc:mysql://localhost:3306/" + dbSettings.database + "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true")
    ds.addDataSourceProperty("autoCommit", "false")
    ds.setMaximumPoolSize(10)
    ds.addDataSourceProperty("user", dbSettings.user)
    ds.addDataSourceProperty("password", dbSettings.psw)
    ds
  }
  if(dbSettings.dbType.equals(PostgreSQL)){
    Class.forName("org.postgresql.Driver")
    dataSource = {
      val ds = new HikariDataSource()
      val url = String.join(
        "", "jdbc:postgresql://", dbSettings.host, ":", dbSettings.port, "/", dbSettings.database
      )
      ds.setJdbcUrl(url + "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true")
      ds.addDataSourceProperty("autoCommit", "false")
      ds.setMaximumPoolSize(10)
      ds.addDataSourceProperty("user", dbSettings.user)
      ds.addDataSourceProperty("password", dbSettings.psw)
      ds
    }
  } else {
    Class.forName("com.mysql.cj.jdbc.Driver")
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


  def close = writeValues


  private def writeValues = {
    val batchTxParams: Seq[Seq[Any]] = buffer.toList
    using(ConnectionPool.borrow()) { db =>
      val startTime = System.currentTimeMillis()/1000
      insertQuery.batch(batchTxParams: _*).apply()
      Table.writeTime = Table.writeTime + (System.currentTimeMillis()/1000 - startTime)
    }
    buffer.clear()
  }
}

