package ru.example.rmq.publishsubscribe

import com.rabbitmq.client.ConnectionFactory
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "logs"
    val host          = if (args.length > 1) args(0) else "localhost"

    val factory = new ConnectionFactory
    factory.setHost(host)

    val message =
      if (args.length < 2) "info: Hello World!"
      else args.slice(1, args.length).mkString(" ")

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)

      channel.exchangeDeclare(EXCHANGE_NAME, "fanout")
      channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"))
      println(s"[x] Sent '$message'")
    }
    sys.exit(0)
  }
}
