package tcs.examples.ethereum.mongo


import tcs.blockchain.BlockchainLib
import tcs.db.DatabaseSettings
import tcs.mongo.Collection
import java.util.Date

import tcs.blockchain.ethereum.EthereumSettings

/**
  *
  * This script is an example of usage of the tool. If a local Parity node's JSON RPC Server is listening on port 8545,
  * and a MongoDB server is running, it will populate a Collection "VerifiedContracts" with:
  *
  * - contractAddress
  * - contractName
  * - date (to be fair, this is the date the block containing the tx that creates the contract was added to the
  *         blockchain, so there could be a little mismatch (at most a few days) compared to the real date.)
  * - sourceCode
  * - usesPermissions
  *
  *
  * @author Laerte
  * @author Luca
  */
object VCWithPermissions {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("http://localhost:8545", true))
    val mongo = new DatabaseSettings("myDatabase")
    val verifiedContracts = new Collection("VerifiedContracts", mongo)

    blockchain.start(1196010).end(1196020).foreach(block => {
//      if(block.number % 1000 == 0){
        println("Current block ->" + block.number)
//      }

      block.transactions.foreach(tx => {

        println("Block: " + block.number + " Transaction: " + tx.hash + " Address created: " + tx.addressCreated)

        if (tx.hasContract){

          val list = List(
            ("contractAddress", tx.contract.address),
            ("contractName", tx.contract.name),
            ("date", new Date(block.timeStamp.longValue()*1000)),
            ("dateVerified", tx.contract.verificationDate),
            ("sourceCode", tx.contract.sourceCode),
            ("usesPermissions", tx.contract.usesPermissions)
          )

          verifiedContracts.append(list)
        }

      })
    })

    verifiedContracts.close
  }
}
