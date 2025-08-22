ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "ru.vzaigrin"

lazy val slf4jVersion  = "2.0.17"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "stream-client" % "1.2.0",
  "org.slf4j"    % "slf4j-api"     % slf4jVersion,
  "org.slf4j"    % "slf4j-simple"  % slf4jVersion
)

lazy val producer = (project in file("Producer"))
  .settings(assembly / mainClass := Some("ru.vzaigrin.rmq.streams.console.Producer"))
  .settings(assembly / assemblyJarName := "producer.jar")
  .settings(assembly / assemblyMergeStrategy := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("meta-inf")    => MergeStrategy.discard
    case "module-info.class"                        => MergeStrategy.first
    case _                                          => MergeStrategy.first
  })

lazy val consumer = (project in file("Consumer"))
  .settings(assembly / mainClass := Some("ru.vzaigrin.rmq.streams.console.Consumer"))
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
