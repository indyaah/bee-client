//-----------------------------------------------------------------------------
// The MIT License
//
// Copyright (c) 2012 Rick Beton <rick@bigbeeconsultants.co.uk>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//-----------------------------------------------------------------------------

package uk.co.bigbeeconsultants.http.response

import uk.co.bigbeeconsultants.http.header.MediaType
import scala.io.Source
import collection.mutable

object MimeTypeRegistry {

  lazy val table: Map[String, MediaType] = {
    val map = new mutable.HashMap[String, MediaType]
    val is = getClass.getClassLoader.getResourceAsStream("mime-types.txt")
    require(is != null, "Failed to load mime-types.txt")
    try {
      val source = Source.fromInputStream(is, "utf-8")
      for (line <- source.getLines()) {
        val trimmed = line.trim
        if (trimmed.length > 0 && trimmed(0) != '#') {
          val sp = line.indexOf(' ')
          if (sp > 0) {
            val mime = MediaType(line.substring(0, sp))
            line.substring(sp).trim.split(' ').foreach(map.put(_, mime))
          }
        }
      }

    } finally {
      is.close()
    }
    map.toMap
  }

  /** This set holds all the 'unusual' textual types, in addition to the widely known types. */
  lazy val textualTypes: Set[String] = {
    val set = new mutable.HashSet[String]
    val is = getClass.getClassLoader.getResourceAsStream("textual-types.txt")
    require(is != null, "Failed to load textual-types.txt")
    try {
      val source = Source.fromInputStream(is, "utf-8")
      for (line <- source.getLines()) {
        val trimmed = line.trim
        if (trimmed.length > 0 && trimmed(0) != '#') {
          set += line
        }
      }

    } finally {
      is.close()
    }
    set.toSet
  }
}
