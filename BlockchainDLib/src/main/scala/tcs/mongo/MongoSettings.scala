package tcs.mongo

/**
  * Created by stefano on 13/06/17.
  */
class MongoSettings(val database: String,
                    val host: String = "localhost",
                    val port: String = "27017",
                    val user: String = "",
                    val psw: String = ""
                   ) {
}
