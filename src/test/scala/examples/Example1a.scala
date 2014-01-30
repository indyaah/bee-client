package examples

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.response.Response
import java.net.URL

object Example1a {
  def main(args: Array[String]) {
    val httpClient = new HttpClient
    val response: Response = httpClient.get(new URL("http://www.google.com/"))
    println(response.status)
    println(response.headers.list.mkString("", "\n", "\n"))
    println(response.body.asString)
  }
}
