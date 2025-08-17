package ru.example.rmq.routing

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage: Consumer host [info] [warning] [error]")
      sys.exit(-1)
    }

    val EXCHANGE_NAME = "direct_logs"
    val host          = args(0)
    val user          = "user"
    val password      = "password"
    val virtualHost   = "/"

    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(user)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    try {
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
          val message = new String(delivery.getBody, StandardCharsets.UTF_8)
          println(s"[x] Received '${delivery.getEnvelope.getRoutingKey}': '$message'")
        },
        (_: String, _: ShutdownSignalException) => {}
      )
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
  }
}
