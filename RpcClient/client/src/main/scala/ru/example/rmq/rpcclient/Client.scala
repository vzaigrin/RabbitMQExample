package ru.example.rmq.rpcclient

import com.rabbitmq.client._
import scala.util.Using

object Client {
  def main(args: Array[String]): Unit = {
    val requestQueueName = "rpc_queue"
    val host             = if (args.length > 0) args(0) else "localhost"
    val factory          = new ConnectionFactory
    factory.setHost(host)

    Using.Manager { use =>
      val connection = use(factory.newConnection)
      val channel    = use(connection.createChannel)

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
    }
    sys.exit(0)
  }
}
