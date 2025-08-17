package ru.example.rmq.publishsubscribe

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "logs"
    val host          = if (args.length > 0) args(0) else "localhost"
    val user          = "user"
    val password      = "password"
    val virtualHost   = "/"

    val message =
      if (args.length < 2) "info: Hello World!"
      else args.slice(1, args.length).mkString(" ")

    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(user)
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
