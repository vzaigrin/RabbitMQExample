package ru.example.rmq.topics

import com.rabbitmq.client._

object Consumer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "topic_logs"

    if (args.length < 2) {
      println("Usage: Consumer <host> [binding_key]...")
      sys.exit(-1)
    }

    val host    = args(0)
    val factory = new ConnectionFactory
    factory.setHost(host)
    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.exchangeDeclare(EXCHANGE_NAME, "topic")
    val queueName = channel.queueDeclare().getQueue

    args.slice(1, args.length).foreach { bindingKey =>
      println(s"Bind to $bindingKey")
      channel.queueBind(queueName, EXCHANGE_NAME, bindingKey)
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
