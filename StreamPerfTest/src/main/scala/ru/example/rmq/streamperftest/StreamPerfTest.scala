package ru.example.rmq.streamperftest

import com.rabbitmq.stream.Environment
import java.time.Duration

object StreamPerfTest {
  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      println("Usage: StreamPergTest uri stream messageCount batchSize")
      println("\turi = rabbitmq-stream://guest:guest@localhost:5552")
      sys.exit(-1)
    }

    val uri          = args(0)
    val stream       = args(1)
    val messageCount = args(2).toInt
    val batchSize    = args(3).toInt

    // Подключаем к RabbitMQ
    // Создаём Environment
    val environment = Environment.builder().uri(uri).build()

    // Создаём Stream
    environment.streamCreator().stream(stream).create()

    // Создаём Producer
    val producer = environment
      .producerBuilder()
      .name("PerfTest")
      .stream(stream)
      .batchSize(batchSize)
      .build()

    // Получаем номер последней записи в stream
    val firstPublishingId = producer.getLastPublishingId

    // Отправляем в RabbitMQ
    val start = System.nanoTime

    (0 until messageCount) foreach { _ =>
      producer.send(
        producer
          .messageBuilder()
          .addData("1".getBytes())
          .build(),
        _ => {}
      )
    }

    val end = System.nanoTime
    println(
      s"Published ${producer.getLastPublishingId - firstPublishingId} messages by batch $batchSize in ${Duration.ofNanos(end - start).toMillis} ms"
    )

    // Закрываем и выходим
    producer.close()
    environment.close()
    sys.exit(0)
  }
}
