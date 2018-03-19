# Numero di blocchi minati da ogni singolo miner
SELECT miner, COUNT(*) as tot FROM `block` GROUP BY miner
