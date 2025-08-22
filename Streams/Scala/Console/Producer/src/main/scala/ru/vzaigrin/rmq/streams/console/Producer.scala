package ru.vzaigrin.rmq.streams.console

import com.rabbitmq.stream.Environment
import java.nio.charset.StandardCharsets
import scala.io.StdIn
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    val uri    = if (args.length > 0) args(0) else "rabbitmq-stream://user:password@rmq1:5552"
    val stream = if (args.length > 1) args(1) else "test"

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
            .name("ConsoleProducer")
            .stream(stream)
            .build()
        )

        print("> ")
        var line = StdIn.readLine()

        while (line.nonEmpty) {
          val message = producer
            .messageBuilder()
            .addData(line.getBytes(StandardCharsets.UTF_8))
            .build()
          producer.send(message, _ => {})

          print("> ")
          line = StdIn.readLine()
        }

        // Пауза для отправки неотправленных пакетов
        Thread.sleep(10 * 1000)
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
