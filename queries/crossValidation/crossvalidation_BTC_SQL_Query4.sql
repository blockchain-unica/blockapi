# - Same number of outputs
SELECT CASE WHEN
(SELECT COUNT(*) FROM txtool as t, txexp as e WHERE t.txHash=e.txHash AND t.numoutput=e.numoutput) = (SELECT COUNT(*) FROM txtool)
THEN 'True' ELSE 'False' END AS Status;