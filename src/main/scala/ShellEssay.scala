//#!/bin/sh
//exec scala -savecompiled $0 $@
//!#

import uk.co.bigbeeconsultants.shell._
import uk.co.bigbeeconsultants.shell.Shell._

object ShellEssay {
  val sh = echoCommands.exitOnError

  def main(args: Array[String]) {
    //    if (args.length > 1) {
    //      val lines = readFile (args (0))
    //      val result = lines.mkString ("\n")
    //      println (result)
    //    }
    //    else {
    println ("===== 1 =====")
    sh.cmd ("ls -l src") |> Stdout
    println ("===== 2 =====")
    sh.cat (List ("hello", "world", env ("SHELL"))) | "cut -c 2-" |> Stdout
    println ("===== 3 =====")
    sh.cat (List ("hello", "world", env ("SHELL"))) | ((x: String) => x.substring (0, 3)) |> Stdout
    println ("===== 4 =====")
    sh.cat (EncFile ("/etc/hosts")) |> Stdout
    println ("===== 5 =====")
    sh.cat (EncFile ("/etc/hosts")) & ((x: String) => x.startsWith ("127.")) | ((x: String) => "=" + x) |> Stdout
    println ("===== 6 =====")
    sh.cmd ("find . -type d") & ((x: String) => x.startsWith ("./src/main/")) |> Stdout
    println ("===== 7 =====")
    sh.cmd ("cat /nonexistent") |> Stdout
    println ("===== 8 =====")
    //      println ("Usage: cat-nginx-files file")
    //    }
  }

  //  private def readFile(fileName: String) = {
  //    val lines = TextFile(fileName).get
  //    val noComments = lines.map (_.replaceAll ("#.*", "").replaceAll (" +$", ""))
  //    val noBlanks = noComments.filter (_.length > 0)
  //    noBlanks
  //  }
}
