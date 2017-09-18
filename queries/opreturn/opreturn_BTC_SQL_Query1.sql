SELECT protocol, count(*) as number FROM opreturn.opreturnoutput
group by protocol order by number desc;