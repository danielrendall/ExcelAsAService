import sbt.Package.ManifestAttributes
import sbtassembly.AssemblyKeys.assemblyShadeRules

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / organization     := "uk.co.danielrendall"
ThisBuild / organizationName := "excel-as-a-service"

ThisBuild / githubOwner := "danielrendall"
ThisBuild / githubRepository := "ExcelAsAService"
ThisBuild / githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

lazy val dispatchVersion = "1.2.0"

lazy val schema = (project in file("schema"))
  .enablePlugins(ScalaxbPlugin)
  .settings(
    name := "excel-as-a-service-schema",
    scalaVersion := "2.13.7",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
    ),
    Compile / scalaxb / scalaxbPackageNames := Map(
      new java.net.URI("http://www.danielrendall.co.uk/saas/excel/v1") ->  "uk.co.danielrendall.saas.excel.generated.v1"),
    Compile / scalaxb / scalaxbClassPrefix := Some("Raw"),
    Compile / scalaxb / scalaxbGenerateDispatchClient := false,
    Compile / scalaxb / scalaxbGenerateGigahorseClient := false
  )

lazy val plugin = (project in file("plugin"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "excel-as-a-service",
    scalaVersion := "3.1.0",
      libraryDependencies ++= Seq(
      "uk.co.danielrendall" %% "services-as-a-service-interfaces" % "0.0.1",
      "org.scala-lang.modules" % "scala-xml_2.13" % "1.3.0",
      "org.apache.poi" % "poi-ooxml" % "5.1.0", // This brings in loads of dependencies which is slightly annoying...
      "org.nanohttpd" % "nanohttpd" % "2.3.1" % Provided
    ),
    packageOptions := Seq(ManifestAttributes(
      ("Serviceable-Class", "uk.co.danielrendall.saas.excel.ExcelService"))),
    assemblyShadeRules := Seq(
      ShadeRule.zap("scala.**").inLibrary("org.scala-lang" % "scala3-library" % "3.1.0")
    ),
    assemblyMergeStrategy := {
      case "META-INF/versions/9/module-info.class" => MergeStrategy.rename
      case x =>
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  ).dependsOn(schema)

lazy val root = (project in file("."))
  .dependsOn(schema, plugin)
  .aggregate(schema, plugin)
