package de.choffmeister.akkastreamviz

import java.awt.image.RenderedImage
import java.io._

import akka.Done
import akka.stream.scaladsl.{Flow, Keep, Sink, StreamConverters}
import akka.util.ByteString
import javax.imageio.ImageIO

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.{BasicIO, Process}

object VideoGenerator {
  def generate(file: File,
               framerate: Int = 24)(implicit executor: ExecutionContext): Sink[RenderedImage, Future[Done]] = {
    Flow[RenderedImage]
      .map(frame => {
        val stream = new ByteArrayOutputStream()
        ImageIO.write(frame, "png", stream)
        ByteString(stream.toByteArray)
      })
      .toMat(StreamConverters.asInputStream(10.seconds))(Keep.right)
      .mapMaterializedValue { frames =>
        val proc = Process(
          "ffmpeg",
          Seq("-y", "-f", "image2pipe", "-i", "-", "-vcodec", "h264", "-r", framerate.toString, file.toString)
        )
        var stdout = Option.empty[ByteString]
        var stderr = Option.empty[ByteString]
        var stdin = Option.empty[OutputStream]
        val io = BasicIO
          .standard(true)
          .withOutput(stream => {
            val bytes = new ByteArrayOutputStream()
            copyStream(stream, bytes)
            stdout = Some(ByteString(bytes.toByteArray))
            stream.close()
          })
          .withError(stream => {
            val bytes = new ByteArrayOutputStream()
            copyStream(stream, bytes)
            stderr = Some(ByteString(bytes.toByteArray))
            stream.close()
          })
          .withInput(stream => {
            copyStream(frames, stream)
            stream.close()
          })
        Future {
          proc.run(io).exitValue() match {
            case 0    => Done
            case code => throw new RuntimeException(s"ffmpeg exited with code $code\n\n${stderr.get.utf8String}")
          }
        }
      }
  }

  private def copyStream(from: InputStream, to: OutputStream, bufferSize: Int = 8192): Unit = {
    val buffer = new Array[Byte](1024 * 1024)
    var count = 0
    do {
      count = from.read(buffer)
      if (count > 0) to.write(buffer, 0, count)
    } while (count > 0)
  }
}
