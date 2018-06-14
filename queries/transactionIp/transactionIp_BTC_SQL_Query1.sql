SELECT t.ipRelayedBy, 
  count(t.ipRelayedBy)
FROM transaction as t
group by t.ipRelayedBy
