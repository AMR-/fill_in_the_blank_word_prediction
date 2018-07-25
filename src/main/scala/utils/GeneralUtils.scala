package utils

import scala.language.reflectiveCalls

object GeneralUtils {

  def using[A, B <: {def close(): Unit}] (closeable: B) (f: B => A): A =
    try {
      f(closeable)
    } finally {
      if (closeable != null)
        closeable close()
    }

}
