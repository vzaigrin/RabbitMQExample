package ru.example.rmq.helloworld

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val QUEUE_NAME  = "hello"
    val hostname    = if (args.length > 0) args(0) else "localhost"
    val username    = if (args.length > 1) args(2) else "username"
    val password    = if (args.length > 2) args(3) else "password"
    val message     = if (args.length > 3) args(1) else "Hello World!"
    val virtualHost = "/"

    val arguments = new java.util.HashMap[String, Object]()
    arguments.put("x-message-ttl", 3600000.asInstanceOf[Object])

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    Using.Manager { use =>
      try {
        val connection = use(factory.newConnection)
        val channel    = use(connection.createChannel)

        channel.queueDeclare(QUEUE_NAME, true, false, false, arguments)
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8))
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
