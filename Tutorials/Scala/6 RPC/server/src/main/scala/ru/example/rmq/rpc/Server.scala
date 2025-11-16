package ru.example.rmq.rpc

import com.rabbitmq.client._
import java.nio.charset.StandardCharsets

object Server {
  def main(args: Array[String]): Unit = {
    val RPC_QUEUE_NAME = "rpc_queue"
    val hostname       = if (args.length > 0) args(0) else "localhost"
    val username       = if (args.length > 1) args(1) else "username"
    val password       = if (args.length > 2) args(2) else "password"
    val virtualHost    = "/"

    lazy val fib: LazyList[Int] = 0 #:: 1 #:: fib.zip(fib.tail).map { case (a, b) => a + b }

    val factory = new ConnectionFactory()
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    val arguments = new java.util.HashMap[String, Object]()
    arguments.put("x-message-ttl", 3600000.asInstanceOf[Object])

    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, arguments)
    channel.queuePurge(RPC_QUEUE_NAME)
    channel.basicQos(1)

    println("[x] Awaiting RPC requests")

    channel.basicConsume(
      RPC_QUEUE_NAME,
      false,
      (_: String, delivery: Delivery) => {
        val replyProps = new AMQP.BasicProperties.Builder()
          .correlationId(delivery.getProperties.getCorrelationId)
          .build()

        try {
          val message = new String(delivery.getBody, StandardCharsets.UTF_8)
          val n       = message.toInt

          val response = fib(n).toString
          println(s"[.] fib($message) = $response")

          channel.basicPublish(
            "",
            delivery.getProperties.getReplyTo,
            replyProps,
            response.getBytes(StandardCharsets.UTF_8)
          )
        } catch {
          case e: Exception => System.err.println(e.getLocalizedMessage)
        } finally {
          channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
        }
      },
      (_: String, _: ShutdownSignalException) => {}
    )
  }
}
