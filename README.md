# Bitcoin analytics API
A Scala library for the development of custom analytics on Bitcoin, by [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu), [Stefano Lande](http://tcs.unica.it/members/stefano-lande), and [Stefano Ferru](https://github.com/ferruvich).

The library is dicussed in [A general framework for Bitcoin analytics](https://arxiv.org/pdf/1707.01021.pdf).

More material is available in the [project page](http://blockchain.unica.it/projects/blockchain-analytics/).

The Scaladoc (uploaded on this repository in the [doc folder](https://github.com/bitbart/bitcoin-analytics-api/tree/master/docs)) is also available at the following [link](https://bitbart.github.io/bitcoin-analytics-api/).

### Install prerequisites
1. [Bitcoin Core](https://bitcoin.org/en/bitcoin-core/)
2. [MongoDB](https://www.mongodb.com/what-is-mongodb)
3. IDE for executing a Scala SBT project (we used [IntelliJ IDEA](https://www.jetbrains.com/idea/))

__or use the command line in place of an IDE__

3. Install [SBT](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html)

### Install Bitcoin analytics API
1. Execute Bitcoin Core in order to obtain a local copy of the Bitcoin blockchain (this process may take several hours)
2. Enable Bitcoin Core RPC calls:
```bash
bitcoind -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password 
```
3. Run a localhost instance of MongoDB on the default port:
```bash
mongodb
```
4. Clone the [Bitcoin analytics API](https://github.com/bitbart/bitcoin-analytics-api/) repository
5. From your IDE: select "import project" and provide your local path to the [build.sbt](https://github.com/bitbart/bitcoin-analytics-api/blob/master/build.sbt) file

### Running 
1. From your IDE: open the file one of the available [examples](https://github.com/bitbart/bitcoin-analytics-api/tree/master/src/main/scala/tcs/examples) and select run

__or use the command line in place of an IDE__
1. From the root directory of the project, execute
```bash
sbt "runMain tcs.examples.ClassName"
```

Our framework will build the MongoDB database that you can query for performing your analysis.
For each available Scala script, we provide some default [MongoDB queries along with the resulting csv files](https://github.com/bitbart/bitcoin-analytics-api/tree/master/queries).
