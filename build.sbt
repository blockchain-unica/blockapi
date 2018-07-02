name := "BlockchainDLib"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.mavenLocal

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "net.liftweb" %% "lift-json" % "3.0.1"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0"

libraryDependencies += "org.web3j" % "core" % "3.2.0"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.1"
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.14.5"

// https://mvnrepository.com/artifact/com.github.briandilley.jsonrpc4j/jsonrpc4j
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
// https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.6.0"
//libraryDependencies += "org.mongodb.scala" %% "mongo-scala-bson" % "2.1.0"

libraryDependencies += "org.apache.tika" % "tika-server" % "1.5"

libraryDependencies += "com.j256.simplemagic" % "simplemagic" % "1.1"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5"

// https://mvnrepository.com/artifact/com.codesnippets4all/quick-json
libraryDependencies += "com.codesnippets4all" % "quick-json" % "1.0.4"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.0.2",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.postgresql" % "postgresql" % "42.2.0",
  "com.zaxxer" % "HikariCP" % "2.7.1"
)

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.7.0"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"

libraryDependencies += "org.litecoinj" % "litecoinj-core" % "0.15-SNAPSHOT"
