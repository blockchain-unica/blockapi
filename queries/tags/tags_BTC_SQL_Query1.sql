SELECT txdate, count(*) as number FROM outwithtags.tagsoutputs
where tag='SatoshiDICE' group by txdate order by txdate;