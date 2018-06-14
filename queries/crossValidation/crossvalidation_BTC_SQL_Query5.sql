# - Same value of output
SELECT CASE WHEN
(SELECT COUNT(*) FROM txtool as t, txexp as e WHERE t.txHash=e.txHash AND t.outputsum=e.outputsum) = (SELECT COUNT(*) FROM txtool)
THEN 'True' ELSE 'False' END AS Status;