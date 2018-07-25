package utils

import java.io.File
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import scala.language.postfixOps

object FileUtils {

  final def asString(file : File) : String = {
    scala.io.Source fromFile file mkString
  }

  final def writeMapToCsv[A](map: Map[String,String], filename: String) : Unit = {
    writeStringToFile(
      map.foldLeft(StringBuilder newBuilder)( (acc, kv) => acc.append(kv._1 + "," + kv._2 + "\n") ).toString,
      filename)
  }

  final def writeStringToFile(string: String, filename: String): Unit = {
//    new File(filename) mkdirs()
//    scala.tools.nsc.io.File(filename).writeAll(string)
    Files.write(Paths.get(filename), string.getBytes(StandardCharsets.UTF_8))
  }

  final def allFromDir(fullDir : String) : List[File] = {
    new java.io.File(fullDir).listFiles toList
  }

}
