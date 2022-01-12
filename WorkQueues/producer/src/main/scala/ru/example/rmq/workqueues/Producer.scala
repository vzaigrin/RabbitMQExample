package ru.example.rmq.workqueues

import com.rabbitmq.client.{ConnectionFactory, MessageProperties}

object Producer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"
    val message         = args.mkString(" ")

    try {
      val factory = new ConnectionFactory
      factory.setHost("localhost")
      val connection = factory.newConnection
      val channel    = connection.createChannel

      channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null)
      channel.basicPublish(
        "",
        TASK_QUEUE_NAME,
        MessageProperties.PERSISTENT_TEXT_PLAIN,
        message.getBytes("UTF-8")
      )
      println(s"[x] Sent '$message'")
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
    sys.exit(0)
  }
}
