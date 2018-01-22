package tcs.examples.ethereum.sql

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.custom.ethereum.ICO
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, PostgreSQL}


object ICOInfos {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain("https://mainnet.infura.io/OCPoiiZvFpsPKZcOMGaG")
      .setStart(0).setEnd(1370000)
    val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres")

    val blockTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS block(
            hash CHARACTER VARYING(50) NOT NULL PRIMARY KEY,
            number INTEGER,
            parentHash CHARACTER VARYING(50) REFERENCES block(hash),
            timestamp TIMESTAMP,
            author CHARACTER VARYING(50),
            miner CHARACTER VARYING(50)
          )
         """,
      sql"""
          INSERT INTO block(hash, number, parentHash, timestamp,
          author, miner)
          VALUES (?, ?, ?, ?, ?, ?)
         """,
      pg
    )

    val txTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS transaction(
            hash CHARACTER VARYING(50) NOT NULL PRIMARY KEY,
            nonce NUMERIC(30,2),
            transactionIndex INTEGER,
            txFrom CHARACTER VARYING(50),
            txTo CHARACTER VARYING(50),
            txInput CHARACTER VARYING(50),
            txValue NUMERIC(30,2),
            gas NUMERIC(30,2),
            gasPrice NUMERIC(30,2),
            blockHash CHARACTER VARYING(50) REFERENCES block(hash)
          )
         """,
      sql"""
            INSERT INTO transaction(hash, nonce, transactionIndex,
            txFrom, txTo, txInput, txValue, gas, gasPrice)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
         """,
      pg
    )

    val internalTxTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS internal_transaction(
            id INTEGER NOT NULL PRIMARY KEY,
            parentTxHash CHARACTER VARYING(50) REFERENCES transaction(hash),
            txType CHARACTER VARYING(50),
            itxFrom CHARACTER VARYING(50),
            itxTo CHARACTER VARYING(50),
            itxValue NUMERIC(30,2)
          )
         """,
      sql"""
            INSERT INTO internal_transaction(parentTxHash, txType,
            itxFrom, itxTo, itxValue)
            VALUES (?, ?, ?, ?, ?)
         """,
      pg
    )

    val icoTable = new Table(
      sql"""
         CREATE TABLE IF NOT EXISTS ico(
          icoId INTEGER NOT NULL PRIMARY KEY,
          tokenName CHARACTER VARYING(25),
          tokenSymbol CHARACTER VARYING(10),
          contractAddress CHARACTER VARYING(50),
          marketCap NUMERIC(20,2),
          totalSupply NUMERIC(20,2),
          blockchain CHARACTER VARYING(15),
          priceUSD NUMERIC(8,8),
          priceETH NUMERIC(8,8),
          priceBTC NUMERIC(8,8),
          hypeScore NUMERIC(3,2),
          riskScore NUMERIC(3,2),
          investmentRating CHARACTER VARYING(10),
          txCreatorHash CHARACTER VARYING(50) REFERENCES transaction(hash)
         ) """,
      sql"""
          INSERT INTO ico(tokenName, tokenSymbol, contractId, marketCap,
          totalSupply, blockchain, priceUSD, priceETH, priceBTC,
          hypeScore, riskScore, investmentRating)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         """,
      pg)

    blockchain.foreach(block => {
      println(block.number)
      blockTable.insert(Seq(
        block.hash, block.number, block.parentHash,
        block.timeStamp, block.author, block.miner
      ))
      block.transactions.foreach(tx => {
        txTable.insert(Seq(
          tx.hash, tx.nonce, tx.transactionIndex, tx.from,
          tx.to, tx.input, tx.value, tx.gas, tx.gasPrice,
          tx.blockHash
        ))
        if(!tx.creates.isEmpty){
          try{
            val ico = new ICO(tx.creates)
            icoTable.insert(Seq(
              ico.getName, ico.getSymbol, ico.getContractAddress,
              ico.getMarketCap, ico.getTotalSupply, ico.getBlockchain,
              ico.getUSDPrice, ico.getETHPrice, ico.getBTCPrice,
              ico.getHypeScore, ico.getRiskScore, ico.getInvestmentRating
            ))
          } catch {
            case e: Exception => {}
          }
        }
      })
      block.internalTransactions.foreach(itx => {
        internalTxTable.insert(Seq(
          itx.parentTxHash, itx.txType,
          itx.from, itx.to, itx.value
        ))
      })
    })

    blockTable.close
    txTable.close
    internalTxTable.close
    icoTable.close
  }
}
