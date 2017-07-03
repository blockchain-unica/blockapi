# bitcoin-analytics-api

## Quick start

### Prerequisites
* an localhost instance of MongoDB on the default port
* Bitcoin Core server with RPC calls enabled. Run as
```bash
bitcoind  -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password 
```

### Running 
To run the examples, you need:
```bash
sbt "runMain tcs.examples.ClassName"
```
