package ru.example.rmq.workqueues

import com.rabbitmq.client._
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"
    val host            = if (args.length > 0) args(0) else "localhost"
    val factory         = new ConnectionFactory
    factory.setHost(host)
    val message = args.slice(1, args.length).mkString(" ")

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)

      channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null)
      channel.basicPublish(
        "",
        TASK_QUEUE_NAME,
        MessageProperties.PERSISTENT_TEXT_PLAIN,
        message.getBytes("UTF-8")
      )
      println(s"[x] Sent '$message'")
    }
    sys.exit(0)
  }
}
