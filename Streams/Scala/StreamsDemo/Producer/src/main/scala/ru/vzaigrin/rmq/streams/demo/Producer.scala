package ru.vzaigrin.rmq.streams.demo

import com.rabbitmq.stream.Environment
import com.typesafe.config.ConfigFactory
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import java.nio.charset.StandardCharsets
import scala.util.Using

object Producer {
  def main(args: Array[String]): Unit = {
    // Читаем конфигурационный файл
    val config = ConfigFactory.load()
    val data   = config.getString("data")
    val uri    = config.getString("uri")
    val stream = config.getString("stream")

    // Encoder для Book
    implicit val bookEncoder: Encoder[Book] = deriveEncoder[Book]

    Using.Manager { use =>
      try {
        // Читаем файл с данными
        val in        = new FileReader(data)
        val csvFormat = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get()
        val records   = use(csvFormat.parse(in))

        // Подключаем к RabbitMQ
        // Создаём Environment
        val environment = use(Environment.builder().uri(uri).build())

        // Создаём Stream
        environment.streamCreator().stream(stream).create()

        // Создаём Producer
        val producer = use(
          environment
            .producerBuilder()
            .name("DemoProducer")
            .stream(stream)
            .build()
        )

        // Преобразовываем записи в JSON и отправляем в RabbitMQ
        records.forEach { r =>
          val message = producer
            .messageBuilder()
            .addData(Book(r).asJson.noSpaces.getBytes(StandardCharsets.UTF_8))
            .build()
          producer.send(message, _ => {})
        }

        // Пауза для отправки неотправленных пакетов
        Thread.sleep(100)
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
