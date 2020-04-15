# BlockAPI: Blockchain analytics API
A Scala framework for the development of general-purpose analytics on blockchains, maintained by [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu) and [Stefano Lande](http://tcs.unica.it/members/stefano-lande) of the [Blockchain@Unica Lab](http://blockchain.unica.it) at the University of Cagliari.

The framework allows to combine data *within* the blockchain 
with data from the *outside* (e.g. exchange rates and tags). 
The workflow consists in two steps: 
1. construct a view of the blockchain and save it in a database;
2. analyse the view by using the query language of the database.

The blockchains currently supported are Bitcoin and  Ethereum.
The DBMS currently supported are MongoDB, MySQL, PostgreSQL, and Fuseki.

The library is dicussed in [A general framework for blockchain analytics](https://www.researchgate.net/publication/321415812_A_general_framework_for_blockchain_analytics),
in proceeding of the [SERIAL workshop 2017](https://serial17.ibr.cs.tu-bs.de/).

More material is available in the [project page](http://blockchain.unica.it/projects/blockchain-analytics/analytics.html).

The Scaladoc is uploaded on this repository in the [doc folder](https://github.com/bitbart/blockchain-analytics-api/tree/master/docs).

### Install prerequisites
#### General prerequisites
At the moment, [Java JDK 1.8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) is needed to run the API.

Prerequisites depends on the target analyses.
Generally speaking the framework needs at least one blockchain client and one DBMS.

1. Blockchain clients:
    * [Bitcoin Core](https://bitcoin.org/en/bitcoin-core/) (Tested with version v0.19.0.1: extracts data from Bitcoin)
    * [Parity](https://www.parity.io/ethereum/) (Tested with version v2.6.8: extracts data from Ethereum)
2. DBMS:
    * [MongoDB](https://www.mongodb.com/what-is-mongodb) (Tested with version v4.2: constructs a NoSQL view of the data)
    * [MySQL](https://www.mysql.com/) (Tested with version v5.7: constructs a SQL view of the data)
    * [PostgreSQL](https://www.postgresql.org/) (Tested with version v11: constructs a SQL view of the data)
    * [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/index.html) (Tested with version v3.14.0: constructs a RDF view of the data)
3. Either
    * use an IDE for executing a Scala SBT project (we used [IntelliJ IDEA](https://www.jetbrains.com/idea/)) or
    * use the command line in place of an IDE (Install [SBT](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html))

#### Required libraries
This step is required to sucessfully compile the project.
Before building the project, execute the following commands:

##### Bitcoinj
```bash
git clone https://github.com/bitbart/bitcoinj.git
cd bitcoinj
git checkout segwit
mvn install -DskipTests
cd core
mvn install -DskipTests
```

#### Analysis dependent prerequisites 

##### Bitcoin

This instructions are needed for performing analyses on CrossValidation on Bitcoin.

In order to use some analyses on Bitcoin you must require the respective API key:
1. [Blockchain.info](https://api.blockchain.info/customer/signup) 
    * It is sufficient to sign up and then active your [APIKey](https://exchange.blockchain.com/api/#introduction)

After doing this, do the following:
Copy `Blockchain` key into `bitcoin\CrossValidationBitcoin.apiKey` attribute

##### Ethereum

This instructions are needed for performing analyses on CrossValidation on Ethereum.

In order to use some analyses on Bitcoin you must require the respective API key:
1. [EtherScan](https://etherscan.io) 
    * It is sufficient to sign up and then go [here](https://etherscan.io/myapikey)

After doing these steps, do the following:
Copy `EtherScan` key into `utils\Etherscan.apiKey` attribute

### Install blockchain analytics API
1. Execute the blockchain client in order to obtain a local copy of the target blockchain (this process may take several hours)
2. Enable the client RPC calls.
    * In the Bitcoin Core case
        ```bash
        bitcoind -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password -rpcserialversion=0
        ```
3. Run a localhost instance of the DBMS on the default port.
4. Clone this repository.
5. From your IntelliJ welcome screen: select "Import Project" and open the [build.sbt](https://github.com/bitbart/bitcoin-analytics-api/blob/master/build.sbt) file from your repository directory.

### Running 
1. Execute of one of the available [examples](https://github.com/blockchain-unica/blockapi/tree/master/src/main/scala/it/unica/blockchain/analyses). Either 
    * open the file from your IDE and select run or 
    * use the command line: from the root directory of the project, execute
        ```bash
        sbt "runMain it.unica.blockchain.analyses.ClassName"
        ```

Our framework will build the selected database. Then you can query it for performing your analysis.
For each available Scala script, we provide some default [queries along with the resulting csv files](https://github.com/bitbart/blockchain-analytics-api/tree/master/queries).

### Acknowledgments
The authors thank the [following developers](Acknowledgments.md) of the Department of Mathematics and Computer Science of the University of Cagliari for their valuable contributions. 
