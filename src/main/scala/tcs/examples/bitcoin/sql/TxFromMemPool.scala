package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.utils.DateConverter.convertDate
import java.io._


object TxFromMemPool
{
  


  def main(args: Array[String]): Unit =
  {

    val blockchain = BlockchainLib.getBitcoinBlockchain(new BitcoinSettings("user", "password", "8332", MainNet))

    val startTime = System.currentTimeMillis()/1000

    var hashList:List[String] = Nil
    /*val pw = new PrintWriter(new File("memPoolTransactions.txt" ))


    def printToFile(hashList: List[String]): Unit= 
    {
      for (hash <- hashList)
      {
        try
        {
          
          pw.write(blockchain.getTransaction(hash).getPrintableTransaction())
        } 
        catch
        {
          case e: RuntimeException => println("error at hash " + hash + ": " + e.toString())
          case _: Throwable => println("Got some other kind of exception")
        }
      }
    }

    println(hashList.length)
    printToFile(hashList)*/
    for(hash <- blockchain.getMemPool())
    {
        hashList = hash :: hashList
    }

    do{
        for(hash <- blockchain.getMemPool())
        {
            if(!hashList.contains(hash))
            {
                hashList = hash :: hashList
            }
        }
        println(hashList.length)
    }while((System.currentTimeMillis() / 1000)>=300)
    //pw.close
  }
}