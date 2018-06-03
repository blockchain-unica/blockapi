SELECT t.country, count(distinct t.country)
FROM db.transaction as t
group by t.country