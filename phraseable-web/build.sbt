name := "phraseable-web"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "com.h2database" % "h2" % "1.4.178",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "commons-codec" % "commons-codec" % "1.10",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

lazy val main = (project in file(".")).enablePlugins(PlayScala)
