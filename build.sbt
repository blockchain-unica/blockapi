name := "BlockchainDLib"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.mavenLocal

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "org.web3j" % "core" % "2.3.0"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.1"
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.15-SNAPSHOT"

// https://mvnrepository.com/artifact/com.github.briandilley.jsonrpc4j/jsonrpc4j
libraryDependencies += "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1"

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5"

// https://mvnrepository.com/artifact/com.codesnippets4all/quick-json
libraryDependencies += "com.codesnippets4all" % "quick-json" % "1.0.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.0.2",

  "mysql" % "mysql-connector-java" % "6.0.6",

  "com.zaxxer" % "HikariCP" % "2.7.1"
)

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"


