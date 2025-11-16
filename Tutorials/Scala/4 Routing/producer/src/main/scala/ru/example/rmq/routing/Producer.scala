package ru.example.rmq.routing

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "direct_logs"
    val virtualHost   = "/"

    if (args.length < 3) {
      System.out.println("Usage: Consumer hostname username password [severity] [message...]")
      System.exit(-1)
    }

    val hostname = args(0)
    val username = args(1)
    val password = args(2)

    val severity = if (args.length > 3) args(3) else "info"
    val message  = if (args.length > 4) args.slice(4, args.length).mkString(" ") else "Hello World!"

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    Using.Manager { use =>
      try {
        val connection = use(factory.newConnection)
        val channel    = use(connection.createChannel)

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)
        channel.basicPublish(
          EXCHANGE_NAME,
          severity,
          null,
          message.getBytes(StandardCharsets.UTF_8)
        )
        println(s"[x] Sent '$severity': '$message'")
      } catch {
        case e: Exception =>
          println(e.getLocalizedMessage)
          sys.exit(-1)
      }
    }
    sys.exit(0)
  }
}
