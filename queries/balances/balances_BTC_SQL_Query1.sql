# - Ten addresses with the most dollars in input
SELECT *
FROM bitcoinBalances.balances as balances
ORDER BY balances.inValue DESC
LIMIT 10;