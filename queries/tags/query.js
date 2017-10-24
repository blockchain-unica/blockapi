function getdhm(id) {
        var formattedTime = id.year + "-" + id.month + '-' + id.day;
        return formattedTime;

    }

db.outWithTags.aggregate([
{ $match: {
    "tags" : "SatoshiDICE"
    }
},
{ $group : {
    _id: {	year : { $year : "$date" },        
            month : { $month : "$date" },        
            day : { $dayOfMonth : "$date" },
        },
    count: {$sum : 1},
    sum: {$sum: "$value"}
    }
}
]).forEach(function(e){
  print(getdhm(e._id)+","+e.count+","+e.sum/100000000);
});