package tcs.examples.ethereum.sql

import java.util.Date

import scalikejdbc._
import tcs.blockchain.BlockchainLib
import tcs.custom.ethereum.ICO
import tcs.db.sql.Table
import tcs.db.{DatabaseSettings, PostgreSQL}


object ICOInfos {
  def main(args: Array[String]): Unit = {
    val blockchain = BlockchainLib.getEthereumBlockchain("http://52.38.68.64:8545")
      .setStart(3224233)
    val pg = new DatabaseSettings("ethereum", PostgreSQL, "postgres")

    val blockTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS block(
            hash CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            number INTEGER,
            parentHash CHARACTER VARYING(100),
            timestamp TIMESTAMP,
            author CHARACTER VARYING(100),
            miner CHARACTER VARYING(100)
          )
         """,
      sql"""
          INSERT INTO block(hash, number, parentHash, timestamp,
          author, miner)
          VALUES (?, ?, ?, ?, ?, ?)
         """,
      pg, 1
    )

    val txTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS transaction(
            hash CHARACTER VARYING(100) NOT NULL PRIMARY KEY,
            nonce NUMERIC(30,2),
            transactionIndex INTEGER,
            txFrom CHARACTER VARYING(100),
            txTo CHARACTER VARYING(100),
            txValue NUMERIC(30,2),
            creates CHARACTER VARYING(100),
            gas NUMERIC(30,2),
            gasPrice NUMERIC(30,2),
            blockHash CHARACTER VARYING(100) REFERENCES block(hash)
          )
         """,
      sql"""
            INSERT INTO transaction(hash, nonce, transactionIndex,
            txFrom, txTo, txValue, creates, gas, gasPrice, blockHash)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         """,
      pg, 1
    )

    val internalTxTable = new Table(
      sql"""
          CREATE TABLE IF NOT EXISTS internal_transaction(
            id SERIAL PRIMARY KEY,
            parentTxHash CHARACTER VARYING(100) REFERENCES transaction(hash),
            txType CHARACTER VARYING(100),
            itxFrom CHARACTER VARYING(100),
            itxTo CHARACTER VARYING(100),
            itxValue NUMERIC(30,2)
          )
         """,
      sql"""
            INSERT INTO internal_transaction(parentTxHash, txType,
            itxFrom, itxTo, itxValue)
            VALUES (?, ?, ?, ?, ?)
         """,
      pg, 1
    )

    val icoTable = new Table(
      sql"""
         CREATE TABLE IF NOT EXISTS ico(
          icoId SERIAL PRIMARY KEY,
          tokenName CHARACTER VARYING(50),
          tokenSymbol CHARACTER VARYING(15),
          contractAddress CHARACTER VARYING(100),
          marketCap NUMERIC,
          totalSupply NUMERIC,
          blockchain CHARACTER VARYING(30),
          priceUSD NUMERIC,
          priceETH NUMERIC,
          priceBTC NUMERIC,
          hypeScore NUMERIC(5,2),
          riskScore NUMERIC(5,2),
          investmentRating CHARACTER VARYING(10),
          txCreatorHash CHARACTER VARYING(100) REFERENCES transaction(hash)
         ) """,
      sql"""
          INSERT INTO ico(tokenName, tokenSymbol, contractAddress, marketCap,
          totalSupply, blockchain, priceUSD, priceETH, priceBTC,
          hypeScore, riskScore, investmentRating)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         """,
      pg, 1
    )

    blockchain.foreach(block => {
      if (block.number % 100 == 0) {
        println(block.number)
      }
      blockTable.insert(Seq(
        block.hash, block.number, block.parentHash,
        new Date(block.timeStamp.longValue() * 1000), block.author, block.miner
      ))
      block.transactions.foreach(tx => {
        txTable.insert(Seq(
          tx.hash, tx.nonce, tx.transactionIndex, tx.from,
          tx.to, tx.value, tx.creates, tx.gas,
          tx.gasPrice, tx.blockHash
        ))
        if (tx.creates != null) {
            val ico = new ICO(tx.creates)
            if(ico.itExists){
              icoTable.insert(Seq(
                ico.getName, ico.getSymbol, ico.getContractAddress,
                ico.getMarketCap, ico.getTotalSupply, ico.getBlockchain,
                ico.getUSDPrice, ico.getETHPrice, ico.getBTCPrice,
                ico.getHypeScore, ico.getRiskScore, ico.getInvestmentRating
              ))
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
