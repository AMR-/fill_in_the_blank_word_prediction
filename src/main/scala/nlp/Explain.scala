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
      explanation append(s"\nTop positive prediction (${npred prediction}):\n" +
        s" ${str(pre)} ${npred prediction} ${str(post)}\n" +
        s"Additional considered options:\n")
      explanation append str(npred details)
      explanation append '\n'
    }
    )
    explanation toString
  }

}

class MergeMap[A,B,C,D](x: Map[A,B], y: Map[A,C], f: (Option[B],Option[C]) => Option[D]) {
  def get(key: A): Option[D] = f(x.get(key),y.get(key))
  def keys: Set[A] = x.keySet  ++ y.keySet
  def apply(key: A) : Option[D] = this get key
}
