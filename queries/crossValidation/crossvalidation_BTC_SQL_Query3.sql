# - Same number of inputs
SELECT CASE WHEN
(SELECT COUNT(*) FROM txtool as t, txexp as e WHERE t.txHash=e.txHash AND t.numinput=e.numinput) = (SELECT COUNT(*) FROM txtool)
THEN 'True' ELSE 'False' END AS Status;
