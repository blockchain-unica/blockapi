# - Same number of transactions
SELECT CASE WHEN
(SELECT COUNT(*) FROM txtool) = (SELECT COUNT(*) FROM txexp)
THEN 'True' ELSE 'False' END AS Status;
