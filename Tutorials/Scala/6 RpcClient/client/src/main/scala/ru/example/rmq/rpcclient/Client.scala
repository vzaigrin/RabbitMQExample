package ru.example.rmq.rpcclient

import com.rabbitmq.client._
import scala.util.Using

object Client {
  def main(args: Array[String]): Unit = {
    val requestQueueName = "rpc_queue"
    val hostname         = if (args.length > 0) args(0) else "localhost"
    val username         = if (args.length > 1) args(1) else "username"
    val password         = if (args.length > 2) args(2) else "password"
    val virtualHost      = "/"

    val factory = new ConnectionFactory
    factory.setHost(hostname)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

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
        print(s"[x] Requesting fib($i_str) ..")
        val response = rpc.stringCall(i_str)
        println(s" Got '$response'")
      }
    }
    sys.exit(0)
  }
}
