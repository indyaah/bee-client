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

name := "bee-client"

organization := "uk.co.bigbeeconsultants"

version := "0.24.1"

// 2.10.1 suffices for all micro versions of 2.10
crossScalaVersions := Seq("2.9.0", "2.9.1", "2.9.2", "2.9.3", "2.10.1")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("http://www.bigbeeconsultants.co.uk/bee-client"))

//publishTo := Some(Resolver.file("file", new File("/home/websites/your/releases"))

// append several options to the list of options passed to the Java compiler
//javacOptions += "-g:none"
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// append -deprecation to the options passed to the Scala compiler
//scalacOptions += "-deprecation"

// Copy all managed dependencies to <build-root>/lib_managed/
retrieveManaged := true
