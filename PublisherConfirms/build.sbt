ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "amqp-client"  % "5.16.0",
  "org.slf4j"    % "slf4j-api"    % "2.0.5",
  "org.slf4j"    % "slf4j-simple" % "2.0.5"
)

lazy val root = (project in file(".")).settings(name := "PublisherConfirms")

assembly / assemblyMergeStrategy := {
  case m if m.toLowerCase.endsWith("manifest.mf")       => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")   => MergeStrategy.discard
  case m if m.toLowerCase.matches("version.conf")       => MergeStrategy.discard
  case "module-info.class"                              => MergeStrategy.first
  case "reference.conf"                                 => MergeStrategy.concat
  case x: String if x.contains("UnusedStubClass.class") => MergeStrategy.first
  case _                                                => MergeStrategy.first
}
