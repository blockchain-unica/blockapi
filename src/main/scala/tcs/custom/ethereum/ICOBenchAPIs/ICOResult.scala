package tcs.custom.ethereum.ICOBenchAPIs

import java.time.LocalDateTime

case class ICOResult(
                    id: Int,
                    name: String,
                    url: String,
                    logo: String,
                    desc: String,
                    rating: Float,
                    premium: Int,
                    raised: Int,
                    dates: ICODates
                    )



case class ICODates(
                     preIcoStart: String,
                     preIcoEnd: String,
                     icoStart: String,
                     icoEnd: String
                   )