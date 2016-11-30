name := "IRSystem"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
"com.typesafe.akka" % "akka-actor_2.11" % "2.4.14",
"org.jsoup" % "jsoup" % "1.8.3",
"commons-validator" % "commons-validator" % "1.4.0",
"org.slf4j" % "slf4j-simple" % "1.7.21",
"edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
"edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
"com.microsoft.sqlserver" % "mssql-jdbc" % "6.1.0.jre8"
)