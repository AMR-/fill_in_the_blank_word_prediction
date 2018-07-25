package nlp

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import scala.language.postfixOps
import scala.collection.{SortedMap, immutable, mutable}
import utils.Props
import utils.GeneralUtils.using

object NGramManager {

  val verboseLogging: Boolean = Props("verbose_logging").get.toBoolean

  // N-Grams
  // Map[String, Map[String, Int]]
  val bigram_fwd_ct = new OuterCountingHashMap[String, String]
  val bigram_rev_ct = new OuterCountingHashMap[String, String]
  // Map[Tuple2, Map[String, Int]]
  val trigram_fwd_ct = new OuterCountingHashMap[(String, String), String]
  val trigram_rev_ct = new OuterCountingHashMap[(String, String), String]

  // Frequency Maps
  var bigram_fwd_freq_k : FrequencyByKey[String, String] = _
  var bigram_rev_freq_k : FrequencyByKey[String, String] = _
  var trigram_fwd_freq_k : FrequencyByKey[(String, String), String] = _
  var trigram_rev_freq_k : FrequencyByKey[(String, String), String] = _

  var has_freq_been_calced = false

  // In order of N-grams listed above, followed by alpha factor
  val weightings: List[Double] = Props("ngram_weightings").getAsDoubleList :+ Props("affect_weighting").get.toDouble

  val stop_words: Set[String] = Props("stop_words").getAsStringList toSet
  val ignore_words: Set[String] = Props("ignore_words").getAsStringList toSet
                                 // TODO if need to, use ignore-words-per-key dict somehow

  val noPredictionPlaceholder: String = Props("no_prediction_placeholder") get

  final def add_counts(sentence: List[String]) : Unit = {
    has_freq_been_calced = false
    val core_sentence: List[String] = strip_stop_words(sentence)
    if (core_sentence.size > 1) {
      core_sentence.sliding(2).foreach(w => bigram_fwd_ct.increment(w.head, w(1)))
      core_sentence.reverse.sliding(2).foreach(w => bigram_rev_ct.increment(w.head, w(1)))
      if (core_sentence.size > 2) {
        core_sentence.sliding(3).foreach(w => trigram_fwd_ct.increment(Tuple2(w.head, w(1)), w(2)))
        core_sentence.reverse.sliding(3).foreach(w => trigram_rev_ct.increment(Tuple2(w.head, w(1)), w(2)))
      }
    }
  }

  final def strip_stop_words(sentence: List[String]) : List[String] = sentence.filterNot(stop_words contains)

  // Calculates the frequency maps from the n-grams
  final def calculateFrequencyMap() : Unit = {
    bigram_fwd_freq_k = nGramCount2FrequencyByKey(bigram_fwd_ct)
    bigram_rev_freq_k = nGramCount2FrequencyByKey(bigram_rev_ct)
    trigram_fwd_freq_k = nGramCount2FrequencyByKey(trigram_fwd_ct)
    trigram_rev_freq_k = nGramCount2FrequencyByKey(trigram_rev_ct)
    has_freq_been_calced = true
  }


  final def save(filename: String) : Unit = {
    println("Saving n-gram data to file.")
    val nGramCounts: NGramCounts = NGramCounts(bigram_fwd_ct, bigram_rev_ct, trigram_fwd_ct, trigram_rev_ct)
    val nGramCountsForStorage: NGramCountsForStorage = toNGramStorable(nGramCounts)
    println(s"There are ${bigram_fwd_ct.size} bigram stems and ${trigram_fwd_ct.size} trigram stems.")
    println(s"${nGramCountsForStorage.bigram_fwd_ct.size} bigram stems " +
      s"and ${nGramCountsForStorage.trigram_fwd_ct.size} trigram stems will be saved.")
    using(new ObjectOutputStream(new FileOutputStream(filename)))(_.writeObject(nGramCountsForStorage))
    println(s"N-gram data saved to $filename")
  }

  final def load(filename: String) : Unit = {
    println(s"Loading N-gram data from $filename")
    val ois = new ObjectInputStream(new FileInputStream(filename))
    val nGramCountsFromStorage: NGramCountsForStorage = ois.readObject.asInstanceOf[NGramCountsForStorage]
    val nGramCounts: NGramCounts = toNGramCounts(nGramCountsFromStorage)
    println(s"There are ${nGramCounts.bigram_fwd_ct.size} bigram stems and ${nGramCounts.trigram_fwd_ct.size} " +
      s"trigram stems in the loaded dataset.")
    populateNGramsFrom(nGramCounts)
    println("N-gram data loaded from file.")
    calculateFrequencyMap()
    println("Frequency map calculated.")
  }

  private def toImmutable[A](innerCountingHashMap: InnerCountingHashMap[A]): immutable.HashMap[A,Int] =
    immutable.HashMap[A,Int](innerCountingHashMap.toSeq:_*)

