import sbt.Package.ManifestAttributes
import sbtassembly.AssemblyKeys.assemblyShadeRules

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "3.1.0"
ThisBuild / organization     := "uk.co.danielrendall"
ThisBuild / organizationName := "excel-as-a-service"

githubOwner := "danielrendall"
githubRepository := "XsltAsAService"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
releaseCrossBuild := true

lazy val root = (project in file("."))
  .settings(
    name := "excel-as-a-service",
    libraryDependencies ++= Seq(
      "uk.co.danielrendall" %% "services-as-a-service-interfaces" % "0.0.1-SNAPSHOT",
      "org.nanohttpd" % "nanohttpd" % "2.3.1" % Provided
    ),
    packageOptions := Seq(ManifestAttributes(
      ("Serviceable-Class", "uk.co.danielrendall.saas.excel.ExcelService"))),
    assemblyShadeRules := Seq(
      ShadeRule.zap("scala.**").inAll
    )
  )
