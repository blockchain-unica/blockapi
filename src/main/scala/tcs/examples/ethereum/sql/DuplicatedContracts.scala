package tcs.examples.ethereum.sql

//import scalikejdbc._
import tcs.db.{DatabaseSettings, MySQL}
import tcs.blockchain.BlockchainLib
import tcs.blockchain.ethereum.EthereumSettings
//import tcs.db.sql.Table
import tcs.mongo.Collection

object DuplicatedContracts {

  def main(args: Array[String]): Unit = {

    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("https://mainnet.infura.io/lGhdnAJw7n56K0xXGP3i:8545", true))
    val mongo = new DatabaseSettings("ethereum")
    //val pg = new DatabaseSettings("ethereum", MySQL, "root", "toor")
    val contracts = new Collection("contracts", mongo)

    /*val contractTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS contract(
            address CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            source_code LONGTEXT NOT NULL,
            date TIMESTAMP NOT NULL,
            name CHARACTER VARYING(100) NOT NULL
          )
         """,
      sql"""
          INSERT INTO contract(address, source_code, date, name) VALUES (?, ?, ?, ?)
         """,
      pg, 1
    )*/

    blockchain.start(49400).foreach(block => {

      if (block.height % 100 == 0) {
        println(block.height)
      }

      block.txs.foreach(tx => {

        println("Block: " + block.height + " Transaction: " + tx.hash + " Address created: " + tx.addressCreated)

        if (tx.hasContract) {
          val list = List(
            ("contractAddress", tx.contract.address),
            ("contractName", tx.contract.name),
            ("date", block.date),
            ("sourceCode", tx.contract.sourceCode)
          )
          contracts.append(list)
        }

      })
    })

    contracts.close
  }

}
