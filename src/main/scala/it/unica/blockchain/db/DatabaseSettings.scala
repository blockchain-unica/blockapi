package it.unica.blockchain.db

/**
  * Created by stefano on 13/06/17.
  */
class DatabaseSettings(val database: String,
                       val dbType: Database = Mongo,
                       val user: String = "",
                       val psw: String = "",
                       val host: String = "127.0.0.1"
                   ) {

  val port: String =
    if(dbType.equals(MySQL))
      "3306"
    else if(dbType.equals(Mongo))
      "27017"
    else if(dbType.equals(PostgreSQL))
      "5432"
    else if(dbType.equals(Fuseki))
      "3030"
    else
      ""
}

class Database

object Mongo extends Database

object MySQL extends Database

object Fuseki extends Database

object PostgreSQL extends Database