  private def toMutable[A](immutableInnerCountingHashMap: immutable.HashMap[A,Int]): InnerCountingHashMap[A] =
    new InnerCountingHashMap[A](immutableInnerCountingHashMap.toSeq: _*)

  private def toImmutable[A,B](outerCountingHashMap: OuterCountingHashMap[A,B]):
  immutable.HashMap[A,immutable.HashMap[B,Int]] =
    immutable.HashMap[A,immutable.HashMap[B,Int]](
      outerCountingHashMap.map{ case (k, v) => k -> toImmutable[B](v) }.toSeq:_*)

  private def toMutable[A,B](immutableOuterCountingHashMap: immutable.HashMap[A,immutable.HashMap[B,Int]]):
  OuterCountingHashMap[A,B] =
    new OuterCountingHashMap[A, B](
      immutableOuterCountingHashMap.map{ case (k, v) => k -> toMutable[B](v) }.toSeq: _*
    )

  private def toNGramStorable(nGramCounts: NGramCounts) : NGramCountsForStorage = {
    NGramCountsForStorage(toImmutable(nGramCounts.bigram_fwd_ct), toImmutable(nGramCounts.bigram_rev_ct),
      toImmutable(nGramCounts.trigram_fwd_ct), toImmutable(nGramCounts.trigram_rev_ct))
  }

  private def toNGramCounts(stored: NGramCountsForStorage) : NGramCounts = {
    NGramCounts(toMutable(stored.bigram_fwd_ct), toMutable(stored.bigram_rev_ct),
      toMutable(stored.trigram_fwd_ct), toMutable(stored.trigram_rev_ct))
  }

  private def populateNGramsFrom(nGramCounts: NGramCounts) : Unit = {
    bigram_fwd_ct ++= nGramCounts.bigram_fwd_ct
    bigram_rev_ct ++= nGramCounts.bigram_rev_ct
    trigram_fwd_ct ++= nGramCounts.trigram_fwd_ct
    trigram_rev_ct ++= nGramCounts.trigram_rev_ct
  }

  // Predicts
  // NOTE: make sure when pass in preceding and following that you strip out the stop_words ahead of time
  // Note: invertAffect = False is normal, where FullPredictionInformation's first argument,
  //    positivePredictionInformation, contains the positive info, and the second has negative
  //    if invertAffect = True this is reversed.  For sentences where the desired word should be the opposite affect
  //    so that full sentence has the desired affect
  final def predict_word(preceding: (String, String), following: (String, String), invertAffect: Boolean = false) : Option[FullPredictionInformation[String]] = {
    if (has_freq_been_calced) {
      //noinspection NameBooleanParameters
      Some(FullPredictionInformation(
        getPredictionInformation(preceding, following, !invertAffect),  // normal case is true
        getPredictionInformation(preceding, following, invertAffect)))  // normal case is false
    } else {
      println("Please calculate frequencies first")
      None
    }
  }

  private def getPredictionInformation(preceding: (String, String), following: (String, String), affect: Boolean) :
  PredictionInformation[String] = {
    val combinedSorted: SortedMap[Double,String] = getCombinedFrequencies(preceding, following, affect)
    val predictedWord: String = combinedSorted
      .filter( e => !stop_words.contains(e._2) )  // stop words (though redundant since stripped earlier)
      .find( e => !ignore_words.contains(e._2) )  // blacklist
      .getOrElse[(Double,String)]( (0, noPredictionPlaceholder))._2
    PredictionInformation[String](predictedWord, combinedSorted)
  }

  private def nGramCount2FrequencyByKey[A,B](outerCountMap: OuterCountingHashMap[A,B]): FrequencyByKey[A,B] = {
    val map: mutable.HashMap[A,immutable.HashMap[B,Double]] =
      for (e <- outerCountMap) yield e._1 -> innerCount2SortedNormalizedFreqByKey(e._2)
    new FrequencyByKey[A,B](map.toSeq:_*)
  }

  // includes Laplacian smoothing
  private def innerCount2SortedNormalizedFreqByKey[A](innerCountingHashMap: InnerCountingHashMap[A]):
  immutable.HashMap[A, Double] = {
    val denom: Double = (innerCountingHashMap.size + innerCountingHashMap.values.sum).toDouble
    val map : mutable.HashMap[A, Double] = for (e <- innerCountingHashMap) yield e._1 -> ((e._2 + 1).toDouble / denom)
    immutable.HashMap(map.toSeq: _*)
  }

