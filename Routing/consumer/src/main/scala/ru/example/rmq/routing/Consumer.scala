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

    println("[*] Waiting for messages. To exit press CTRL+C")
    channel.basicConsume(
      queueName,
      true,
      (_: String, delivery: Delivery) => {
        val message = new String(delivery.getBody, "UTF-8")
        println(s"[x] Received '${delivery.getEnvelope.getRoutingKey}': '$message'")
      },
      (_: String, _: ShutdownSignalException) => {}
    )
  }
}
