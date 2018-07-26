import scala.language.postfixOps
import nlp.FullPredictionInformation
import utils.Props
import utils.SentenceUtils.{firstTwo, lastTwo}
import utils.FileUtils.{writeMapToCsv, writeStringToFile}
import nlp.NGramManager.{load, predict_word}
import nlp.SentenceProcessor.getSentenceComponentsByID
import nlp.Explain.{explainPredictions,summarizePredictions}

object Predict extends App {
  println("Predict")

  val nGramCountFilename: String = Props("ngram_ct_filename") get
  val in_sentences_filename: String = Props("input_csv_filename") get
  val out_sentences_positive_filename: String = Props("output_csv_positive_filename") get
  val out_sentences_negative_filename: String = Props("output_csv_negative_filename") get
  val out_detail_filename: String = Props("output_details") get
  val out_summary_filename: String = Props("output_summary") get
  val noPredictionPlaceholder: String = Props("no_prediction_placeholder") get

  load(nGramCountFilename)
  println("Loaded NGram Model")

  println(s"loading sentences from $in_sentences_filename")
  // pre-process to get list of ids + preceding/following words
  val sentencesById: Map[String, (Array[String], Array[String], Boolean)] =
    getSentenceComponentsByID(in_sentences_filename)
  println(s"Sentences loaded.  There are ${sentencesById.size} sentences.")

  val predictions: Map[String, Option[FullPredictionInformation[String]]] =
    sentencesById.map{case (k, (pre, post, inversion)) =>
      (k, predict_word(lastTwo(pre), firstTwo(post), inversion))}
  println("Calculated Predictions")

  val positives: Map[String,String] =
    predictions map{ case(k, v) => v match {
      case Some(fpred) => (k, fpred.positivePredictionInformation prediction)
      case None => (k, noPredictionPlaceholder)
    }
    }
//  val sortedPositives: SortedMap[String,String] = SortedMap(positives.toSeq:_*)(SentenceIdOrder) TODO order
  writeMapToCsv(positives, out_sentences_positive_filename)
  println(s"Wrote positive csv file at $out_sentences_positive_filename")

  val negatives: Map[String,String] =
    predictions map{ case(k, v) => v match {
      case Some(fpred) => (k, fpred.negativePredictionInformation prediction)
      case None => (k, noPredictionPlaceholder)
    }
    }
  writeMapToCsv(negatives, out_sentences_negative_filename)
  println(s"Wrote negative csv file at $out_sentences_negative_filename")

  val summary: String = summarizePredictions(sentencesById, negatives, positives)
  writeStringToFile(summary, out_summary_filename)
  println(s"Wrote summary text file at $out_summary_filename")

  val explanation: String = explainPredictions(sentencesById, predictions)
  writeStringToFile(explanation, out_detail_filename)
  println(s"Wrote explanation text file at $out_detail_filename")

}
