package tcs.examples.ethereum.levensthein

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Projections._
import scala.math.min
import tcs.examples.ethereum.levensthein.Helpers._

/**
  * Created by Danieru on 26/09/2017.
  */
object LevenstheinDistance {
  def main(args: Array[String]): Unit = {
    val mongo: MongoClient = MongoClient()
    val db: MongoDatabase = mongo.getDatabase("myDatabase")
    val collection: MongoCollection[Document] = db.getCollection("contractsWithCode")

    val documents: Seq[Document] = collection.find().results()
    val pairDocuments: Seq[(String,String)] = documents.map((doc: Document) => doc.toMap)
      .map(map => (map("contractAddress").asString.getValue, map("contractCode").asString.getValue))
    var combinedDocuments: Map[(String,String), Int] = Map()
    pairDocuments.foreach(pair1 => {
      pairDocuments.foreach(pair2 => {
        if(!pair1.equals(pair2)){
          println("processing couple " + (pair1._1, pair2._1))
          combinedDocuments += (pair1._1, pair2._1) -> levenstheinDistance(pair1._2, pair2._2)
        }
      })
    })

    combinedDocuments.toList.sortBy(_._2).foreach(println)
  }

  private def levenstheinDistance(s1: String, s2: String): Int = {
    val dist=Array.tabulate(s2.length+1, s1.length+1){(j,i)=>if(j==0) i else if (i==0) j else 0}

    for(j<-1 to s2.length; i<-1 to s1.length)
      dist(j)(i)=if(s2(j-1)==s1(i-1)) dist(j-1)(i-1)
      else min(min(dist(j-1)(i)+1, dist(j)(i-1)+1), dist(j-1)(i-1)+1)

    dist(s2.length)(s1.length)
  }
}
