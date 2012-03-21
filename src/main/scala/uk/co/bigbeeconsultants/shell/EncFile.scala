package uk.co.bigbeeconsultants.shell

import java.io.File

class EncFile(val name: File, val encoding: String = "UTF-8") {

  def mkdirs(): Boolean = {
    name.getParentFile.mkdirs ()
  }
}

object EncFile {
  def apply(fileName: String, encoding: String = "UTF-8") = new EncFile (new File (fileName), encoding)
}
