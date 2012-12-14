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

/**
 * Provides a simple diagnostic timer for evaluating execution times of sections of code.
 * The measurement precision is to the nearest microsecond.
 *
 * Simply create an instance before the code under test and observe the duration afterwards.
 */
final class DiagnosticTimer {
  val thousand = 1000
  val million = thousand * thousand

  /** The time at which the timer was started, measured in arbitrary ticks. */
  val start = now

  private def now = System.nanoTime() / thousand

  /** The duration since the timer was started, measured in microseconds. */
  def duration = now - start

  /** Gets the duration since the timer was started, formatted for humans to read. */
  override def toString =
    if (duration >= 50 * million)
      (duration / million) + "s"
    else if (duration >= 50 * thousand)
      (duration / thousand) + "ms"
    else
      duration + "μs"
}
