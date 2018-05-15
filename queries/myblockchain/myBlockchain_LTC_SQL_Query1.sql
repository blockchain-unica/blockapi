SELECT year(t.timestamp), month(t.timestamp), day(t.timestamp), 
  count(distinct i.id)/count(distinct t.txid), count(distinct o.id)/count(distinct t.txid)
FROM myblockchainlite.transaction as t
  join myblockchainlite.input as i on t.txid=i.id
    join myblockchainlite.output as o on o.id = i.id
group by year(t.timestamp),month(t.timestamp),day(t.timestamp)
order by year(t.timestamp),month(t.timestamp),day(t.timestamp) asc;