select array_to_json(array_agg(row_to_json(t)))
from (
  	select mining_pool, count(*) as num_blocks
	from pools
	group by mining_pool
	order by num_blocks desc
) t