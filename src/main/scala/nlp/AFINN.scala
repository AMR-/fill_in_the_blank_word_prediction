package nlp

import scala.language.postfixOps
import scala.io.Source.fromFile
import utils.Props

object AFINN {

  val resources_folder: String = Props("resources_folder") get
  val verboseLogging: Boolean = Props("verbose_logging").get.toBoolean

  val AFINN_Files: List[String] = Props("afinn_filenames") getAsStringList

  val affectDict: Map[String,Int] = AFINN_Files
    .foldRight[Map[String,Int]](scala.collection.immutable.Map.empty)((filename, map) => map ++ loadAFINN(filename))
    .withDefault(_ => 0)

  println(s"Affect Dictionary has ${affectDict.size} values.")
  if (verboseLogging) println(affectDict)

  private def loadAFINN(fileName: String) : Map[String, Int] = {
    val kvp = fromFile(resources_folder + fileName, "UTF-8").getLines
      .map(_.stripLineEnd.split("\t",-1))
      .map(fields => fields(0) -> fields(1).toInt).toList
    Map(kvp: _*)
  }

}
