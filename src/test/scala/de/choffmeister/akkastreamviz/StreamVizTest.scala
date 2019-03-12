package de.choffmeister.akkastreamviz

import java.io.File

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class StreamVizTest
    extends TestKit(ActorSystem())
    with WordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(15, Seconds), interval = Span(10, Millis))
  implicit val materializer = ActorMaterializer()

  "work" in {
    val file = File.createTempFile("akka-stream-viz", ".mp4")
    println(file)
    implicit val ec = ExecutionContext.Implicits.global
    implicit val ctx = StreamViz.createContext(file, 800, 800)

    val source = Source.unfold(0)(i => Some(i + 1, i))
      .via(StreamViz("source", _.toString))
      .take(100)
      .throttle(1, 100.millis, 1, ThrottleMode.shaping)

    val sink = Flow[Int]
      .via(StreamViz("sink", _.toString))
      .toMat(Sink.ignore)(Keep.right)

    source.toMat(sink)(Keep.right).run().futureValue

    ctx.stop()
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }
}
