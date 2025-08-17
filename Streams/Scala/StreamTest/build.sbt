import sbtassembly.AssemblyPlugin.autoImport.assembly

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

lazy val configVersion = "1.4.2"
lazy val csvVersion    = "1.10.0"
lazy val circeVersion  = "0.14.5"
lazy val streamVersion = "0.9.0"
lazy val slf4jVersion  = "2.0.5"

ThisBuild / libraryDependencies ++= Seq(
  "com.typesafe"       % "config"        % configVersion,
  "org.apache.commons" % "commons-csv"   % csvVersion,
  "io.circe"          %% "circe-core"    % circeVersion,
  "io.circe"          %% "circe-generic" % circeVersion,
  "io.circe"          %% "circe-parser"  % circeVersion,
  "com.rabbitmq"       % "stream-client" % streamVersion,
  "org.slf4j"          % "slf4j-api"     % slf4jVersion,
  "org.slf4j"          % "slf4j-simple"  % slf4jVersion
)

lazy val producer = (project in file("producer"))
  .settings(assembly / mainClass := Some("ru.example.rmq.streamtest.Producer"))
  .settings(assembly / assemblyJarName := "producer.jar")
  .settings(assembly / assemblyMergeStrategy := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("meta-inf")    => MergeStrategy.discard
    case "module-info.class"                        => MergeStrategy.first
    case _                                          => MergeStrategy.first
  })

lazy val consumer = (project in file("consumer"))
  .settings(assembly / mainClass := Some("ru.example.rmq.streamtest.Consumer"))
  .settings(assembly / assemblyJarName := "consumer.jar")
  .settings(assembly / assemblyMergeStrategy := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("meta-inf")    => MergeStrategy.discard
    case "module-info.class"                        => MergeStrategy.first
    case _                                          => MergeStrategy.first
  })

assembly / assemblyMergeStrategy := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.endsWith("meta-inf")    => MergeStrategy.discard
  case "module-info.class"                        => MergeStrategy.first
  case _                                          => MergeStrategy.first
}
