package ru.example.rmq.topics

import com.rabbitmq.client._
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "topic_logs"
    val host          = if (args.length > 0) args(0) else "localhost"
    val factory       = new ConnectionFactory
    factory.setHost(host)

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)
      channel.exchangeDeclare(EXCHANGE_NAME, "topic")

      val strings    = args.slice(1, args.length)
      val routingKey = getRouting(strings)
      val message    = getMessage(strings)

      channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"))
      println(s"[x] Sent '$routingKey': '$message'")
    }
    sys.exit(0)
  }

  def getRouting(strings: Array[String]): String = {
    if (strings.length < 1) "anonymous.info"
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
