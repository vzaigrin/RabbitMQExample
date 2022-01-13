package ru.example.rmq.topics

import com.rabbitmq.client._

object Producer {
  def main(args: Array[String]): Unit = {
    val EXCHANGE_NAME = "topic_logs"

    try {
      val factory = new ConnectionFactory
      factory.setHost("localhost")

      val connection = factory.newConnection
      val channel    = connection.createChannel
      channel.exchangeDeclare(EXCHANGE_NAME, "topic")

      val routingKey = getRouting(args)
      val message    = getMessage(args)

      channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"))
      println(s"[x] Sent '$routingKey': '$message'")
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
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

    if (length == 0) ""
    else if (length <= startIndex) ""
    else strings.splitAt(1)._2.mkString(delimiter)
  }
}
