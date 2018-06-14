# - Ten addresses with the most dollars in output
SELECT *
FROM litecoinbalances.balanceslite as balances
ORDER BY balances.outValue DESC
LIMIT 10;