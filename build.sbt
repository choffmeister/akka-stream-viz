val akkaVersion = "2.5.21"

name := "akka-stream-viz"
organization := "de.choffmeister"
scalaVersion := "2.12.7"
crossScalaVersions := Seq("2.12.7", "2.11.12")

resolvers += Resolver.bintrayRepo("choffmeister", "maven")
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

scmInfo := Some(ScmInfo(url("https://github.com/choffmeister/akka-stream-viz"), "git@github.com:choffmeister/akka-stream-viz.git"))
git.remoteRepo := scmInfo.value.get.connection
sourceDirectory in Paradox := baseDirectory.value / "docs"
siteSubdirName in SiteScaladoc := "api"
paradoxProperties in Paradox ++= Map(
  "snip.src.base_dir" -> (baseDirectory.value + "/src/main/scala/de/choffmeister/akkastreamviz/"),
  "snip.test.base_dir" -> (baseDirectory.value + "/src/test/scala/de/choffmeister/akkastreamviz/"),
  "scaladoc.de.choffmeister.akkastreamviz.base_url" -> "/api"
)

enablePlugins(GitVersioning)
enablePlugins(ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin)
