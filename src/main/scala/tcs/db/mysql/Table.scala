package tcs.db.mysql

import org.mongodb.scala.{Completed, Observer}
import org.mongodb.scala.bson.Document
import scalikejdbc._
import tcs.db.DatabaseSettings
import tcs.utils.Convert

/**
  * Created by Livio on 11/09/2017.
  */
class Table(val name: String, val dbSettings: DatabaseSettings){

  // after loading JDBC drivers
  ConnectionPool.singleton(dbSettings.host, dbSettings.user, dbSettings.psw)

  val settings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 20,
    connectionTimeoutMillis = 3000L,
    validationQuery = "select 1 from dual")


  def append(list: List[(String, Any)]): Unit = {

    //TODO: handle lists
    val doc = list.map(Convert.convertPair(_)).reduce((a, b) => a ++ b)

//    collection.insertOne(doc).subscribe(new Observer[Completed] {
//      override def onNext(result: Completed): Unit = {}
//      override def onError(e: Throwable): Unit = {}
//      override def onComplete(): Unit = {}
//    })
//    val d = Document("redeemedTxHash" -> 1)

  }

  def close() {
    // all the connections are released, old connection pool will be abandoned
    ConnectionPool.close()
  }
}
