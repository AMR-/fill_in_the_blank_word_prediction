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

//  final def sortedMapIdComparator : Ordering[String] = 0
//  case class SentenceIdOrder(implicit s: String) extends Ordered[String] {
    // Greater than 0 means this > that
//    def compare(that: String) : Int = {
//      val s1 = this.s
//      val s2 = that.s
//      if (this.s == that) 0 else {
//        val s1 = stripChars(this.s)
//        val s2 = stripChars(that)
//        if (s1 == s2) this.s.compareTo(that) else {
//          s1.toInt - s2.toInt
//        }
//      }
//    }
//    private def stripChars(st: String): String = st.replaceAll("[a-zA-Z]","")
//    private def stripChars(st: String): String = st.replaceAll("[^0-9]","")
//  }

}

