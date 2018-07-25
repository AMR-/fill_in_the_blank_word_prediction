import scala.language.postfixOps
import java.io.File

import nlp.Preprocessing.{preprocess, tokenize}
import nlp.NGramManager.{add_counts, save}
import utils.FileUtils.allFromDir
import utils.FileUtils.writeStringToFile
import utils.Props

object BuildModel extends App {
  println("Build Model")

  val nGramCountFilename: String = Props("ngram_ct_filename") get

  val corpusFileFolder = Props("corpus_file_folder") get
  val files : List[File] = allFromDir(corpusFileFolder)
  println(s"There are ${files.size} files to process.")

  val test_preprocessing: Boolean = Props("test_preprocessing").get toBoolean
  val test_pp_prefix: String = Props("test_preprocessing_out_prefix") get
  val skip_training: Boolean = Props("skip_training").get toBoolean

  // Process each file into list of lists, then for each update the ngram
  files.foreach(
    f => {
      val name: String = f.getName
      println(s"Preparing to extract from file $name")
      val prepocessedFile: String = preprocess(f)
      if (test_preprocessing) writeStringToFile(prepocessedFile, s"$test_pp_prefix$name")
      if (!skip_training) {
        val sentences: List[List[String]] = tokenize(prepocessedFile)
        println(s"There are ${sentences.size} sentences extracted from $name")
        sentences.foreach(add_counts)
        println(s"Finished n-gram counting for sentences from $name.")
      }
    }
  )

  save(nGramCountFilename)

}
