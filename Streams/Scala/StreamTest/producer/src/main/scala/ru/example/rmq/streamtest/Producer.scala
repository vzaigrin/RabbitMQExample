package ru.example.rmq.streamtest

import com.rabbitmq.stream.Environment
import com.typesafe.config.ConfigFactory
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import java.nio.charset.StandardCharsets

object Producer {
  def main(args: Array[String]): Unit = {
    // Читаем конфигурационный файл
    val config = ConfigFactory.load()
    val data   = config.getString("data")
    val uri    = config.getString("uri")
    val stream = config.getString("stream")

    // Читаем файл с данными
    val in      = new FileReader(data)
    val records = CSVFormat.RFC4180.withFirstRecordAsHeader.parse(in)

    // Encoder для Book
    implicit val bookEncoder: Encoder[Book] = deriveEncoder[Book]

    // Подключаем к RabbitMQ
    // Создаём Environment
    val environment = Environment.builder().uri(uri).build()

    // Создаём Stream
    environment.streamCreator().stream(stream).create()

    // Создаём Producer
    val producer = environment
      .producerBuilder()
      .name("Test")
      .stream(stream)
      .build()

    // Преобразовываем записи в JSON и отправляем в RabbitMQ
    try {
      records.forEach { r =>
        val message = producer
          .messageBuilder()
          .addData(Book(r).asJson.noSpaces.getBytes(StandardCharsets.UTF_8))
          .build()
        producer.send(message, _ => {})
      }
    } catch {
      case e: Exception =>
        System.err.println(e.getLocalizedMessage)
        sys.exit(-1)
    } finally {
      records.close()
    }

    // Пауза для отправки не полного пакета
    Thread.sleep(100)

    // Закрываем и выходим
    producer.close()
    environment.close()
    sys.exit(0)
  }
}
