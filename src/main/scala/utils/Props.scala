package utils

import java.util.Properties
import utils.GeneralUtils.using
import scala.collection.JavaConverters._
import scala.language.postfixOps

object Props {
  val DEFAULT_PROPERTIES_FILE = "local.default.properties"
  val MAIN_PROPERTIES_FILE = "local.properties"

  def asList = 0

  // props load is like props.load(_)
  private val localProperties : Map[String, String] = {
    val props = new Properties()
    using(getClass.getClassLoader getResourceAsStream DEFAULT_PROPERTIES_FILE)(props load)
    using(getClass.getClassLoader getResourceAsStream MAIN_PROPERTIES_FILE)(props load)
    props.asScala.toMap
  }

  def get(name : String) : Option[String] = {
    localProperties get name
  }

  def apply(name : String) : PropertyOption = PropertyOption(get(name))

}

case class PropertyOption(prop : Option[String]) {
  final def get: String = prop get
  final def getOrElse[B >: String](default: => B): B =
    if (prop isEmpty) default else prop get
  final def getAsStringList: List[String] = {
    prop match {
      case Some(p) => p split ',' toList
      case None => List()
    }
  }
  final def getAsFloatList: List[Float] =
    if (prop isEmpty) List[Float]() else prop.get.split(',').toList map(_ toFloat)
  final def getAsDoubleList: List[Double] =
    if (prop isEmpty) List[Double]() else prop.get.split(',').toList map(_ toDouble)
}

