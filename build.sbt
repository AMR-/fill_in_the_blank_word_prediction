name := "fill_in_the_blank"

version := "1.0"

scalaVersion := "2.12.6"

scalacOptions += "-feature"

lazy val train = TaskKey[Unit]("train", "Train n-gram models using the corpera.")
lazy val predict = TaskKey[Unit]("predict", "Predict positives and negative sentences.")

fullRunTask(train, Compile, "BuildModel")
fullRunTask(predict, Compile, "Predict")
