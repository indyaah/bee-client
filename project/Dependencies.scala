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

import sbt._

object Resolvers {
  //val localhost = URLRepository("local-nexus", Patterns("http://localhost:8081/nexus/"))
  val bigbeeRepo = URLRepository("bigbee", Patterns("http://repo.bigbeeconsultants.co.uk/repo/"))
  val resolvers = Seq(bigbeeRepo, DefaultMavenRepository)
}

object Dependencies {
  val slf4jVersion = "1.7.7"
  val logbackVersion = "1.1.2"
  val jettyVersion = "6.1.26"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion withSources()
  val slf4jJcl = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion withSources()
  val slf4jLog4j = "org.slf4j" % "log4j-over-slf4j" % slf4jVersion withSources()

  val jodaTime = "joda-time" % "joda-time" % "2.+" withJavadoc()
  val jodaConvert = "org.joda" % "joda-convert" % "1.7"


  def scalaActors(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor)) if scalaMajor >= 10 =>
        Seq("org.scala-lang" % "scala-actors" % "2.10.0" % "test")
      case _ =>
        Seq()
    }

//  val jcsp = "org.codehaus.jcsp" % "jcsp" % "1.1-rc5"

  val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5.20110712" withSources()
//  val servlet = "javax.servlet" % "servlet-api" % "2.5" withSources()

  val beeConfig = "bee-config" %% "bee-config" % "1.4.+" withSources() withJavadoc()

  // ========== Test ==========

  val junit = "junit" % "junit" % "4.11" % "test"

  def scalaTest(scalaVersion: String) = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor)) if scalaMajor >= 10 =>
        "org.scalatest" %% "scalatest" % "2.1.3" % "test" withSources()
      case _ =>
        "org.scalatest" %% "scalatest" % "1.9.1" % "test" withSources()
    }
  }

  //val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.1" % "test" withSources()

  val jsontools = "com.sdicons.jsontools" % "jsontools-core" % "1.7" % "test" withSources()

  // http://tarttelin.github.io/JavaStubServer/
  val javastubserver = "com.pyruby" % "java-stub-server" % "0.12"   % "test" withSources()

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"

  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion % "test"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion % "test"

  val jettyEmbedded = "org.mortbay.jetty" % "jetty-embedded" % jettyVersion % "test" withSources()
  //  val jettyCore        = "org.mortbay.jetty"  % "jetty"             % jettyVersion withSources()
  //  val jettyUtil        = "org.mortbay.jetty"  % "jetty-util"        % jettyVersion withSources()
}
