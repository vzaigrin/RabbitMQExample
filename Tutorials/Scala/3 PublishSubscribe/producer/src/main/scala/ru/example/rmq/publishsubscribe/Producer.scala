package ru.example.rmq.publishsubscribe

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "logs"

    if (args.length < 3) {
      System.out.println("Usage: Producer hostname username password [message]")
      System.exit(-1)
    }

    val hostname    = args(0)
    val username    = args(1)
    val password    = args(2)
    val virtualHost = "/"

    val message =
      if (args.length > 3) args.slice(4, args.length).mkString(" ")
      else "info: Hello World!"

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    Using.Manager { use =>
      try {
        val connection = use(factory.newConnection)
        val channel    = use(connection.createChannel)

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT)
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8))
        println(s"[x] Sent '$message'")
      } catch {
        case e: Exception =>
          println(e.getLocalizedMessage)
          sys.exit(-1)
      }
    }
    sys.exit(0)
  }
}
