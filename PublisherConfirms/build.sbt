ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "ru.example"

ThisBuild / libraryDependencies ++= Seq(
  "com.rabbitmq" % "amqp-client"  % "5.14.0",
  "org.slf4j"    % "slf4j-api"    % "1.7.32",
  "org.slf4j"    % "slf4j-simple" % "1.7.32"
)

lazy val root = (project in file(".")).settings(name := "PublisherConfirms")
