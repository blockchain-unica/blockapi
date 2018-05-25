package tcs.mongo


import com.mongodb.{MongoClient, MongoClientURI}
import org.mongodb.scala.bson._
import tcs.db.DatabaseSettings
import tcs.db.mongo.BsonConverter

/**
  * Created by stefano on 2/05/18.
  */
class Collection(val name: String, val settings: DatabaseSettings) {

  private val connectionString =
    if (settings.user.eq(""))
      new MongoClientURI("mongodb://" + settings.host + ":" + settings.port)
    else
      new MongoClientURI("mongodb://" + settings.user + ":" + settings.psw + "@" + settings.host + ":" + settings.port)

  private val mongoClient = new MongoClient(connectionString)


  private val database = mongoClient.getDatabase(settings.database)
  private val collection = database.getCollection(name)

  def append(list: List[(String, Any)]): Unit = {

    var doc: Document = Document()

    doc = list.map(a => {
      a._2 match {
        case l: List[Any] => (a._1, if (l.isEmpty) "Empty List" else l)
        case _ => (a._1, a._2)
      }
    }).map(BsonConverter.convertPair).reduce(_ ++ _)

    insert(doc)

  }

  def insert(doc: Document): Unit = {
    //collection.insertOne(doc, (result: Void, t: Throwable) => {})
    collection.insertOne(doc)
  }

  def close = {
    mongoClient.close()
  }

  def append(x: Document) = insert(x)
}


