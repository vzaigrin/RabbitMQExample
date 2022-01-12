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

    class deliverCallback extends DeliverCallback {
      override def handle(s: String, delivery: Delivery): Unit = {
        val message = new String(delivery.getBody, "UTF-8")
        println(s"[x] Received '$message'")
      }
    }

    class consumerShutdownSignalCallback extends ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(s: String, e: ShutdownSignalException): Unit = {}
    }

    println("[*] Waiting for messages. To exit press CTRL+C")
    channel.basicConsume(queueName, true, new deliverCallback, new consumerShutdownSignalCallback)
  }
}
