organization := "datadog-scala"

name := "datadog-scala"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4")

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies += "io.spray" % "spray-can" % "1.3.1"

libraryDependencies += "io.spray" % "spray-http" % "1.3.1"

libraryDependencies += "io.spray" % "spray-httpx" % "1.3.1"

libraryDependencies += "io.spray" % "spray-util" % "1.3.1"

libraryDependencies += "io.spray" % "spray-can" % "1.3.1"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.10"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.2"

libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.2"

libraryDependencies += "org.specs2" %% "specs2" % "2.4.5" % "test"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.6" % "test"

publishTo := Some(Resolver.file("file",  new File( "/Users/gphat/src/mvn-repo/releases" )) )
