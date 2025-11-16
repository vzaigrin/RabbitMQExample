package ru.example.rmq

import com.rabbitmq.client._
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.BooleanSupplier
import scala.util.Using

object PublisherConfirms {
  private val arguments = new java.util.HashMap[String, Object]()

  def main(args: Array[String]): Unit = {
    val host         = if (args.length > 0) args(0) else "localhost"
    val user         = if (args.length > 1) args(1) else "username"
    val password     = if (args.length > 2) args(2) else "username"
    val messageCount = if (args.length > 3) args(3).toInt else 50_000
    val virtualHost  = "/"

    arguments.put("x-message-ttl", 3600000.asInstanceOf[Object])

    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(user)
    factory.setPassword(password)
    factory.setVirtualHost(virtualHost)

    Using.Manager { use =>
      try {
        val connection = use(factory.newConnection)
        val channel    = use(connection.createChannel)
        channel.confirmSelect

        publishMessagesIndividually(channel, messageCount)
        publishMessagesInBatch(channel, messageCount)
        handlePublishConfirmsAsynchronously(channel, messageCount)
      } catch {
        case e: Exception =>
          println(e.getLocalizedMessage)
          sys.exit(-1)
      }
    }
    sys.exit(0)
  }

  private def publishMessagesIndividually(channel: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    channel.queueDeclare(queue, false, false, false, arguments)

    val start = System.nanoTime

    (0 until messageCount).foreach { i =>
      channel.basicPublish("", queue, null, i.toString.getBytes)
      channel.waitForConfirmsOrDie(5_000)
    }

    val end = System.nanoTime
    println(s"Published $messageCount messages individually in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  private def publishMessagesInBatch(channel: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    channel.queueDeclare(queue, false, false, false, arguments)

    val batchSize = 100
    val quotient  = math.ceil(messageCount / batchSize).toInt
    val remainder = messageCount % batchSize
    val batchList = (0 until batchSize).toList

    val start = System.nanoTime

    (0 until messageCount)
      .zip((0 until quotient).toList.flatMap(_ => batchList) ::: batchList.slice(0, remainder))
      .foreach { ij =>
        channel.basicPublish("", queue, null, ij._1.toString.getBytes)
        if (ij._2 == batchSize) channel.waitForConfirmsOrDie(5_000)
      }
    if (remainder > 0) channel.waitForConfirmsOrDie(5_000)

    val end = System.nanoTime
    println(s"Published $messageCount messages in batch in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  private def handlePublishConfirmsAsynchronously(channel: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    channel.queueDeclare(queue, false, false, false, arguments)
    val outstandingConfirms = new ConcurrentSkipListMap[Long, String]

    val cleanOutstandingConfirms: ConfirmCallback = (sequenceNumber: Long, multiple: Boolean) => {
      if (multiple) {
        val confirmed = outstandingConfirms.headMap(sequenceNumber, true)
        confirmed.clear()
      } else outstandingConfirms.remove(sequenceNumber)
    }

    channel.addConfirmListener(
      cleanOutstandingConfirms,
      (sequenceNumber: Long, multiple: Boolean) => {
        val body = outstandingConfirms.get(sequenceNumber)
        System.err.println(
          s"Message with body $body has been nack-ed. Sequence number: $sequenceNumber, multiple: $multiple"
        )
        cleanOutstandingConfirms.handle(sequenceNumber, multiple)
      }
    )

    val start = System.nanoTime

    (0 until messageCount).foreach { i =>
      val body = String.valueOf(i)
      outstandingConfirms.put(channel.getNextPublishSeqNo, body)
      channel.basicPublish("", queue, null, body.getBytes)
    }

    if (!waitUntil(Duration.ofSeconds(60), () => outstandingConfirms.isEmpty))
      throw new IllegalStateException("All messages could not be confirmed in 60 seconds")

    val end = System.nanoTime
    println(s"Published $messageCount messages and handled confirms asynchronously in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  @throws[InterruptedException]
  private def waitUntil(timeout: Duration, condition: BooleanSupplier): Boolean = {
    var waited = 0
    while ({ !condition.getAsBoolean && waited < timeout.toMillis }) {
      Thread.sleep(100L)
      waited = +100
    }
    condition.getAsBoolean
  }
}
