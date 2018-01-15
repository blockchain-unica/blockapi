package tcs.custom.ethereum.ICOBenchAPIs

class ICOShortResult(
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


class ICODates(
                preIcoStart: String,
                preIcoEnd: String,
                icoStart: String,
                icoEnd: String
              )