# Numero di blocchi minati da ogni singolo miner
SELECT id, miner, COUNT(*) as tot FROM `block` GROUP BY miner ORDER BY id ASC
