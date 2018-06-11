/*Shows how many empty blocks (with only Coinbase transaction) are mined by each miner in a chosen Blockchain slice*/

SELECT miner, COUNT(*) as emptyblocks FROM `emptyblockanalysis` where txsnumber = 1
GROUP BY miner
ORDER BY emptyblocks DESC;