package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME = "hello"
    val host       = if (args.length > 0) args(0) else "localhost"
    val message    = if (args.length > 1) args(1) else "Hello World!"

    val factory = new ConnectionFactory
    factory.setHost(host)

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)

      channel.queueDeclare(QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8))
      println(s"[x] Sent '$message'")
    }
    sys.exit(0)
  }
}
