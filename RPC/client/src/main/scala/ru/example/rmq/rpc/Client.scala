package ru.example.rmq.rpc

import com.rabbitmq.client._
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue

object Client {
  val requestQueueName           = "rpc_queue"
  var factory: ConnectionFactory = _
  var connection: Connection     = _
  var channel: Channel           = _

  def main(args: Array[String]): Unit = {
    try {
      val host = if (args.length > 0) args(0) else "localhost"
      factory = new ConnectionFactory
      factory.setHost(host)
      connection = factory.newConnection
      channel = connection.createChannel

      (0 until 32).foreach { i =>
        val i_str = i.toString
        println(s"[x] Requesting fib($i_str)")
        val response = call(i_str)
        println(s"[.] Got '$response'")
      }
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    } finally {
      if (channel != null) channel.close()
      if (connection != null) connection.close()
    }
    sys.exit(0)
  }

  def call(message: String): String = {
    val corrId         = UUID.randomUUID.toString
    val replyQueueName = channel.queueDeclare().getQueue
    val response       = new ArrayBlockingQueue[String](1)

    val props = new AMQP.BasicProperties.Builder()
      .correlationId(corrId)
      .replyTo(replyQueueName)
      .build()

    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"))

    val cTag = channel.basicConsume(
      replyQueueName,
      true,
      (_: String, delivery: Delivery) => {
        if (delivery.getProperties.getCorrelationId.equals(corrId)) {
          response.offer(new String(delivery.getBody, "UTF-8"))
        }
      },
      (_: String, _: ShutdownSignalException) => {}
    )

    val result = response.take()
    channel.basicCancel(cTag)
    result
  }
}
