SELECT t.ipRelayedBy, 
  count(distinct t.ipRelayedBy)
FROM db.transaction as t
group by t.ipRelayedBy