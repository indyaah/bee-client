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

package uk.co.bigbeeconsultants.http.util

import java.io._
import com.sdicons.json.model._
import scala.util.matching.Regex
import collection.JavaConversions
import collection.immutable.ListMap
import com.sdicons.json.parser.JSONParser

/**
 * Pimps JsonTools (http://jsontools.berlios.de/) for easy Scala access.
 */
class JSONWrapper(val json: JSONValue) {

  private val IntPattern = new Regex("[0-9]+")

  def isArray = json.isArray

  def isBoolean = json.isBoolean

  def isComplex = json.isComplex

  def isDecimal = json.isDecimal

  def isInteger = json.isInteger

  def isNull = json.isNull

  def isNumber = json.isNumber

  def isObject = json.isObject

  def isSimple = json.isSimple

  def isString = json.isString

  def render(pretty: Boolean) = json.render(pretty)

  private def getNode(path: String): JSONValue = {
    val parts = path.split('/')
    var node = json
    for (i <- 0 until parts.length) {
      parts(i) match {
        case IntPattern(i) => node = node.asInstanceOf[JSONArray].get(i.toInt)
        case s => node = node.asInstanceOf[JSONObject].get(s)
      }
      if (node == null) {
        throw new NoSuchElementException(parts.take(i + 1).mkString("/"))
      }
    }
    node
  }

  def get(path: String): JSONWrapper = {
    new JSONWrapper(getNode(path))
  }

  lazy val asBoolean = {
    json.asInstanceOf[JSONBoolean].getValue
  }

  lazy val asString = {
    json.asInstanceOf[JSONString].getValue
  }

  lazy val asInteger = {
    new BigInt(json.asInstanceOf[JSONInteger].getValue)
  }

  lazy val asDecimal = {
    new BigDecimal(json.asInstanceOf[JSONDecimal].getValue)
  }

  lazy val asArray: Vector[JSONWrapper] = {
    val juList = json.asInstanceOf[JSONArray].getValue
    Vector() ++ JavaConversions.iterableAsScalaIterable(juList).map(new JSONWrapper(_))
  }

  lazy val asMap: ListMap[String, JSONWrapper] = {
    val juMap = json.asInstanceOf[JSONObject].getValue
    val entries = for (e <- JavaConversions.iterableAsScalaIterable(juMap.entrySet())) yield {
      e.getKey -> new JSONWrapper(e.getValue)
    }
    ListMap() ++ entries
  }
}

object JSONWrapper {
  implicit def apply(json: JSONValue): JSONWrapper = new JSONWrapper(json)

  def apply(is: InputStream): JSONWrapper = apply(new JSONParser(is).nextValue)

  def apply(ir: Reader): JSONWrapper = apply(new JSONParser(ir).nextValue)

  def apply(s: String): JSONWrapper = apply(new StringReader(s))
}