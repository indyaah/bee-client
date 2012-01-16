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
import sbt.Package._
import Keys._

object Resolvers {
  val resolvers = Seq (ScalaToolsReleases, DefaultMavenRepository)
}

object Dependencies {
  val slf4jVersion    = "1.6.+"
  val logbackVersion  = "0.9.+"
//  val springVersion   = "3.0.5.RELEASE"
  val jettyVersion    = "6.1.26"
//  val jacksonVersion  = "1.7.3"
  val jerseyVersion = "1.10"

//  val springAsm        = "org.springframework"  % "spring-asm"         % springVersion withSources()
//  val springCore       = "org.springframework"  % "spring-core"        % springVersion withSources()
//  val springContext    = "org.springframework"  % "spring-context"     % springVersion withSources()
//  val springExpression = "org.springframework"  % "spring-expression"  % springVersion withSources()
//  val springWeb        = "org.springframework"  % "spring-web"         % springVersion withSources()
//  val springWebMvc     = "org.springframework"  % "spring-webmvc"      % springVersion withSources()
//  val springBeans      = "org.springframework"  % "spring-beans"       % springVersion withSources()

  val slf4jApi         = "org.slf4j"            % "slf4j-api"          % slf4jVersion withSources()
  val slf4jJcl         = "org.slf4j"            % "jcl-over-slf4j"     % slf4jVersion withSources()
  val slf4jLog4j       = "org.slf4j"            % "log4j-over-slf4j"   % slf4jVersion withSources()

  val logbackCore      = "ch.qos.logback"       % "logback-core"       % logbackVersion
  val logbackClassic   = "ch.qos.logback"       % "logback-classic"    % logbackVersion

//  val jacksonCore      = "org.codehaus.jackson" % "jackson-core-asl"   % jacksonVersion
//  val jacksonMapper    = "org.codehaus.jackson" % "jackson-mapper-asl" % jacksonVersion

//  val jerseyClient     = "com.sun.jersey"       % "jersey-client"      % jerseyVersion
//  val jerseyCore       = "com.sun.jersey"       % "jersey-core"        % jerseyVersion
//  val jerseyServer     = "com.sun.jersey"       % "jersey-server"      % jerseyVersion
//  val jerseyServlet    = "com.sun.jersey"       % "jersey-servlet"     % jerseyVersion
  //val jerseySpring     = "com.sun.jersey.contribs" % "jersey-spring" % jerseyVersion

  val apacheCommons    = "org.apache.commons"   % "commons-lang3"      % "3.+" withJavadoc() intransitive()
  val apacheIo         = "commons-io"           % "commons-io"         % "1.+" withSources() intransitive()
  //  val apacheNet	       = "commons-net"        % "commons-net"    % "2.0"
  //  val apacheCodec      = "commons-codec"      % "commons-codec"  % "1.4"

  val googleGuava      = "com.google.guava"   % "guava"          % "10.0.+" withSources() withJavadoc()

  val jodaTime         = "joda-time"          % "joda-time"      % "2.+" withJavadoc()
  val jodaConvert      = "joda-time"          % "joda-convert"   % "1.+" withJavadoc()

  val jcsp             = "org.codehaus.jcsp"  % "jcsp"           % "1.1-rc5"

  val jettyCore        = "org.mortbay.jetty" % "jetty"          % jettyVersion withSources()
  val jettyEmbedded    = "org.mortbay.jetty" % "jetty-embedded" % jettyVersion withSources()
  val jettyUtil        = "org.mortbay.jetty" % "jetty-util"     % jettyVersion withSources()
  val servlet          = "javax.servlet"     % "servlet-api"    % "2.5" withSources()

  val xmlResolver      = "xml-resolver"     % "xml-resolver"      % "1.1" withSources()
  val jsoup            = "org.jsoup"        % "jsoup"             % "1.6.+" withSources() withJavadoc()

//  val databinder       = "net.databinder"  %% "unfiltered-filter" % "0.5.0"

  // ========== Test ==========

  val scalatest	       = "org.scalatest"           % "scalatest_2.9.1"          % "1.6.+" % "test" withSources()
  val junit            = "junit"                   % "junit"                    % "4.8.+" % "test" withSources()
  val junitInterface   = "com.novocode"            % "junit-interface"          % "0.7"   % "test"

  // see lib folder
  //val stubserver       = "tarttelin"               % "stubserver"               % "0.2"   % "test"

//  val mockito          = "org.mockito"             % "mockito-all"              % "1.8.+" % "test"
}
