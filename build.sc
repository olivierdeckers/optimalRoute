import mill._
import mill.modules.Jvm
import mill.scalalib._

object optimalRoute extends ScalaModule {
  def scalaVersion = "2.12.5"

  def ivyDeps = Agg(
    ivy"org.scala-graph::graph-core:1.12.3",
    ivy"org.scala-lang.modules::scala-xml:1.1.0",
    ivy"org.graphstream:gs-core:1.3",
    ivy"org.graphstream:gs-algo:1.3"
  )

  def debug(args: String*) = T.command {
    Jvm.interactiveSubprocess(
      finalMainClass(),
      runClasspath().map(_.path),
      forkArgs() ++ Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
      forkEnv(),
      args,
      workingDir = ammonite.ops.pwd
    )
  }

  object test extends Tests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.6.0")
    def testFrameworks = Seq("utest.runner.Framework")
  }
}
