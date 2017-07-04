# Bitcoin analytics API
A library for the development of custom analytics on Bitcoin, by [Stefano Lande](http://tcs.unica.it/members/stefano-lande) and [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu).

### Install prerequisites
1. [Bitcoin Core](https://bitcoin.org/en/bitcoin-core/)
2. [MongoDB](https://www.mongodb.com/what-is-mongodb)
3. IDE for executing a Scala SBT project (we used [IntelliJ IDEA](https://www.jetbrains.com/idea/))

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
1. From your IDE: open the file one of the available [examples](https://github.com/bitbart/bitcoin-analytics-api/tree/master/src/main/scala/tcs/examples) and
2. Select run

Our framework will build the MongoDB database that you can query for performing your analysis.
For each available example, [we provide](https://github.com/bitbart/bitcoin-analytics-api/tree/master/queries) some default MongoDB queries along with the resulting csv files.
