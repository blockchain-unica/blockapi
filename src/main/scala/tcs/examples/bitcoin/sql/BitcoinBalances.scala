package tcs.examples.bitcoin.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.blockchain.bitcoin.{BitcoinSettings, MainNet}
import tcs.db.{DatabaseSettings, MySQL}
import tcs.db.sql.Table
import tcs.utils.converter.DateConverter
import tcs.externaldata.rates.BitcoinRates

import scala.collection.mutable
import scala.collection.mutable.Map

object BitcoinBalances
{
  /**
    * Definition of custom type for balance, data retrieved for each address:
    *
    * 1) total dollars received
    * 2) total dollars sent
    * 3) total balance
    * 4) transaction input count
    * 5) transaction output count
    *
    */
  type balance = (Double, Double, Double, BigInt, BigInt)

  def main(args: Array[String]): Unit = {
    // - Bitcoind settings, retrieveInputValues set as true
    var bitcoindSett = new BitcoinSettings("user", "password", "8332", MainNet, true)

    // - MySQL database settings
    val mySQL = new DatabaseSettings("bitcoinBalances", MySQL, "user", "password")

    // - Conversion rate from satoshi to bitcoin
    val satoshiToBTC = 0.00000001

    // - Slice of blockchain to analyse
    val startingBlock = 0
    val endingBlock = 100000

    // - Get blockchain slice
    var bitcoinBlockchain = BlockchainLib.getBitcoinBlockchain(bitcoindSett)

    // - Table definition
    val outTable = new Table(
      sql"""
           CREATE TABLE IF NOT EXISTS balances(
           	  address VARCHAR(256) NOT NULL PRIMARY KEY,
              inValue DOUBLE UNSIGNED,
              outValue DOUBLE UNSIGNED,
              totalValue DOUBLE,
           	  inputTxNumber BIGINT UNSIGNED,
              outputTxNumber BIGINT UNSIGNED
           )""",
      sql"""insert into balances(address, inValue, outValue, totalValue, inputTxNumber, outputTxNumber) values (?, ?, ?, ?, ?, ?)""",
      mySQL)

    // - Mutable map containing addresses and their balances, with the user defined type balance
    var balances = mutable.Map[String, balance]()

    // - Updating the map for each block
    bitcoinBlockchain.start(startingBlock).end(endingBlock).foreach(block => {

      // - Updating the map for each transaction
      block.txs.foreach(tx => {
        // - Get conversion rate using date of transaction
        val conversionRate = BitcoinRates.getRate(tx.date)

        // - Updating the map for each input
        tx.inputs.foreach(in => {
          if (in.getAddress(MainNet).isDefined) {// checks if None
            // - Get address of input
            val addr = in.getAddress(MainNet).get.toBase58

            // - Add the address to the map if it's not already there
            if (!balances.contains(addr))
              balances += addr -> (0.0, 0.0, 0.0, BigInt(0), BigInt(0))

            // - Retrieve balance of address from the map
            val balanceOfAddress = balances(addr)

            // - Calculate the value in dollars
            val valueInDollars = in.value.toDouble * satoshiToBTC * conversionRate

            /* - Update the map's entry for the address, the value in dollars gets added to the total output and
             * - subtracted to the current balance, the output transactions counter is also incremented
             */
            balances(addr) = (
                balanceOfAddress._1,                  // input value
                balanceOfAddress._2 + valueInDollars, // output value
                balanceOfAddress._3 - valueInDollars, // total balance value
                balanceOfAddress._4,                  // input tx counter
                balanceOfAddress._5 + 1               // output tx counter
            )
          }
        })

        // - Updating the map for each output
        tx.outputs.foreach(out => {
          if (out.getAddress(MainNet).isDefined) { // checks if None
            // - Get address of input
            val addr = out.getAddress(MainNet).get.toBase58

            // - Add the address to the map if it's not already there
            if (!balances.contains(addr))
              balances += addr -> (0.0, 0.0, 0.0, BigInt(0), BigInt(0))

            // - Retrieve balance of address from the map
            val data = balances(addr)

            // - Calculate the value in dollars
            val valueInDollars = out.value.toDouble * satoshiToBTC * conversionRate

            /* - Update the map's entry for the address, the value in dollars gets added to the total input
             * - and to the total balance, the input transactions counter is also incremented
             */
            balances(addr) = (
                data._1 + valueInDollars, // input value
                data._2,                  // output value
                data._3 + valueInDollars, // total balance value
                data._4 + 1,              // input counter
                data._5                   // output counter
              )
          }
        })
      })
    })


    // Add each entry of the map into the table
    for(balanceInfo <- balances.toSeq)
    {
      outTable.insert(
        balanceInfo._1 ::     //address
        balanceInfo._2._1 ::  //input value
        balanceInfo._2._2 ::  //output value
        balanceInfo._2._3 ::  //total balance value
        balanceInfo._2._4 ::  //input tx counter
        balanceInfo._2._5 ::  //output tx counter
        List()
      )
    }

    outTable.close
  }
}
