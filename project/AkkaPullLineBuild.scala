import sbt._
import sbt.Keys._

object AkkaPullLineBuild extends Build {

  lazy val akkaPullLine = Project(
    id = "akka-pull-line",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "akka-pull-line",
      organization := "org.saglam",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.3",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions"),
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.3-M1",
        "com.typesafe.akka" %% "akka-slf4j" % "2.3-M1",
        "com.typesafe.akka" %% "akka-testkit" % "2.3-M1",
        "ch.qos.logback" % "logback-classic" % "1.0.13",
        "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test"
      )
    )
  )
}
