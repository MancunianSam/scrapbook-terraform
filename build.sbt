import scala.sys.process._

organization := "dev.sampalmer"
name := "scrapbook-terraform"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.hashicorp" % "cdktf" % "0.10.1",
      "com.hashicorp" % "cdktf-provider-aws" % "5.0.29",
      "software.constructs" % "constructs" % "10.0.25",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1"
    ),
  )
