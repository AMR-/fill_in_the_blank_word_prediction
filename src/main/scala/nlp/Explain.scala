package nlp

import scala.language.postfixOps
import utils.SentenceUtils.str

object Explain {

  type CombinedTuple = (Array[String], Array[String], Option[FullPredictionInformation[String]])

  type CombinedInformationMap = MergeMap[String,
    (Array[String], Array[String], Boolean),Option[FullPredictionInformation[String]],
    CombinedTuple]

  final def explainPredictions(sentences: Map[String, (Array[String], Array[String], Boolean)], predictions: Map[String, Option[FullPredictionInformation[String]]]): String = {
    val explanation: StringBuilder = StringBuilder.newBuilder
    val info: CombinedInformationMap =
      new CombinedInformationMap(sentences, predictions, (sent, pred) => Some(sent.get _1, sent.get _2, pred get))
    info.keys.foreach( id => {
      val (pre, post, ofpred) : CombinedTuple = info(id) get
      val ppred = ofpred.get positivePredictionInformation
      val npred = ofpred.get negativePredictionInformation

      explanation append s"\n\nPrediction Information for sentence $id: '${str(pre)} _ ${str(post)}'\n\n"
      explanation append(s"Top positive prediction (${ppred prediction}):\n" +
        s" ${str(pre)} ${ppred prediction} ${str(post)}\n" +
        s"Additional considered options:\n")
      explanation append str(ppred details)
      explanation append(s"\nTop negative prediction (${npred prediction}):\n" +
        s" ${str(pre)} ${npred prediction} ${str(post)}\n" +
        s"Additional considered options:\n")
      explanation append str(npred details)
      explanation append '\n'
    }
    )
    explanation toString
  }

  final def summarizePredictions(sentences: Map[String, (Array[String], Array[String], Boolean)], negatives: Map[String,String], positives: Map[String,String]): String = {
    val summary_pos: StringBuilder = StringBuilder.newBuilder
    summary_pos.append("Positive sentences:\n")
    val summary_neg: StringBuilder = StringBuilder.newBuilder
    summary_neg.append("Negative sentences:\n")
    sentences.foreach( kv => {
      val (id: String, pre: Array[String], post: Array[String]) = (kv._1, kv._2._1, kv._2._2)
      summary_pos.append(id + ": " + str(pre) + " " + positives(id) + " " + str(post) + "\n")
      summary_neg.append(id + ": " + str(pre) + " " + negatives(id) + " " + str(post) + "\n")
    }
    )
    summary_pos.toString + "\n" + summary_neg.toString
  }

}

class MergeMap[A,B,C,D](x: Map[A,B], y: Map[A,C], f: (Option[B],Option[C]) => Option[D]) {
  def get(key: A): Option[D] = f(x.get(key),y.get(key))
  def keys: Set[A] = x.keySet  ++ y.keySet
  def apply(key: A) : Option[D] = this get key
}
