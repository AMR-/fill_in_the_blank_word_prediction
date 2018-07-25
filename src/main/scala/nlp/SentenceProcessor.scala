package nlp

import scala.collection.mutable
import scala.language.postfixOps

object SentenceProcessor {

  /**
    * @param filename - get the filename for the sentences.csv files
    * @return a map with all info.  keys are the string ids.  the 3-Tuple as preceding words, following words, and
    *         a boolean to indicate if there is an inversion of affect requested.
    */
  final def getSentenceComponentsByID(filename: String): Map[String, (Array[String], Array[String], Boolean)] = {
    val sentencesByID: Map[String, (String, Boolean)] = getSentencesByID(filename)
    sentencesToComponentsByID(sentencesByID)
  }

  private def getSentencesByID(filename: String): Map[String, (String, Boolean)] = {
    val comma:Char = ','
    val bufferedSource = io.Source.fromFile(filename)
    val map = mutable.HashMap.empty[String, (String, Boolean)]
    for (line <- bufferedSource.getLines) {
      if (line.trim.length > 0) {
        if (line.count(_ == comma) != 3)
          throw new RuntimeException(s"Do not include commas in the sentence (offending line: $line)")
        val Array(id, floor, sentence, inversion) = line.split(comma).map(_.trim)
        println(s"$id $floor $sentence $inversion")
        map.put(id, (sentence, inversion == "1"))
      }
    }
    map toMap
  }

  private def sentencesToComponentsByID(sentences: Map[String, (String, Boolean)]):
  Map[String, (Array[String], Array[String], Boolean)] =
    sentences.map{ case(k,v) => (k, sentenceToComponent(v._1, v._2) ) }

  private def sentenceToComponent(sentence: String, bool: Boolean): (Array[String], Array[String], Boolean) = {
    val pre: Array[String] = sentence.split(" ").takeWhile(w => !w.contains('_'))
    val post: Array[String] = sentence.split(" ").reverse.takeWhile(w => !w.contains('_')).reverse
    (pre, post, bool)
  }

}
