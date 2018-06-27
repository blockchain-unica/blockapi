/**This query shows how many op_return contain SegWit commitment in header**/

SELECT isSegwit as "Signaling Segwit?", Count(*) as TxNumber FROM opreturn.opreturnoutputlite
GROUP by isSegwit;