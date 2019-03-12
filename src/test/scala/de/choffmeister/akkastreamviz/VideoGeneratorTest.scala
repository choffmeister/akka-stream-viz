package de.choffmeister.akkastreamviz

import java.awt.image.BufferedImage
import java.io._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext

class VideoGeneratorTest
    extends TestKit(ActorSystem())
    with WordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(15, Seconds), interval = Span(10, Millis))
  implicit val materializer = ActorMaterializer()

  "work" in {
    val width = 100
    val height = 100
    val file = File.createTempFile("akka-stream-viz", ".mp4")
    println(file)
    Source
      .unfold((width / 2, height / 2)) {
        case (x, y) =>
          val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
          val rgb = (Math.random() * Int.MaxValue).toInt & 0xffffff
          img.setRGB(x, y, rgb)
          val dx = (Math.random() * 3).toInt - 1
          val dy = (Math.random() * 3).toInt - 1
          Some((cap(x + dx, 0, width - 1), cap(y + dy, 0, height - 1)), img)
      }
      .take(24 * 5)
      .runWith(VideoGenerator.generate(file, 24)(ExecutionContext.Implicits.global))
      .futureValue
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  private def cap(i: Int, min: Int, max: Int) = Math.min(Math.max(i, min), max)
}
