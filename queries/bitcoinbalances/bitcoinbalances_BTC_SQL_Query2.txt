# - Ten addresses with the most dollars in output
SELECT *
FROM bitcoinBalances.balances as balances
ORDER BY balances.outValue DESC
LIMIT 10;