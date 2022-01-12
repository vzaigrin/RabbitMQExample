package ru.example.rmq.publishsubscribe

import com.rabbitmq.client.{ConnectionFactory, MessageProperties}

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "logs"

    val message =
      if (args.length < 1) "info: Hello World!"
      else args.mkString(" ")

    try {
      val factory = new ConnectionFactory
      factory.setHost("localhost")
      val connection = factory.newConnection
      val channel    = connection.createChannel
      channel.exchangeDeclare(EXCHANGE_NAME, "fanout")
      channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"))
      println(s"[x] Sent '$message'")
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
    sys.exit(0)
  }
}
