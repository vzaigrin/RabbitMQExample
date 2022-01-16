package ru.example.rmq.publishsubscribe

import com.rabbitmq.client._

object Consumer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "logs"

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout")
    val queueName = channel.queueDeclare().getQueue
    channel.queueBind(queueName, EXCHANGE_NAME, "")

    println("[*] Waiting for messages. To exit press CTRL+C")
    channel.basicConsume(
      queueName,
      true,
      (_: String, delivery: Delivery) => {
        val message = new String(delivery.getBody, "UTF-8")
        println(s"[x] Received '$message'")
      },
      (_: String, _: ShutdownSignalException) => {}
    )
  }
}
