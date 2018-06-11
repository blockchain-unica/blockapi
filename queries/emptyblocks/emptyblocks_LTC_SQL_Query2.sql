/*This query shows clearly how many empty blocks were mined by each miner confronted with total blocks mined by him
on a chosen Blockchain slice*/

SELECT miner,
  COUNT(CASE WHEN txsnumber = 1 THEN 1 ELSE NULL END) AS emptyblocks,
  COUNT(CASE WHEN txsnumber > 0 THEN 1 ELSE NULL END) AS totalblocks
FROM emptyblockanalysis
GROUP BY miner;