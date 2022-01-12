import sbtassembly.AssemblyPlugin.autoImport.assembly

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "amqp-client"  % "5.14.0",
  "org.slf4j"    % "slf4j-api"    % "1.7.32",
  "org.slf4j"    % "slf4j-simple" % "1.7.32"
)

lazy val producer = (project in file("producer"))
  .settings(assembly / mainClass := Some("ru.example.rmq.helloworld.Producer"))
  .settings(assembly / assemblyJarName := "producer.jar")

lazy val consumer = (project in file("consumer"))
  .settings(assembly / mainClass := Some("ru.example.rmq.helloworld.Consumer"))
  .settings(assembly / assemblyJarName := "consumer.jar")
