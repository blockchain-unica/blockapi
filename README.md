# Blockchain analytics API
A Scala framework for the development of general-purpose analytics on blockchains, by [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu), [Stefano Lande](http://tcs.unica.it/members/stefano-lande), and [Stefano Ferru](https://github.com/ferruvich).

Our framework allows to combine data *within* the blockchain 
with data from the *outside* (e.g. exchange rates and tags). 
The workflow consists in two steps: 
1. construct a view of the blockchain and save it in a database;
2. analyse the view by using the query language of the database.

The blockchains currently supported are Bitcoin and Ethereum.
The DBMS currently supported are MongoDB and MySQL.

The library is dicussed in [A general framework for Bitcoin analytics](https://arxiv.org/pdf/1707.01021.pdf) (an updated version is coming soon).

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
    * [MySQL](https://www.mysql.com/) (constructs a SQL view of of the data)
3. Either
    * use an IDE for executing a Scala SBT project (we used [IntelliJ IDEA](https://www.jetbrains.com/idea/)) or
    * use the command line in place of an IDE (Install [SBT](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html))

### Install blockchain analytics API
1. Execute the blockchain client in order to obtain a local copy of the target blockchain (this process may take several hours)
2. Enable the client RPC calls.
    * In the Bitcoin Core case
    ```bash
    bitcoind -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password 
    ```
    * In th Parity case
    ```bash
    
    ```
3. Run a localhost instance of the DBMS on the default port.
4. Clone the [Blockchain analytics API](https://github.com/bitbart/blockchain-analytics-api/) repository
5. From your IDE: select "import project" and provide your local path to the [build.sbt](https://github.com/bitbart/bitcoin-analytics-api/blob/master/build.sbt) file

### Running 
1. From your IDE: open the file one of the available [examples](https://github.com/bitbart/bitcoin-analytics-api/tree/master/src/main/scala/tcs/examples) and select run

__or use the command line in place of an IDE__
1. From the root directory of the project, execute
```bash
sbt "runMain tcs.examples.ClassName"
```

Our framework will build the database that you can query for performing your analysis.
For each available Scala script, we provide some default [queries along with the resulting csv files](https://github.com/bitbart/blockchain-analytics-api/tree/master/queries).
