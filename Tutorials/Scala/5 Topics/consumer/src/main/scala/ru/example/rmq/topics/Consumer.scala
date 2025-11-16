package ru.example.rmq.topics

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "topic_logs"
    val virtualHost   = "/"

    if (args.length < 4) {
      println("Usage: Consumer hostname username password binding_key [binding_key...]")
      sys.exit(-1)
    }

    val hostname = args(0)
    val username = args(1)
    val password = args(2)

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    try {
      val connection = factory.newConnection
      val channel    = connection.createChannel

      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC)
      val queueName = channel.queueDeclare().getQueue

      args.slice(3, args.length).foreach { bindingKey =>
        println(s"Bind to $bindingKey")
        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey)
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
