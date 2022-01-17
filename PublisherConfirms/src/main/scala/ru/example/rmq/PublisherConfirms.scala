package ru.example.rmq

import com.rabbitmq.client.{ConfirmCallback, _}
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.BooleanSupplier
import scala.util.Using

object PublisherConfirms {
  val MESSAGE_COUNT = 50_000

  def main(args: Array[String]): Unit = {
    val cf = new ConnectionFactory
    cf.setHost("localhost")
    cf.setUsername("guest")
    cf.setPassword("guest")
    cf.newConnection

    Using.Manager { use =>
      val connection = use(cf.newConnection)
      val ch         = use(connection.createChannel)
      ch.confirmSelect

      publishMessagesIndividually(ch)
      publishMessagesInBatch(ch)
      handlePublishConfirmsAsynchronously(ch)
    }
    sys.exit(0)
  }

  def publishMessagesIndividually(ch: Channel): Unit = {
    val queue = UUID.randomUUID.toString
    ch.queueDeclare(queue, false, false, true, null)
    val start = System.nanoTime

    (0 until MESSAGE_COUNT).foreach { i =>
      ch.basicPublish("", queue, null, i.toString.getBytes)
      ch.waitForConfirmsOrDie(5_000)
    }

    val end = System.nanoTime
    println(s"Published $MESSAGE_COUNT messages individually in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  def publishMessagesInBatch(ch: Channel): Unit = {
    val queue = UUID.randomUUID.toString
    ch.queueDeclare(queue, false, false, true, null)

    val batchSize = 100
    val quotient  = math.ceil(MESSAGE_COUNT / batchSize).toInt
    val remainder = MESSAGE_COUNT % batchSize
    val batchList = (0 until batchSize).toList
    val start     = System.nanoTime

    (0 until MESSAGE_COUNT)
      .zip((0 until quotient).toList.flatMap(_ => batchList) ::: batchList.slice(0, remainder))
      .foreach { ij =>
        ch.basicPublish("", queue, null, ij._1.toString.getBytes)
        if (ij._2 == batchSize) ch.waitForConfirmsOrDie(5_000)
      }
    if (remainder > 0) ch.waitForConfirmsOrDie(5_000)

    val end = System.nanoTime
    println(s"Published $MESSAGE_COUNT messages in batch in ${Duration.ofNanos(end - start).toMillis} ms")
  }

  def handlePublishConfirmsAsynchronously(ch: Channel): Unit = {
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

    (0 until MESSAGE_COUNT).foreach { i =>
      val body = String.valueOf(i)
      outstandingConfirms.put(ch.getNextPublishSeqNo, body)
      ch.basicPublish("", queue, null, body.getBytes)
    }

    if (!waitUntil(Duration.ofSeconds(60), () => outstandingConfirms.isEmpty))
      throw new IllegalStateException("All messages could not be confirmed in 60 seconds")

    val end = System.nanoTime
    println(
      s"Published $MESSAGE_COUNT messages and handled confirms asynchronously in ${Duration.ofNanos(end - start).toMillis} ms"
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
