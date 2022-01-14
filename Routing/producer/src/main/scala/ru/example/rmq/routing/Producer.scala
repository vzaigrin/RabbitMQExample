package ru.example.rmq.routing

import com.rabbitmq.client._
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "direct_logs"
    val factory       = new ConnectionFactory
    factory.setHost("localhost")

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT)

      val severity = getSeverity(args)
      val message  = getMessage(args)

      channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"))
      println(s"[x] Sent '$severity': '$message'")
    }
    sys.exit(0)
  }

  def getSeverity(strings: Array[String]): String = {
    if (strings.length < 1) "info"
    else strings(0)
  }

  def getMessage(strings: Array[String]): String = {
    if (strings.length < 2) "Hello World!"
    else joinStrings(strings, " ", 1)
  }

  def joinStrings(strings: Array[String], delimiter: String, startIndex: Int): String = {
    val length = strings.length

    if (length == 0 || length <= startIndex) ""
    else strings.splitAt(1)._2.mkString(delimiter)
  }
}
