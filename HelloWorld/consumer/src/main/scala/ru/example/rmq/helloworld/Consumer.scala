package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME = "hello"
    val host       = if (args.length == 1) args(0) else "localhost"

    val factory = new ConnectionFactory
    factory.setHost(host)
    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.queueDeclare(QUEUE_NAME, false, false, false, null)
    println("[*] Waiting for messages. To exit press CTRL+C")

    channel.basicConsume(
      QUEUE_NAME,
      true,
      (_: String, delivery: Delivery) => {
        val message = new String(delivery.getBody, StandardCharsets.UTF_8)
        println(s"[x] Received '$message'")
      },
      (_: String, _: ShutdownSignalException) => {}
    )
  }
}
