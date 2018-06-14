SELECT Pool.pool AS Pool, COUNT(*) AS Blocks

FROM myblockchain.btcpools AS Pool

GROUP BY Pool.pool
