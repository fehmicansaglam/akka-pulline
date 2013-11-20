import sbt._
import sbt.Keys._

object BuildSettings {
  val buildVersion = "0.1-SNAPSHOT"

  val filter = { (ms: Seq[(File, String)]) =>
    ms filter {
      case (file, path) =>
        path != "logback.xml" && !path.startsWith("toignore") && !path.startsWith("samples")
    }
  }

  val buildSettings = Defaults.defaultSettings ++ Seq(
    name := "Akka Pulline",
    organization := "org.akkapulline",
    version := buildVersion,
    scalaVersion := "2.10.3",
    crossScalaVersions := Seq("2.10.3"),
    crossVersion := CrossVersion.binary,
    javaOptions in test ++= Seq("-Xmx512m", "-XX:MaxPermSize=512m"),
    scalacOptions ++= Seq("-unchecked", "-deprecation" /*, "-Xlog-implicits", "-Yinfer-debug", "-Xprint:typer", "-Yinfer-debug", "-Xlog-implicits", "-Xprint:typer"*/ ),
    scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation", "-diagrams", "-implicits"),
    mappings in (Compile, packageBin) ~= filter,
    mappings in (Compile, packageSrc) ~= filter,
    mappings in (Compile, packageDoc) ~= filter) ++ Publish.settings ++ Format.settings
}

object Publish {
  object TargetRepository {
    def local: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
      val localPublishRepo = "./repository"
      if (version.trim.endsWith("SNAPSHOT"))
        Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
      else Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
    }
  }
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= TargetRepository.local,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Unlicense" -> url("http://unlicense.org/")))
}

object Format {
  import com.typesafe.sbt.SbtScalariform._

  lazy val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := formattingPreferences)

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().
      setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, false).
      setPreference(CompactControlReadability, false).
      setPreference(CompactStringConcatenation, false).
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(FormatXml, true).
      setPreference(IndentLocalDefs, false).
      setPreference(IndentPackageBlocks, true).
      setPreference(IndentSpaces, 2).
      setPreference(MultilineScaladocCommentsStartOnFirstLine, false).
      setPreference(PreserveSpaceBeforeArguments, false).
      setPreference(PreserveDanglingCloseParenthesis, false).
      setPreference(RewriteArrowSymbols, false).
      setPreference(SpaceBeforeColon, false).
      setPreference(SpaceInsideBrackets, false).
      setPreference(SpacesWithinPatternBinders, true)
  }
}

object Dependencies {
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.3-M1"
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.3-M1"
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.3-M1"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"
  val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test"
}

object AkkaPullineBuild extends Build {
  import BuildSettings._
  import Resolvers._
  import Dependencies._

  lazy val akkaPulline = Project(
    id = "akka-pulline",
    base = file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        akkaActor,
        akkaSlf4j,
        akkaTestkit,
        logback,
        scalatest
      )
    )
  )
}
