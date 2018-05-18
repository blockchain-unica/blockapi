select array_to_json(array_agg(row_to_json(t)))
from (
  	select * from balances order by sent desc limit 10
) t
