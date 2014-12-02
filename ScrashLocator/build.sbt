name := "ScrashLocator"

version := "1.0"

scalaVersion := "2.11.4"

resolvers += "Maven Repository for Spoon Snapshot" at "http://spoon.gforge.inria.fr/repositories/snapshots/"

libraryDependencies ++=Seq(
  "net.liftweb" % "lift-json_2.11" % "2.6-M4",
  "fr.inria.gforge.spoon" % "spoon-core" % "IAGL-SNAPSHOT"
)