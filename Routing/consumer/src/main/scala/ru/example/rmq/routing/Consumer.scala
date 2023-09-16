package ru.example.rmq.routing

import com.rabbitmq.client._

object Consumer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "direct_logs"

    if (args.length < 1) {
      println("Usage: Consumer host [info] [warning] [error]")
      sys.exit(-1)
    }

    val host    = args(0)
    val factory = new ConnectionFactory
    factory.setHost(host)
    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)
    val queueName = channel.queueDeclare().getQueue

    args.slice(1, args.length).foreach { severity =>
      channel.queueBind(queueName, EXCHANGE_NAME, severity)
    }

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
