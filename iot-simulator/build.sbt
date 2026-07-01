ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "iot-simulator",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.1",
      "io.circe" %% "circe-core" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
