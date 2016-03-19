name := "phraseable-web"

version := "0.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalastyleConfig := baseDirectory.value / "etc" / "scalastyle" / "scalastyle-config.xml"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "com.h2database" % "h2" % "1.4.178",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

lazy val main = (project in file(".")).enablePlugins(PlayScala)
