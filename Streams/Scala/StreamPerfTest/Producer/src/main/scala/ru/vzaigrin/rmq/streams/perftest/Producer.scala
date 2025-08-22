package ru.vzaigrin.rmq.streams.perftest

import com.rabbitmq.stream.Environment
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val uri          = if (args.length > 0) args(0) else "rabbitmq-stream://user:password@rmq1:5552"
    val stream       = if (args.length > 1) args(1) else "test"
    val messageCount = if (args.length > 2) args(2).toLong else 10000L
    val messageSize  = if (args.length > 3) args(3).toInt else 1024
    val batchSize    = if (args.length > 4) args(4).toInt else 100

    val messageConfirmed   = new AtomicLong(0)
    val messageUnconfirmed = new AtomicLong(0)

    val value = Array.fill[Byte](messageSize)(0)

    Using.Manager { use =>
      try {
        // Подключаем к RabbitMQ
        // Создаём Environment
        val environment = use(Environment.builder().uri(uri).build())

        // Создаём Stream
        environment.streamCreator().stream(stream).create()

        // Создаём Producer
        val producer = use(
          environment
            .producerBuilder()
            .name("PerfTestProducer")
            .stream(stream)
            .batchSize(batchSize)
            .batchPublishingDelay(Duration.ZERO)
            .maxUnconfirmedMessages(batchSize)
            .build()
        )

        // Отправляем в RabbitMQ
        val start = System.nanoTime
        (0L until messageCount) foreach { _ =>
          val message = producer
            .messageBuilder()
            .properties()
            .messageId(UUID.randomUUID())
            .messageBuilder()
            .addData(value)
            .build()

          producer.send(
            message,
            confirmationStatus => {
              if (confirmationStatus.isConfirmed) messageConfirmed.incrementAndGet()
              else messageUnconfirmed.incrementAndGet()
            }
          )
        }
        val end      = System.nanoTime
        val duration = Duration.ofNanos(end - start).toMillis

        // Пауза для отправки неотправленных пакетов
        Thread.sleep(10 * 1000)

        // Выводим результат
        println(s"Published $messageCount messages with size $messageSize by batch $batchSize in $duration ms")
        println(s"Confirmed ${messageConfirmed.get}, Unconfirmed ${messageUnconfirmed.get}")
      } catch {
        case e: Exception =>
          val message = e.getLocalizedMessage
          if (message.nonEmpty)
            println(message)
          sys.exit(-1)
      }
    }
    sys.exit(0)
  }
}
