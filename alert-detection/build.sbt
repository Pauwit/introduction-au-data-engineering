ThisBuild / scalaVersion := "2.12.18"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "alert-detection",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.5.1",
      "org.apache.spark" %% "spark-sql" % "3.5.1",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    Test / fork := true,
    Test / javaOptions ++= Seq(
      "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens", "java.base/java.nio=ALL-UNNAMED"
    ),
    run / fork := true,
    run / javaOptions ++= Seq(
      "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens", "java.base/java.nio=ALL-UNNAMED"
    )
  )
