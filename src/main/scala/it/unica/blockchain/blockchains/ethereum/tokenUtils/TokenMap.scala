package it.unica.blockchain.blockchains.ethereum.tokenUtils

import java.io.{BufferedWriter, File, FileWriter}

import it.unica.blockchain.blockchains.ethereum.tokenUtils.TokenType.TokenType

import scala.collection.mutable
import scala.io.{BufferedSource, Source}

/** Defines a list of the most known tokens */

object TokenMap
{
  private var mapERCAddress: mutable.Map[String, TokenType] = mutable.Map[String, TokenType]()
  private var mapAdded: mutable.Map[String, TokenType] = mutable.Map[String, TokenType]()

  private val file_path_ERC20 :String = "src/main/scala/it/unica/blockchain/externaldata/token/ERC20.txt"
  private val file_path_ERC721 :String = "src/main/scala/it/unica/blockchain/externaldata/token/ERC721.txt"

  private val sourceERC20 :BufferedSource = Source.fromFile(file_path_ERC20)
  private val sourceERC721 :BufferedSource = Source.fromFile(file_path_ERC721)

  for(line <- sourceERC20.getLines)
    mapERCAddress += (line -> TokenType.ERC20)
  for(line <- sourceERC721.getLines)
    mapERCAddress += (line -> TokenType.ERC721)

  sourceERC20.close
  sourceERC721.close

  def getMap(): mutable.Map[String, TokenType] ={
    mapERCAddress
  }

  def add(address: String, tokenType: TokenType){
    mapERCAddress += (address -> tokenType)
    mapAdded += (address -> tokenType)
  }

  def updateFiles(){
    if(mapAdded.nonEmpty) {
      val file20 = new File(file_path_ERC20)
      val file721 = new File(file_path_ERC721)

      val bw20 = new BufferedWriter(new FileWriter(file20, true))
      val bw721 = new BufferedWriter(new FileWriter(file721, true))

      for (el <- mapAdded) {
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
