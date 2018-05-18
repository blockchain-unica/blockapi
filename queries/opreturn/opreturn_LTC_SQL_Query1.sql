/**/
SELECT protocol, count(*) as number FROM opreturn.opreturnoutputlite
group by protocol order by number desc;