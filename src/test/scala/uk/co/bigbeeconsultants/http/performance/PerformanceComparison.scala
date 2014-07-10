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

package uk.co.bigbeeconsultants.http.performance

import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.Request
import uk.co.bigbeeconsultants.http.response.BufferedResponseBuilder
import uk.co.bigbeeconsultants.http.util.Duration
import java.io.{PrintWriter, OutputStreamWriter, FileOutputStream, File}
import java.text.SimpleDateFormat
import java.util.Date

object PerformanceComparison {
  // Make 20 iterations then discard the worst ten. This smoothes out JVM JIT optimisation effects.
  def sample(log: PrintWriter, script: PrintWriter, iterations: Int, size: String) {
    val name = "large-file-" + size + ".tmp"
    LargeFileGenerator.main(Array(size, "src/test/resources/" + name))
    val client = new HttpClient
    val request = Request.get("http://beeclient/" + name)

    val timings = for (i <- 1 to 2*iterations) yield {
      val responseBuilder = new BufferedResponseBuilder
      client.execute(request, responseBuilder)
      //println(request.url + " " + responseBuilder.response.get.status.code + " " + responseBuilder.networkTimeTaken)
      responseBuilder.networkTimeTaken
    }
    val sTimings = timings.sorted.take(iterations)
    val average = sTimings.fold(Duration.Zero)(_ + _) / iterations
    log.println(name + " average " + average)
    println(name + " average " + average)
    //println("ab -n 10 " + request.url)
    script.println("curl -s -w '" + name + " %{size_download}B %{time_total}ms\\n' -o curl.tmp "  + request.url + " >> performance/curl-$D.txt")
  }

  def open(name: String): PrintWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(name)))

  def now = {
    val df = new SimpleDateFormat("yyyy-MM-dd'_'HHmmss")
    df.format(new Date)
  }

  def main(args: Array[String]) {
    new File("performance").mkdirs()
    val script = open("curl-bench.sh")
    val log = open("performance/beeclient-" + now + ".txt")
    script.println("D=$(date '+%F_%H%M%S')")
    script.println("mkdir -p performance")
    script.println("rm -f performance/curl-$D.txt")
    sample(log, script, 300, "10B")
    sample(log, script, 100, "100B")
    sample(log, script, 100, "1KiB")
    sample(log, script, 60, "10KiB")
    sample(log, script, 30, "100KiB")
    sample(log, script, 30, "1MiB")
    sample(log, script, 10, "10MiB")
    sample(log, script, 10, "30MiB")
    sample(log, script, 6, "100MiB")
    sample(log, script, 3, "300MiB")
//    sample(script, 1, "1GiB")
    script.println("rm -f curl.tmp")
    script.close()
    log.close()
  }
}
