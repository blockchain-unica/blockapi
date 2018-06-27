/*This query counts for how many block we're currently unable to find out who mined it,
 year by year, evidencing a growing number of known mining pools appending data to their blocks
  */

SELECT COUNT(*) as "Unknown miner", YEAR(timestamp) as year from myblockchainlite.ltcpools where pool = "Unknown"
GROUP BY year;