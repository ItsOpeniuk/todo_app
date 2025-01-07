name := """play-java-seed"""
organization := "com.todolist"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "Maven Central" at "https://repo1.maven.org/maven2"

libraryDependencies ++= Seq(
  guice, // Dependency Injection
  "org.mongodb" % "mongodb-driver-sync" % "4.9.0", // MongoDB Java Driver
  "com.auth0" % "java-jwt" % "4.2.1", // JWT authentication for secure APIs
  "org.mockito" % "mockito-core" % "5.5.0" % Test, // Testing framework
  "com.typesafe.play" %% "play-json" % "2.9.2", // Play JSON library
  "junit" % "junit" % "4.13.2" % Test, // JUnit 4
  "com.novocode" % "junit-interface" % "0.11" % Test, // JUnit Interface
  "com.typesafe.play" %% "play-test" % "2.9.2" % Test, // Play Testing Library
  "org.apache.pdfbox" % "pdfbox" % "2.0.29"
)





