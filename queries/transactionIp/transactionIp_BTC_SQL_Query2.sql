SELECT t.country, count(t.country)
FROM transaction as t
group by t.country
