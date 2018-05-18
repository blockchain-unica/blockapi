# Conto quanti blocchi vuoti sono stati minati ogni giorno
SELECT DATE(timestamp) as timestamp, COUNT(*) as tot FROM `block` GROUP BY DATE(timestamp) ORDER BY timestamp ASC
