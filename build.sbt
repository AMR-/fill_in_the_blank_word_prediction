name := "fill_in_the_blank"

version := "1.0"

scalaVersion := "2.12.6"

scalacOptions += "-feature"

//libraryDependencies += "com.github.benhutchison" %% "prickle" % "1.1.13"

//libraryDependencies ++= Seq(
//  "com.github.benhutchison" %% "prickle" % "1.1.14"
//)

//  "org.scala-lang" %% "scala-library" % "2.12.6",

//prickle_2.12-1.1.14.jar

// "de.sciss" %% "serial" % "1.1.1"
//  "org.scalaz" %% "scalaz-core" % "7.2.25",
//  "org.scala-lang.modules" %% "scala-pickling" % "0.11.0-M2"
//  "org.scala-lang.modules" %% "scala-pickling_2.11" % "0.11.0-M2"

lazy val train = TaskKey[Unit]("train", "Train n-gram models using the corpera.")
lazy val predict = TaskKey[Unit]("predict", "Predict positives and negative sentences.")

fullRunTask(train, Compile, "BuildModel")
fullRunTask(predict, Compile, "Predict")
