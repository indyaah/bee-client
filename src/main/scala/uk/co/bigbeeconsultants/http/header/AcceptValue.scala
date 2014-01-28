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

package uk.co.bigbeeconsultants.http.header

/**
 * Holds a header value that refers to a list of acceptable content types as used in an Accept header.
 */
case class AcceptValue(parts: List[Qualifiers] = Nil) extends Value {

  import AcceptValue._

  lazy val value = parts map (_.toString) mkString ", "

  lazy val isValid = {
    parts forall {
      _.qualifiers forall {
        case NameVal(_, None) => true
        case NameVal("q", Some(v)) => QualityPattern.matcher(v).matches
        case _ => false
      }
    }
  }

  def +(mt: MediaType) = append(mt)

  def append(mt: MediaType) =
    new AcceptValue(parts :+ mediaTypeAsQualifier(mt))

  def append(mt: MediaType, q: Float) =
    new AcceptValue(parts :+ mediaTypeAsQualifier(mt, q))
}

object AcceptValue {
  val QualityPattern = "(1(\\.0{1,3})?)|(0(\\.\\d{1,3})?)".r.pattern

  def apply(mt: MediaType) = new AcceptValue(List(mediaTypeAsQualifier(mt)))

  def apply(mt: MediaType, q: Float) = new AcceptValue(List(mediaTypeAsQualifier(mt, q)))

  private def mediaTypeAsQualifier(mt: MediaType) = {
    val nv1 = NameVal(mt.mediaType, None)
    Qualifiers(List(nv1))
  }

  private def mediaTypeAsQualifier(mt: MediaType, q: Float) = {
    require(0.0 <= q && q <= 1.0, q + " is not valid; q must be in the range 0.0 to 1.0")
    val qStr = q.toString
    val qqStr = if (qStr.length > 5) qStr.substring(0, 5) else qStr
    val nv1 = NameVal(mt.mediaType, None)
    val nv2 = NameVal("q", Some(qqStr))
    Qualifiers(List(nv1, nv2))
  }
}