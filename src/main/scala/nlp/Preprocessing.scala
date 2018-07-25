package nlp

import java.io.File
import utils.FileUtils.asString
import scala.language.postfixOps

object Preprocessing {

  /**
    * @param file a file of text
    * @return return a string, where each line is it's own sanitized sentence
    */
  final def preprocess(file : File) : String = {
    println(s"Processing the file ${file getName}.")
    sentence_per_line(
      strip_character_names(
        remove_b_tags(
          asString(file)
        )
      )
    )
  }

  /**
    * @param string the output of def preprocess
    * @return a list of sentences, where each sentence is a list of words in that sentence
    */
  final def tokenize(string : String) : List[List[String]] = {
    string.split("\n").toList map(sentence => sentence split " " toList)
  }

  private def remove_b_tags(string: String) : String = {
    val pattern = "(?s)<b>.*</b>"
    string.replaceAll(pattern, "")
  }

  private def strip_character_names(string : String) : String = {
    println("Assuming that it is a movie script, removing bold tags and stripping character names.")
    // The idea is that it must contain at least one lowercase alpha
    val pattern = ".*[a-z].*"
    val lines : List[String] = string.split('\n').toList.filter(l => l.matches(pattern))
    lines.mkString("\n")
  }

  private def sentence_per_line(string : String) : String = {
    string.replaceAll("\n","").replaceAll("""[\p{Z}\s]{1,}""", " ").replaceAll("[.!?]{1,}","\n")
      .toLowerCase.replaceAll("[^ a-z0-9\n]","").replaceAll(" {1,}", " ")
      .replaceAll("(?m)^ {1,}", "").replaceAll("(?m) {1,}$", "")
  }

}
