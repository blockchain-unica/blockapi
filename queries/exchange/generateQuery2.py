n_ranges = 10
max_value = 3000

for i in xrange(n_ranges):
	min = max_value/n_ranges * i
	max = max_value/n_ranges * (i+1)
	print '{ $cond: [{$and:[ {$gte:["$rate", ' + str(min) + ' ]}, {$lt: ["$rate", ' + str(max) + ']}]}, "' + str(min) + "-" + str(max) + '", ""] },'


