package ru.example.rmq.rpcclient

import com.rabbitmq.client._

object Client {
  def main(args: Array[String]): Unit = {
    val requestQueueName = "rpc_queue"

    try {
      val factory = new ConnectionFactory
      factory.setHost("localhost")
      val connection = factory.newConnection
      val channel    = connection.createChannel

      val rpcClientParams = new RpcClientParams()
        .channel(channel)
        .exchange("")
        .routingKey(requestQueueName)
      val rpc = new RpcClient(rpcClientParams)

      (0 until 32).foreach { i =>
        val i_str = i.toString
        println(s"[x] Requesting fib($i_str)")
        val response = rpc.stringCall(i_str)
        println(s"[.] Got '$response'")
      }
    } catch {
      case e: Exception =>
        println(e.getLocalizedMessage)
        sys.exit(-1)
    }
    sys.exit(0)
  }
}
