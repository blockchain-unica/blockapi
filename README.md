# BlockAPI: Blockchain analytics API
BlockAPI is a general-purpose blockchain analytics Scala API, maintained by [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu) and [Stefano Lande](http://tcs.unica.it/members/stefano-lande) of the [Blockchain@Unica Lab](http://blockchain.unica.it) at the University of Cagliari, under the supervision of [Massimo Bartoletti](https://tcs.unica.it/members/bart).

The standard workflow of BlockAPI consists of two steps: 
1. construct a view of the blockchain (possibly, including external data) and save it in a database;
2. analyse the view by using the query language of the database.

BlockAPI currently supports Bitcoin and  Ethereum, and the DBMS MongoDB, MySQL, PostgreSQL, and Fuseki.

### Documentation ###

* The architecture of BlockAPI and the experimental evaluation of its performance are dicussed in the paper [A general framework for blockchain analytics](https://www.researchgate.net/publication/321415812_A_general_framework_for_blockchain_analytics),
presented at [SERIAL 2017](https://serial17.ibr.cs.tu-bs.de/).
* Some common use cases are documented in the [project web page](http://blockchain.unica.it/projects/blockchain-analytics/analytics.html).
* The usage of the APIs is documented in the [Scaladoc](https://github.com/bitbart/blockchain-analytics-api/tree/master/docs).
* The installation instructions are detailed below in this document.

### Prerequisites

#### General prerequisites
BlockAPI requires [Java JDK 1.8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html), and it supports the following blockchain clients and DBMSs:

1. Blockchain clients:
    * [Bitcoin Core](https://bitcoin.org/en/bitcoin-core/) (Tested with version v0.19.0.1: extracts data from Bitcoin)
    * [Parity](https://www.parity.io/ethereum/) (Tested with version v2.6.8: extracts data from Ethereum)
2. DBMS:
    * [MongoDB](https://www.mongodb.com/what-is-mongodb) (Tested with version v4.2: constructs a NoSQL view of the data)
    * [MySQL](https://www.mysql.com/) (Tested with version v5.7: constructs a SQL view of the data)
    * [PostgreSQL](https://www.postgresql.org/) (Tested with version v11: constructs a SQL view of the data)
    * [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/index.html) (Tested with version v3.14.0: constructs a RDF view of the data)

BlockAPI may be used either as an IDE for executing a Scala SBT projects (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), or
via command line (in this case, you need to install [SBT](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html).

Further prerequisites may depend on the specific analyses one wants to implement.

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

#### Analysis-specific prerequisites 

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

### Installation
1. Execute the blockchain client in order to obtain a local copy of the target blockchain (this process may take several hours)
2. Enable the client RPC calls.
    * In the Bitcoin Core case
        ```bash
        bitcoind -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password -rpcserialversion=0
        ```
3. Run a localhost instance of the DBMS on the default port.
4. Clone this repository.
5. From your IntelliJ welcome screen: select "Import Project" and open the [build.sbt](https://github.com/bitbart/bitcoin-analytics-api/blob/master/build.sbt) file from your repository directory.

### Execution 

You can test BlockAPI through one of the [use cases](https://github.com/blockchain-unica/blockapi/tree/master/src/main/scala/it/unica/blockchain/analyses) in the repository. 
To do this, either: 
* open the file from your IDE and select run or 
* via command line: from the root directory of the project, execute
        ```bash
        sbt "runMain it.unica.blockchain.analyses.ClassName"
        ```
        
Once BlockAPI has built the database, you can query it using one of the available
[queries](https://github.com/bitbart/blockchain-analytics-api/tree/master/queries).

### Acknowledgments
The project has been supported by [several developers](Acknowledgments.md) at the Department of Mathematics and Computer Science of the University of Cagliari, Italy. 
