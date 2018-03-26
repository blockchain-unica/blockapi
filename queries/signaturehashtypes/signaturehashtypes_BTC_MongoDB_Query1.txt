db.signatureHashTypes.aggregate([
{ 
    $group: {
    _id: "$hashType",  // group by hashType; we get the total count for each hash type
    count: { $sum: 1}
    }    
}])
