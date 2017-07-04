# Bitcoin analytics API
A library for the development of custom analytics on Bitcoin, by [Stefano Lande](http://tcs.unica.it/members/stefano-lande) and [Livio Pompianu](http://tcs.unica.it/members/livio-pompianu).

## Quick start

### Prerequisites
* A localhost instance of MongoDB on the default port
* Bitcoin Core server with RPC calls enabled. Run as
```bash
bitcoind  -datadir=path/to/blockchain -server -rpcuser=user -rpcpassword=password 
```

### Running 
To run the examples, execute:
```bash
sbt "runMain tcs.examples.ClassName"
```
