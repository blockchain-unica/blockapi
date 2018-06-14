/*Query that counts how many blocks for each known pool*/

SELECT Pool.pool AS Pool, COUNT(*) AS Blocks

FROM myblockchainlite.ltcpools AS Pool

GROUP BY Pool.pool