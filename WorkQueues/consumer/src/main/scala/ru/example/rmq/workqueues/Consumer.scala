package ru.example.rmq.workqueues

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Consumer {
  def main(args: Array[String]): Unit = {
    val TASK_QUEUE_NAME = "task_queue"

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    val channel    = connection.createChannel

    class deliverCallback extends DeliverCallback {
      override def handle(s: String, delivery: Delivery): Unit = {
        val message = new String(delivery.getBody, StandardCharsets.UTF_8)
        print(s"[x] Received '$message'")
        try doWork(message)
        finally {
          System.out.println(" Done")
          channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
        }
      }
    }

    class consumerShutdownSignalCallback extends ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(s: String, e: ShutdownSignalException): Unit = {}
    }

    channel.basicQos(1)
    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null)
    println("[*] Waiting for messages. To exit press CTRL+C")

    channel.basicConsume(
      TASK_QUEUE_NAME,
      false,
      new deliverCallback,
      new consumerShutdownSignalCallback
    )
  }

  def doWork(task: String): Unit = {
    task.toCharArray.foreach { ch =>
      if (ch == '.')
        try Thread.sleep(1000)
        catch {
          case _: InterruptedException => Thread.currentThread.interrupt()
        }
    }
  }
}
