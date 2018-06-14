/*This query counts, for each miner, the number of empty blocks and total blocks.
  Then calculates the ratio empty/total blocks and sorts the table starting with
  the miner who has got the highest ratio.
 */

SELECT t1.miner as BlockMiner,
       t1.emptyblocks as EmptyB,
       t1.totalblocks as TotalB,
       t1.emptyblocks/t1.totalblocks as Ratio FROM
  ((SELECT miner,
      COUNT(CASE WHEN txsnumber = 1 THEN 1 ELSE NULL END) AS emptyblocks,
      COUNT(CASE WHEN txsnumber > 0 THEN 1 ELSE NULL END) AS totalblocks
    FROM emptyblockanalysis
    GROUP BY miner) as t1)
ORDER BY Ratio desc;