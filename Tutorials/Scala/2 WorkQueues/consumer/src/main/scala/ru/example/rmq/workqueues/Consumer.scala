package ru.example.rmq.workqueues

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"
    val host            = if (args.length > 0) args(0) else "localhost"
    val user            = "user"
    val password        = "password"
    val virtualHost     = "/"

    val arguments = new java.util.HashMap[String, Object]()
    arguments.put("x-message-ttl", 3600000.asInstanceOf[Object])

    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(user)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    try {
      val connection = factory.newConnection
      val channel    = connection.createChannel

      channel.basicQos(1)
      channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, arguments)
      println("[*] Waiting for messages. To exit press CTRL+C")

      channel.basicConsume(
        TASK_QUEUE_NAME,
        false,
        (_: String, delivery: Delivery) => {
          val message = new String(delivery.getBody, StandardCharsets.UTF_8)
          print(s"[x] Received '$message'")
          try doWork(message)
          finally {
            println(" Done")
            channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
          }
        },
        (_: String, _: ShutdownSignalException) => {}
      )
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
  }

  private def doWork(task: String): Unit = {
    task.toCharArray.foreach { ch =>
      if (ch == '.')
        try Thread.sleep(1000)
        catch {
          case _: InterruptedException => Thread.currentThread.interrupt()
        }
    }
  }
}
