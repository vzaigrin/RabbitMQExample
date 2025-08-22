package ru.vzaigrin.rmq.streams.perftest

import com.rabbitmq.stream.{Environment, OffsetSpecification}
import java.time.Duration
import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicLong
import scala.util.Using

object Consumer {
  def main(args: Array[String]): Unit = {
    val uri          = if (args.length > 0) args(0) else "rabbitmq-stream://user:password@rmq1:5552"
    val stream       = if (args.length > 1) args(1) else "test"
    val messageCount = if (args.length > 2) args(2).toInt else 10000

    val messageConsumed = new AtomicLong(0)
    val confirmLatch    = new CountDownLatch(messageCount)

    Using.Manager { use =>
      try {
        // Подключаем к RabbitMQ
        // Создаём Environment
        val environment = use(Environment.builder().uri(uri).build())

        // Создаём Stream
        environment.streamCreator().stream(stream).create()

        // Создаём Consumer и получаем сообщения
        val start = System.nanoTime

        val consumer = environment
          .consumerBuilder()
          .stream(stream)
          .offset(OffsetSpecification.first())
          .messageHandler((_, _) => {
            messageConsumed.incrementAndGet()
            confirmLatch.countDown()
          })
          .build()

        confirmLatch.await(1, TimeUnit.MINUTES)
        val end      = System.nanoTime
        val duration = Duration.ofNanos(end - start).toMillis

        // Выводим результат
        println(s"Consumed $messageConsumed messages in $duration ms")

        // Закрываем и выходим
        consumer.close()
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
