package ru.example.rmq.workqueues

import com.rabbitmq.client._
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"
    val message         = args.mkString(" ")
    val factory         = new ConnectionFactory
    factory.setHost("localhost")

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
