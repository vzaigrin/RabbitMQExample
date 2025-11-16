package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME  = "hello"
    val hostname    = if (args.length > 0) args(0) else "localhost"
    val username    = if (args.length > 1) args(2) else "username"
    val password    = if (args.length > 2) args(3) else "password"
    val virtualHost = "/"

    val arguments = new java.util.HashMap[String, Object]()
    arguments.put("x-message-ttl", 3600000.asInstanceOf[Object])

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    try {
      val connection = factory.newConnection
      val channel    = connection.createChannel

      channel.queueDeclare(QUEUE_NAME, true, false, false, arguments)
      println("[*] Waiting for messages. To exit press CTRL+C")

      channel.basicConsume(
        QUEUE_NAME,
        true,
        (_: String, delivery: Delivery) => {
          val message = new String(delivery.getBody, StandardCharsets.UTF_8)
          println(s"[x] Received '$message'")
        },
        (_: String, _: ShutdownSignalException) => {}
      )
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
  }
}
