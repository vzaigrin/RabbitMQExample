package ru.example.rmq.workqueues

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"
    val hostname        = if (args.length > 0) args(0) else "localhost"
    val username        = if (args.length > 2) args(0) else "username"
    val password        = if (args.length > 3) args(0) else "password"
    val message         = if (args.length > 4) args.slice(1, args.length).mkString(" ") else "Message"
    val virtualHost     = "/"

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

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, arguments)
        channel.basicPublish(
          "",
          TASK_QUEUE_NAME,
          MessageProperties.PERSISTENT_TEXT_PLAIN,
          message.getBytes(StandardCharsets.UTF_8)
        )
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
