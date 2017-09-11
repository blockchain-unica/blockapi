package tcs.pojos

/**
  * Created by Ferruvich on 03/08/2017.
  */
case class TraceBlockHttpResponse(
                              jsonrpc: String,
                              result: List[BlockTrace],
                              id: Int
                            )
