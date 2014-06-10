package examples

import org.scalatest.FunSuite
import java.net.ConnectException

class AllExampleTest extends FunSuite {

  val args = Array[String]()

  test("Example1a") {
    Example1a.main(args)
  }

  test("Example1b") {
    Example1b.main(args)
  }

  test("Example1c") {
    Example1c.main(args)
  }

  test("Example2a") {
    Example2a.main(args)
  }

  test("Example2b") {
    Example2b.main(args)
  }

  test("Example3a") {
    Example3a.main(args)
  }

  test("Example3b") {
    try {
      Example3b.main(args)
    } catch {
      case e: ConnectException => // skip
    }
  }

  test("Example4a") {
    try {
      Example4a.main(args)
    } catch {
      case e: ConnectException => // skip
    }
  }

  test("Example5a") {
    Example5a.main(args)
  }

  test("Example5b") {
    Example5b.main(args)
  }

  test("Example5c") {
    Example5c.main(args)
  }

  test("Example5d") {
    Example5d.main(args)
  }
}
