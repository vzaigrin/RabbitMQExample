package ru.example.rmq

import com.rabbitmq.client.{ConfirmCallback, _}
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.BooleanSupplier
import scala.util.Using

object PublisherConfirms {
  def main(args: Array[String]): Unit = {
    val host         = if (args.length > 0) args(0) else "localhost"
    val username     = if (args.length > 1) args(1) else "guest"
    val password     = if (args.length > 2) args(2) else "guest"
    val messageCount = if (args.length > 3) args(3).toInt else 50_000

    val cf = new ConnectionFactory
    cf.setHost(host)
    cf.setUsername(username)
    cf.setPassword(password)
    cf.newConnection

    Using.Manager { use =>
      val connection = use(cf.newConnection)
      val ch         = use(connection.createChannel)
      ch.confirmSelect

      publishMessagesIndividually(ch, messageCount)
      publishMessagesInBatch(ch, messageCount)
      handlePublishConfirmsAsynchronously(ch, messageCount)
    }
    sys.exit(0)
  }

  def publishMessagesIndividually(ch: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    ch.queueDeclare(queue, false, false, true, null)
    val start = System.nanoTime

    (0 until messageCount).foreach { i =>
      ch.basicPublish("", queue, null, i.toString.getBytes)
      ch.waitForConfirmsOrDie(5_000)
    }

    val end = System.nanoTime
    println(s"Published $messageCount messages individually in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  def publishMessagesInBatch(ch: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    ch.queueDeclare(queue, false, false, true, null)

    val batchSize = 100
    val quotient  = math.ceil(messageCount / batchSize).toInt
    val remainder = messageCount % batchSize
    val batchList = (0 until batchSize).toList
    val start     = System.nanoTime

    (0 until messageCount)
      .zip((0 until quotient).toList.flatMap(_ => batchList) ::: batchList.slice(0, remainder))
      .foreach { ij =>
        ch.basicPublish("", queue, null, ij._1.toString.getBytes)
        if (ij._2 == batchSize) ch.waitForConfirmsOrDie(5_000)
      }
    if (remainder > 0) ch.waitForConfirmsOrDie(5_000)

    val end = System.nanoTime
    println(s"Published $messageCount messages in batch in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  def handlePublishConfirmsAsynchronously(ch: Channel, messageCount: Int): Unit = {
    val queue = UUID.randomUUID.toString
    ch.queueDeclare(queue, false, false, true, null)
    val outstandingConfirms = new ConcurrentSkipListMap[Long, String]

    val cleanOutstandingConfirms: ConfirmCallback = (sequenceNumber: Long, multiple: Boolean) => {
      if (multiple) {
        val confirmed = outstandingConfirms.headMap(sequenceNumber, true)
        confirmed.clear()
      } else outstandingConfirms.remove(sequenceNumber)
    }

    ch.addConfirmListener(
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
      outstandingConfirms.put(ch.getNextPublishSeqNo, body)
      ch.basicPublish("", queue, null, body.getBytes)
    }

    if (!waitUntil(Duration.ofSeconds(60), () => outstandingConfirms.isEmpty))
      throw new IllegalStateException("All messages could not be confirmed in 60 seconds")

    val end = System.nanoTime
    println(
      s"Published $messageCount messages and handled confirms asynchronously in ${Duration.ofNanos(end - start).toMillis} ms"
    )
  }

  @throws[InterruptedException]
  def waitUntil(timeout: Duration, condition: BooleanSupplier): Boolean = {
    var waited = 0
    while ({ !condition.getAsBoolean && waited < timeout.toMillis }) {
      Thread.sleep(100L)
      waited = +100
    }
    condition.getAsBoolean
  }
}
