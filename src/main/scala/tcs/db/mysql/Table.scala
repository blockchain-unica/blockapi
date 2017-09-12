package tcs.db.mysql

import scalikejdbc._
import tcs.db.DatabaseSettings

/**
  * Created by Livio on 11/09/2017.
  */
class Table(val createQuery: SQL[Nothing, NoExtractor], val dbSettings: DatabaseSettings){

  // Initialize JDBC driver & connection pool
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:mem:"+dbSettings.database, dbSettings.user, dbSettings.psw)

  // Ad-hoc session provider on the REPL
  implicit val session = AutoSession

  // Create table
  createQuery.execute.apply()

  // Insert values into table
  def insert(insertQuery: SQL[Nothing, NoExtractor]): Unit = {
    insertQuery.update.apply()
  }

  // All the connections are released, old connection pool will be abandoned
  def close() {
    ConnectionPool.close()
  }
}
