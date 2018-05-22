package tcs.examples.ethereum.sql


import tcs.blockchain.BlockchainLib
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, PostgreSQL}
import scalikejdbc._
import tcs.blockchain.ethereum.EthereumSettings
import tcs.externaldata.rates.EthereumRates

object EthereumBalances {
    def main(args: Array[String]): Unit = {
        val start = 4000000
        val nBlocks = 10000
        val end = start + nBlocks
        val blockchain = BlockchainLib.getEthereumBlockchain(new EthereumSettings("https://mainnet.infura.io/lGhdnAJw7n56K0xXGP3i:8545")).start(start).end(end)
        val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres", "0")

        println("start block: " + start)
        println("  end block: " + end)
        println("number of blocks: " + nBlocks + "\n")

        // map from the address to the array of data; see the db table at the end of the code
        // address => received, sent, final, n_sent_to, n_received_from
        var map = Map[String, Array[Double]]()

        val weiInEth = 1e18

        println("foreach starting...")
        blockchain.foreach(block => {
            if (block.height % 10 == 0)
                println(block.height + ") n transactions inside: " + block.txs.length)

            block.txs.foreach(transaction => {
                val rateInTransactionDate = EthereumRates.getRate(transaction.date)
//                println(transaction.date + " - " + rateInTransactionDate)
                val dollars = transaction.value.toDouble / weiInEth * rateInTransactionDate

                if (map.contains(transaction.from)) {
                    // if the address "from" is already present in the map,
                    // we can update its balance
                    /** how to update:
                      * the amount of dollars goes from the "from" address to the "to" address
                      * 1. add "dollars" to the sent ethereums
                      * 2. substract "dollars" from the final balance
                      * 3. increment the number of transactions that have taken
                      *    eth from "from", i.e. increment n_sent
                      */

                    // current balance
                    val balance: Array[Double] = map(transaction.from)

                    // update of the quantities

                    // update of the quantity of eth sent
                    balance(1) += dollars // sent += dollars

                    // update of the final balance
                    balance(2) -= dollars // final -= dollars

                    // update of the number of transaction that the address has sent to
                    balance(3) += 1 // n_sent ++
                }
                else {
                    // the address "from" is not in the map, so it must be added
                    // only if there is a transaction sending money
                    if (dollars > 0 && transaction.from != null && transaction.from != "")

                    /** add an entry to the map which goes from the "from" address to
                      * an array with:
                      * received: 0
                      * sent: dollars
                      * final: -dollars
                      * n_sent: 1
                      * n_received: 0
                      */
                        map += transaction.from -> Array(0, dollars, -dollars, 1, 0)
                }

                if (map.contains(transaction.to)) {
                    // if the address "to" is already present in the map,
                    // we can update its balance

                    /** how to update:
                      * the "dollars" goes from the "from" address to the "to" address
                      * 1. add "dollars" to the received ethereums
                      * 2. add "dollars" to the final balance
                      * 3. increment the number of transactions that have sent
                      *     eth to "to", i.e. increment n_received
                      */

                    // current balance
                    val balance: Array[Double] = map(transaction.to)

                    // update of the quantity of eth sent
                    balance(0) += dollars // received += dollars

                    // update of the final balance
                    balance(2) += dollars // final += dollars

                    // update of the number of transaction that have taken eth from that address
                    balance(4) += 1 // n_received ++
                }
                else {
                    // the address "to" is not in the map, so it must be added
                    // only if there is a transaction sending money
                    if (dollars > 0 && transaction.to != null && transaction.to != "")

                    /** add an entry to the map which goes from the "to" address to
                      * an array with:
                      * received: dollars
                      * sent: 0
                      * final: dollars
                      * n_sent: 0
                      * n_received: 1
                      */
                        map += transaction.to -> Array(dollars, 0, dollars, 0, 1)
                }
            })
        })
        println("...foreach ended")


        /**
          * Table:
          * address: ~
          * sent: amount of ethereum that the address has sent
          * received: amount of ethereum that the address has received
          * total: ~
          * n_sent: number of transactions which the address has sent etheruem,
          *         or number of transactions that have withdrawn from that address
          * n_received: number of transactions which the address has received
          *             etheruem from, or number of transactions that have sent
          *             ethereum to that address
          */
        val blockTable = new Table(
            sql"""
                      DROP TABLE IF EXISTS balances;
                CREATE TABLE IF NOT EXISTS balances(
                address CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
                received DOUBLE PRECISION,
                sent DOUBLE PRECISION,
                final DOUBLE PRECISION,
                n_sent_to DOUBLE PRECISION,
                n_received_from DOUBLE PRECISION
                )
                """,
            sql"""
                INSERT INTO balances(address, received, sent, final, n_sent_to, n_received_from)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
            pg, nBlocks
        )


        println("table created\ninserting data...")

        for(address <- map.keys)
            blockTable.insert(address :: map(address).toList) // insert each element of the map in the db

        println("...data inserted")

        blockTable.close
        println("table closed")

        println("END")
    }
}