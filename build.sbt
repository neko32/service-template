import sbt._
import Keys._

name := "service-template"

version := "0.1"

organization := "tanuki"
scalaVersion := "2.13.6"

mainClass := Some("org.tanuneko.core.App")

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-Ypartial-unification",
    "utf8"
  )
)

lazy val root = Project(id = "service-template",
  base = file("."))
  .settings(settings)
  .aggregate(binLookup)

lazy val binLookup = Project(id = "binLookup",
  base = file("BinLookup"))
  .settings(
    settings,
    coverageMinimumStmtTotal := 70,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    libraryDependencies ++= commonlibraryDependencies
  )


lazy val commonlibraryDependencies = {
  val catsEffectVersion = "2.5.3"
  val catsCoreVersion = "2.3.0"
  val circeVersion = "0.14.1"
  val scalaCheckVersion = "1.15.4"
  val akkaVersion = "2.6.16"
  val akkahttpVersion = "10.2.6"
  val slf4JVersion = "1.7.32"
  val logbackVersion = "1.2.5"
  Seq(
    "org.typelevel" %% "cats-core" % catsCoreVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.typesafe" % "config" % "1.4.1",
    "org.slf4j" % "slf4j-api" % slf4JVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "net.logstash.logback" % "logstash-logback-encoder" % "6.6",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "com.typesafe.akka" %% "akka-http" % akkahttpVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkahttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkahttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    // "io.circe" %% "circe-generic" % http4sVersion,
    // "io.circe" %% "circe-core" % circeVersion,
    // "io.circe" %% "circe-generic" % circeVersion,
    // "io.circe" %% "circe-parser" % circeVersion,
    //
    // libs for test
    "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkahttpVersion % Test,
    "com.github.tomakehurst" % "wiremock-jre8" % "2.31.0" % Test
  )
}


lazy val assemblySettings = Seq(
  assemblyJarName / assembly := file(name.value + ".jar"),
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
