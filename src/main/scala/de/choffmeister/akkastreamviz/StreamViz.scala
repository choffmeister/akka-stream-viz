package de.choffmeister.akkastreamviz

import java.awt.image.BufferedImage
import java.io.File

import akka.NotUsed
import akka.stream.{KillSwitches, Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Source}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

final class StreamVizContext(file: File, width: Int, height: Int)(implicit ec: ExecutionContext, mat: Materializer) {
  private var nodes = Map.empty[String, (Int, Int)]
  private val emptyFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
  private val frames =
    Source.queue(3, OverflowStrategy.dropHead)
      .prepend(Source.single(emptyFrame))
      .keepAlive((1.0 / 24.0).seconds, () => emptyFrame)
      .map { frame =>
        println("frame")
        frame
      }
      .to(VideoGenerator.generate(file, 24))
      .run()

  def stop(): Unit = {
    frames.complete()
  }

  private[akkastreamviz] def emit(group: String, id: String): Unit = {
    println(s"${System.currentTimeMillis} $group $id")
  }
}

object StreamViz {
  def createContext(file: File, width: Int, height: Int)(implicit ec: ExecutionContext, mat: Materializer): StreamVizContext = new StreamVizContext(file, width, height)

  def apply[T](group: String, id: T => String)(implicit ctx: StreamVizContext): Flow[T, T, NotUsed] = {
    Flow[T]
      .map { elem =>
        ctx.emit(group, id(elem))
        elem
      }
  }
}
