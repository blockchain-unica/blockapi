package it.unica.blockchain.blockchains.ethereum

import java.io.{BufferedWriter, File, FileWriter}

import it.unica.blockchain.blockchains.ethereum.TokenType.TokenType

import scala.collection.mutable
import scala.io.{BufferedSource, Source}

object TokenList
{
  private var listERCAddress: mutable.Map[String, TokenType] = mutable.Map[String, TokenType]()
  private var listAdded: mutable.Map[String, TokenType] = mutable.Map[String, TokenType]()

  private val file_path_ERC20 :String = "src/main/scala/it/unica/blockchain/externaldata/token/ERC20.txt"
  private val file_path_ERC721 :String = "src/main/scala/it/unica/blockchain/externaldata/token/ERC721.txt"

  private val sourceERC20 :BufferedSource = Source.fromFile(file_path_ERC20)
  private val sourceERC721 :BufferedSource = Source.fromFile(file_path_ERC721)

  for(line <- sourceERC20.getLines)
    listERCAddress += (line -> TokenType.ERC20)
  for(line <- sourceERC721.getLines)
    listERCAddress += (line -> TokenType.ERC721)

  sourceERC20.close
  sourceERC721.close

  def getList(): mutable.Map[String, TokenType] ={
    listERCAddress
  }

  def add(address: String, tokenType: TokenType){
    listERCAddress += (address -> tokenType)
    listAdded += (address -> tokenType)
  }

  def updateFiles(){
    if(listAdded.nonEmpty) {
      val file20 = new File(file_path_ERC20)
      val file721 = new File(file_path_ERC721)

      val bw20 = new BufferedWriter(new FileWriter(file20, true))
      val bw721 = new BufferedWriter(new FileWriter(file721, true))

      for (el <- listAdded) {
        if (el._2 == TokenType.ERC20)
          bw20.write(el._1 + "\n")
        else if (el._2 == TokenType.ERC721)
          bw721.write(el._1 + "\n")
      }

      bw20.close()
      bw721.close()
    }
  }
}
