package it.unica.blockchain.db.fuseki

import java.util.{Calendar, Date}

import org.apache.jena.query.{QueryExecution, ReadWrite, ResultSet}
import org.apache.jena.rdf.model.{Model, ModelFactory, Property, Resource}
import org.apache.jena.rdfconnection.RDFConnectionFactory
import it.unica.blockchain.db.DatabaseSettings
import it.unica.blockchain.utils.converter.DateConverter


class GraphModel(
                  var dbSettings: DatabaseSettings,
                  var maxTriple: Long = 50000l
                ) {

  var url: String = "http://" + dbSettings.host + ":" + dbSettings.port + "/" + dbSettings.database

  private var model: Model = _
  private var load: Boolean = true

  def addStatements(resource: String = "",
                    properties: List[(Property, Any)],
                    appendToResource: (String, Property) = (null, null)
                   ): Unit = {

    if (model != null)
      if (model.size() > maxTriple)
        loadTriples()


    if (load) {
      model = ModelFactory.createDefaultModel()
      load = false
    }

    var res: Resource = null

    if (resource == "")
      res = model.createResource()
    else
      res = model.createResource(resource)

    properties.foreach(t => {
      t._2 match {
        case s: String => res.addProperty(t._1, s)
        case r: Resource => res.addProperty(t._1, r)
        case d: Date => res.addLiteral(t._1, DateConverter.getCalendarFromDate(d))
        case o => res.addLiteral(t._1, o)
      }
    })

    if (appendToResource._1 != null && appendToResource._2 != null) {
      model.createResource(appendToResource._1).addProperty(appendToResource._2, res)
    }
    res = null
  }

  def addPropertiesToResource(resource: String,
                              properties: List[(Property, Any)]
                             ): Unit = {

    if (model != null) {
      var res = model.createResource(resource)
      properties.foreach(p => {
        p._2 match {
          case s: String => res.addProperty(p._1, s)
          case r: Resource => res.addProperty(p._1, r)
          case d: Date => res.addLiteral(p._1, DateConverter.getCalendarFromDate(d))
          case o => res.addLiteral(p._1, o)
        }
      })
    }
  }

  def resource(str: String): Resource = {
    if(model == null) {
      model = ModelFactory.createDefaultModel()
      load = false
    }
    model.createResource(str)
  }

  def commit(): Unit = {
    if (model != null) {
      loadTriples()
    }
  }

  def maxCount() : Boolean = {
    if (model != null)
      model.size() > maxTriple
    else
      false
  }

  def getModelSize() = {
    model.size()
  }

  private def loadTriples(): Unit = {

    //indicazioni memoria
    println("Total memory: " + Runtime.getRuntime.totalMemory() / Math.pow(1024, 2))
    println("Max memory: " + Runtime.getRuntime.maxMemory() / Math.pow(1024, 2))
    println("Free memory: " + Runtime.getRuntime.freeMemory() / Math.pow(1024, 2))

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

  def datasetQuery(query: String): ResultSet = {
    val conn = RDFConnectionFactory.connect(url)

    val qExec: QueryExecution = conn.query(query)
    qExec.execSelect()
  }

  def deleteDataset(): Unit = {
    val conn = RDFConnectionFactory.connect(url)
    conn.begin(ReadWrite.WRITE)
    conn.delete()
    conn.close()
  }
}

