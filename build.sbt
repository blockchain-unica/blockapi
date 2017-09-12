name := "BlockchainDLib"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "org.web3j" % "core" % "2.3.0"
libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"
libraryDependencies += "com.gilt" % "jerkson_2.11" % "0.6.9"
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

// https://mvnrepository.com/artifact/org.bitcoinj/bitcoinj-core
libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.14.4"

// https://mvnrepository.com/artifact/com.github.briandilley.jsonrpc4j/jsonrpc4j
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5"

// https://mvnrepository.com/artifact/com.codesnippets4all/quick-json
libraryDependencies += "com.codesnippets4all" % "quick-json" % "1.0.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "3.0.2",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "3.0.2"   % "test",
  "com.h2database"  %  "h2"                % "1.4.196"
//  "ch.qos.logback"  %  "logback-classic"   % "1.2.3"
)

// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25" % "compile"
