package utils

import scala.collection.SortedMap

object SentenceUtils {

  final def firstTwo(words: Array[String], empty: String = ""): (String, String) = {
    words.length match {
      case 0 => (empty, empty)
      case 1 => (words.head, empty)
      case _ => (words.head, words.tail.head)
    }
  }

  final def lastTwo(words: Array[String], empty: String = ""): (String, String) = {
    words.length match {
      case 0 => (empty, empty)
      case 1 => (empty, words.head)
      case _ => (words.takeRight(2).head, words.last)
    }
  }

  // toString
  final def str(arr: Array[String]) : String = arr mkString " "

  final def str[A](map: SortedMap[Double, A]) : String = map.foldLeft("")((acc, kv) => acc + kv.toString + "\n" )

}

