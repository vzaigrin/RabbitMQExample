package ru.example.rmq.routing

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "direct_logs"
    val host          = if (args.length > 0) args(0) else "localhost"
    val user          = "user"
    val password      = "password"
    val virtualHost   = "/"

    val strings  = args.slice(1, args.length)
    val severity = getSeverity(strings)
    val message  = getMessage(strings)

    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(user)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    Using.Manager { use =>
      try {
        val connection = use(factory.newConnection)
        val channel    = use(connection.createChannel)

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)
        channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes(StandardCharsets.UTF_8))
        println(s"[x] Sent '$severity': '$message'")
      } catch {
        case e: Exception =>
          println(e.getLocalizedMessage)
          sys.exit(-1)
      }
    }
    sys.exit(0)
  }

  private def getSeverity(strings: Array[String]): String = {
    if (strings.length < 1) "info"
    else strings(0)
  }

  private def getMessage(strings: Array[String]): String = {
    if (strings.length < 2) "Hello World!"
    else joinStrings(strings)
  }

  private def joinStrings(strings: Array[String]): String = {
    val delimiter  = " "
    val startIndex = 1
    val length     = strings.length

    if (length == 0 || length <= startIndex) ""
    else strings.splitAt(1)._2.mkString(delimiter)
  }
}
