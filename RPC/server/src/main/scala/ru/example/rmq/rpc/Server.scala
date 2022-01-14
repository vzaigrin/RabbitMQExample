package ru.example.rmq.rpc

import com.rabbitmq.client._

object Server {
  def main(args: Array[String]): Unit = {
    val RPC_QUEUE_NAME          = "rpc_queue"
    lazy val fib: LazyList[Int] = 0 #:: 1 #:: fib.zip(fib.tail).map { case (a, b) => a + b }

    val factory = new ConnectionFactory()
    factory.setHost("localhost")

    val connection = factory.newConnection
    val channel    = connection.createChannel

    channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null)
    channel.queuePurge(RPC_QUEUE_NAME)
    channel.basicQos(1)

    class deliverCallback extends DeliverCallback {
      override def handle(s: String, delivery: Delivery): Unit = {
        val replyProps = new AMQP.BasicProperties.Builder()
          .correlationId(delivery.getProperties.getCorrelationId)
          .build()

        try {
          val message = new String(delivery.getBody, "UTF-8")
          val n       = message.toInt

          val response = fib(n).toString
          println(s"[.] fib($message) = $response")

          channel.basicPublish(
            "",
            delivery.getProperties.getReplyTo,
            replyProps,
            response.getBytes("UTF-8")
          )
        } catch {
          case e: Exception => println(e.getLocalizedMessage)
        } finally {
          channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
        }
      }
    }

    class consumerShutdownSignalCallback extends ConsumerShutdownSignalCallback {
      override def handleShutdownSignal(s: String, e: ShutdownSignalException): Unit = {}
    }

    println("[x] Awaiting RPC requests")
    channel.basicConsume(
      RPC_QUEUE_NAME,
      false,
      new deliverCallback,
      new consumerShutdownSignalCallback
    )
  }
}
