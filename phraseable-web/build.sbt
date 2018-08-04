name := "phraseable-web"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "com.h2database" % "h2" % "1.4.178",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.apache.commons" % "commons-email" % "1.3.2",
  "org.apache.commons" % "commons-csv" % "1.4",
  "commons-codec" % "commons-codec" % "1.10",
  "com.atlassian.commonmark" % "commonmark" % "0.8.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

javaOptions in Test += "-Dconfig.file=conf/test/application.conf"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