  private def getCombinedFrequencies(preceding: (String, String), following: (String, String), affect: Boolean) : SortedMap[Double,String] = {
    val combined_ngram : immutable.HashMap[String, Double] = getCombinedNGramFrequencies2(preceding, following)
    if (verboseLogging && combined_ngram.nonEmpty) {
      println(s"Fround some frequencies for preceding and following set $preceding/$following.")
    }
    val affect_factors : immutable.HashMap[String, Double] = getWeightedAffectFactors(combined_ngram, affect)
    val combined_full : immutable.HashMap[String, Double] = merge(combined_ngram, affect_factors)
    val combined_by_freq : immutable.HashMap[Double, String] = combined_full.map{ case (k,v) => (v,k)}
    implicit val floatOrdering = implicitly[Ordering[Double]]
    val combined_sorted: SortedMap[Double,String] =
      SortedMap.empty[Double,String](floatOrdering.reverse) ++ SortedMap[Double,String](combined_by_freq.toSeq: _*)
    combined_sorted
  }

  private def getCombinedNGramFrequencies2(preceding: (String, String), following: (String, String)): immutable.HashMap[String, Double] = {
    val bi_fwd : immutable.HashMap[String, Double] = immutable.HashMap(
      bigram_fwd_freq_k(preceding _2).mapValues(_*weightings.head).toSeq: _*)
    val bi_rev : immutable.HashMap[String, Double] = immutable.HashMap(
      bigram_rev_freq_k(following _1).mapValues(_* weightings(1)).toSeq: _*)
    val tri_fwd : immutable.HashMap[String, Double] = immutable.HashMap(
      trigram_fwd_freq_k(preceding).mapValues(_* weightings(2)).toSeq: _*)
    val tri_rev : immutable.HashMap[String, Double] = immutable.HashMap(
      trigram_rev_freq_k( (following _2, following _1) ).mapValues(_* weightings(3)).toSeq: _*)
    merge(merge(bi_fwd, bi_rev), merge(tri_fwd, tri_rev))
  }

  private def getWeightedAffectFactors(combined_ngram: immutable.HashMap[String, Double], affect: Boolean):
  immutable.HashMap[String, Double] = {
    val weighted_affect: immutable.HashMap[String, Double] =
      immutable.HashMap[String,Double](
        (for (k <- combined_ngram.keys) yield
        k -> AFINN.affectDict(k) * weightings(4) * (if(affect) 1 else -1)
        ).toSeq:_* )
    weighted_affect
  }

  private def merge(m1: immutable.HashMap[String,Double],
                    m2: immutable.HashMap[String,Double]): immutable.HashMap[String,Double] =
    m1.merged(m2)({case ((k,v1),(_,v2)) => (k, v1 + v2)})
}

abstract class CountingHashMap[A,B](elems: (A, B)*) extends mutable.HashMap[A,B] {
  this ++= elems
}

@SerialVersionUID(200L)
class InnerCountingHashMap[A](elems: (A,Int)*) extends CountingHashMap[A, Int] with Serializable {
  this ++= elems
  final override def default(key: A) : Int = 0
  final def increment(key: A) : Option[Int] = {
    this.put(key, this (key) + 1)
  }
}

@SerialVersionUID(300L)
class OuterCountingHashMap[A,B](elems: (A,InnerCountingHashMap[B])*)
  extends CountingHashMap[A, InnerCountingHashMap[B]] with Serializable {
  this ++= elems
  final override def default(key: A) : InnerCountingHashMap[B] = new InnerCountingHashMap[B]()
  final def increment(key1: A, key2: B) : Option[Int] = {
    if (!this.contains(key1)) this.put(key1, new InnerCountingHashMap[B]())

    this (key1) increment key2
  }
}

@SerialVersionUID(400L)
case class NGramCounts(bigram_fwd_ct: OuterCountingHashMap[String, String],
                       bigram_rev_ct: OuterCountingHashMap[String, String],
                       trigram_fwd_ct: OuterCountingHashMap[(String, String), String],
                       trigram_rev_ct: OuterCountingHashMap[(String, String), String]) extends Serializable

@SerialVersionUID(405L)
case class NGramCountsForStorage(bigram_fwd_ct: immutable.HashMap[String, immutable.HashMap[String, Int]],
                                 bigram_rev_ct: immutable.HashMap[String, immutable.HashMap[String, Int]],
                                 trigram_fwd_ct: immutable.HashMap[(String, String), immutable.HashMap[String, Int]],
                                 trigram_rev_ct: immutable.HashMap[(String, String), immutable.HashMap[String, Int]])

class FrequencyByKey[A,B](elems: (A, immutable.HashMap[B,Double])*)
  extends mutable.HashMap[A,immutable.HashMap[B,Double]] {
  this ++= elems
  final override def default(key: A) : immutable.HashMap[B,Double] = {
    if (NGramManager.verboseLogging) println(s"No frequency found for key $key, returning empty.")
    immutable.HashMap.empty[B,Double]
  }
}

case class PredictionInformation[A](prediction: A, details: SortedMap[Double,A])
case class FullPredictionInformation[A](positivePredictionInformation: PredictionInformation[A],
                                        negativePredictionInformation: PredictionInformation[A])
