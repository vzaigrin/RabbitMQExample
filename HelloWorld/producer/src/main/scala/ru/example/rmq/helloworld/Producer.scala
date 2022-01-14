package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME = "hello"
    val message    = "Hello World!"
    val factory    = new ConnectionFactory
    factory.setHost("localhost")

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
