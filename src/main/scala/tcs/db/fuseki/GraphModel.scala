package tcs.db.fuseki

import org.apache.jena.query.{QueryExecution, QuerySolution, ReadWrite, ResultSet}
import org.apache.jena.rdf.model.{Model, ModelFactory, Property, Resource}
import org.apache.jena.rdfconnection.RDFConnectionFactory
import tcs.db.DatabaseSettings

import scala.collection.JavaConversions._

class GraphModel(
            var dbSettings: DatabaseSettings,
            var maxTriple : Long = 50000l
           ) {

    var url : String = "http://localhost:" + dbSettings.port + "/" + dbSettings.database

    private var model : Model = null
    private var load : Boolean = true
    private var count = 0

    def addStatements(resource: String = "",
                      properties : List[(Property, Any)],
                      appendToResource : (String, Property) = (null, null)
                      ) : Unit = {

      if(count > maxTriple && model != null){
        loadTriples()
      }


      if(load) {
        model = ModelFactory.createDefaultModel()
        count = 0
        load = false
      }

      var res : Resource = null

      if(resource == "")
        res = model.createResource()
      else
        res = model.createResource(resource)

      properties.foreach(t => {
        t._2 match {
          case s : String => {res.addProperty(t._1, s)} //Literal
          case r : Resource => {res.addProperty(t._1, r)}
          case _ => "Type error"
        }

        count += 1
      })

      if(appendToResource._1 != null && appendToResource._2 != null){
        model.createResource(appendToResource._1).addProperty(appendToResource._2, res)
        count += 1
      }

      res = null

      println("Count: " + count)
    }

    def addPropertiesToResource(resource : String,
                                properties : List[(Property, Any)]
                               ) : Unit = {

      if (model != null) {
        var res = model.getResource(resource)
        properties.foreach(p => {
          p._2 match {
            case s: String => {
              res.addProperty(p._1, s)
            } //Literal
            case r: Resource => {
              res.addProperty(p._1, r)
            }
            case _ => "Type error"
          }
        })
      }
    }

    def commit(): Unit ={
      if(model != null){
        loadTriples()
      }
    }

    private def loadTriples(): Unit ={

      //indicazioni memoria
      println("Total memory: "+Runtime.getRuntime.totalMemory()/Math.pow(1024,2))
      println("Max memory: "+Runtime.getRuntime.maxMemory()/Math.pow(1024,2))
      println("Free memory: "+Runtime.getRuntime.freeMemory()/Math.pow(1024,2))

      var conn = RDFConnectionFactory.connect(url)
      conn.begin(ReadWrite.WRITE)
      conn.load(model)
      conn.commit()
      conn.close()
      conn = null

      model.close()
      model = null
      load = true

    }

    def datasetQuery(query : String) : ResultSet = {
      val conn = RDFConnectionFactory.connect(url)

      val qExec : QueryExecution = conn.query(query)
      val rs : ResultSet = qExec.execSelect()

      return rs
    }

    def deleteDataset() : Unit = {
      val conn = RDFConnectionFactory.connect(url)
      conn.begin(ReadWrite.WRITE)
      conn.delete()
      conn.close()
    }
}

