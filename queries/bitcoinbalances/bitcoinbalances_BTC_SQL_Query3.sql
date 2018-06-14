# - Ten addresses with the highest total balance
SELECT *
FROM bitcoinBalances.balances as balances
ORDER BY balances.totalvalue DESC
LIMIT 10;