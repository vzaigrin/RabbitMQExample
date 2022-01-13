package ru.example.rmq.routing

import com.rabbitmq.client._

object Consumer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "direct_logs"

    if (args.length < 1) {
      println("Usage: Consumer [info] [warning] [error]")
      sys.exit(-1)
    }

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)
    val queueName = channel.queueDeclare().getQueue

    args.foreach { severity => channel.queueBind(queueName, EXCHANGE_NAME, severity) }

    class deliverCallback extends DeliverCallback {
      override def handle(s: String, delivery: Delivery): Unit = {
        val message = new String(delivery.getBody, "UTF-8")
        println(s"[x] Received '${delivery.getEnvelope.getRoutingKey}': '$message'")
      }
    }

    class consumerShutdownSignalCallback extends ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(s: String, e: ShutdownSignalException): Unit = {}
    }

    println("[*] Waiting for messages. To exit press CTRL+C")
    channel.basicConsume(queueName, true, new deliverCallback, new consumerShutdownSignalCallback)
  }
}
