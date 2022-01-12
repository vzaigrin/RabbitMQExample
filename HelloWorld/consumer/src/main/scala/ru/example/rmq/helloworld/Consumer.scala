package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME = "hello"

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    val channel    = connection.createChannel

    class deliverCallback extends DeliverCallback {
      override def handle(s: String, delivery: Delivery): Unit = {
        val message = new String(delivery.getBody, StandardCharsets.UTF_8)
        println(s"[x] Received '$message'")
      }
    }

    class consumerShutdownSignalCallback extends ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(s: String, e: ShutdownSignalException): Unit = {}
    }

    channel.queueDeclare(QUEUE_NAME, false, false, false, null)
    println("[*] Waiting for messages. To exit press CTRL+C")

    channel.basicConsume(QUEUE_NAME, true, new deliverCallback, new consumerShutdownSignalCallback)
  }
}
