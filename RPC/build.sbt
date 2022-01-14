import sbtassembly.AssemblyPlugin.autoImport.assembly

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "amqp-client"  % "5.14.0",
  "org.slf4j"    % "slf4j-api"    % "1.7.32",
  "org.slf4j"    % "slf4j-simple" % "1.7.32"
)

lazy val producer = (project in file("server"))
  .settings(assembly / mainClass := Some("ru.example.rmq.rpc.Server"))
  .settings(assembly / assemblyJarName := "server.jar")

lazy val consumer = (project in file("client"))
  .settings(assembly / mainClass := Some("ru.example.rmq.rpc.Client"))
  .settings(assembly / assemblyJarName := "client.jar")
