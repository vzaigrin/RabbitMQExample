package ru.example.rmq.helloworld

import com.rabbitmq.client.ConnectionFactory
import java.nio.charset.StandardCharsets

object Producer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME = "hello"
    val message    = "Hello World!"

    try {
      val factory = new ConnectionFactory
      factory.setHost("localhost")
      val connection = factory.newConnection
      val channel    = connection.createChannel

      channel.queueDeclare(QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8))
      println(s"[x] Sent '$message'")
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
    sys.exit(0)
  }
}
