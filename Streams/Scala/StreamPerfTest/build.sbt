import sbtassembly.AssemblyPlugin.autoImport.assembly

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

lazy val streamVersion = "0.9.0"
lazy val slf4jVersion  = "2.0.5"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "stream-client" % streamVersion,
  "org.slf4j"    % "slf4j-api"     % slf4jVersion,
  "org.slf4j"    % "slf4j-simple"  % slf4jVersion
)

lazy val root = (project in file(".")).settings(name := "StreamPerfTest")

assembly / assemblyMergeStrategy := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.endsWith("meta-inf")    => MergeStrategy.discard
  case "module-info.class"                        => MergeStrategy.first
  case _                                          => MergeStrategy.first
}
