# Blockchain analytics API
A Scala framework for the development of general-purpose analytics on blockchains, by [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu), [Stefano Lande](http://tcs.unica.it/members/stefano-lande), and [Daniele Stefano Ferru](https://github.com/ferruvich).

Our framework allows to combine data *within* the blockchain 
with data from the *outside* (e.g. exchange rates and tags). 
The workflow consists in two steps: 
1. construct a view of the blockchain and save it in a database;
2. analyse the view by using the query language of the database.

The blockchains currently supported are Bitcoin and Ethereum.
The DBMS currently supported are MongoDB and MySQL.

The library is dicussed in [A general framework for blockchain analytics](https://arxiv.org/pdf/1707.01021.pdf).

More material is available in the [project page](http://blockchain.unica.it/projects/blockchain-analytics/).

The Scaladoc (uploaded on this repository in the [doc folder](https://github.com/bitbart/blockchain-analytics-api/tree/master/docs)) is also available at the following [link](https://bitbart.github.io/blockchain-analytics-api/).

### Install prerequisites
Prerequisites depends on the target analyses.
Generally speaking the framework needs at least one blockchain client and one DBMS.

1. Blockchain clients:
    * [Bitcoin Core](https://bitcoin.org/en/bitcoin-core/) (extracts data from Bitcoin)
    * [Parity](https://parity.io/) (extracts data from Ethereum)
2. DBMS:
    * [MongoDB](https://www.mongodb.com/what-is-mongodb) (constructs a NoSQL view of the data)
    * [MySQL](https://www.mysql.com/) (constructs a SQL view of the data)
    * [PostgreSQL](https://www.postgresql.org/) (constructs a SQL view of the data)
3. Either
    * use an IDE for executing a Scala SBT project (we used [IntelliJ IDEA](https://www.jetbrains.com/idea/)) or
    * use the command line in place of an IDE (Install [SBT](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html))

Before building the project, execute the following commands:

```bash
git clone https://github.com/bitbart/bitcoinj.git
cd bitcoinj
git checkout segwit
mvn install -DskipTests
cd core
mvn install -DskipTests
```
### ICO Analytics
In order to use the `ICO` class (created to retrieve ICOs data) you must require the respective API keys:
1. [EtherScan](https://etherscan.io) 
    * It is sufficient to sign up and then go [here](https://etherscan.io/myapikey)
2. [ICOBench](https://icobench.com/)
    * [Sign Up](https://icobench.com/register), then require an API key [here](https://icobench.com/developers) 
3. [Ethplorer](https://github.com/EverexIO/Ethplorer/wiki/Ethplorer-API)
    * There is a default, free key: `freekey`. 
      If you need more data or highload of service, you can to get personal API key. 

After doing these steps, do the following:
1. Copy `EtherScan` key into `EtherScanAPI.apiKey` attribute
2. Copy `ICOBench` private key and public key into `ICOBenchAPI.privateKey` and `ICOBenchAPI.publicKey` attributes respectively
3. Copy `Ethplorer` key into `EthplorerAPI.apiKey` attribute

### Install blockchain analytics API
1. Execute the blockchain client in order to obtain a local copy of the target blockchain (this process may take several hours)
2. Enable the client RPC calls.
    * In the Bitcoin Core case
        ```bash
        bitcoind -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password 
        ```
3. Run a localhost instance of the DBMS on the default port.
4. Clone this repository.
5. From your IntelliJ welcome screen: select "Import Project" and open the [build.sbt](https://github.com/bitbart/bitcoin-analytics-api/blob/master/build.sbt) file from your repository directory.

### Running 
1. Execute of one of the available [examples](https://github.com/bitbart/bitcoin-analytics-api/tree/master/src/main/scala/tcs/examples). Either 
    * open the file from your IDE and select run or 
    * use the command line: from the root directory of the project, execute
        ```bash
        sbt "runMain tcs.examples.ClassName"
        ```

Our framework will build the selected database. Then you can query it for performing your analysis.
For each available Scala script, we provide some default [queries along with the resulting csv files](https://github.com/bitbart/blockchain-analytics-api/tree/master/queries).
