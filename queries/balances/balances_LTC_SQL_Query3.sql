# - Ten addresses with the highest total balance
SELECT *
FROM litecoinbalances.balanceslite as balances
ORDER BY balances.totalvalue DESC
LIMIT 10;