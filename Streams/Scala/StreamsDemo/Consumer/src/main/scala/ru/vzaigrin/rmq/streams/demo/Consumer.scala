package ru.vzaigrin.rmq.streams.demo

import com.rabbitmq.stream.{Environment, OffsetSpecification}
import com.typesafe.config.ConfigFactory

object Consumer {
  def main(args: Array[String]): Unit = {
    // Читаем конфигурационный файл
    val config = ConfigFactory.load()
    val uri    = config.getString("uri")
    val stream = config.getString("stream")

    // Подключаем к RabbitMQ
    // Создаём Environment
    val environment = Environment.builder().uri(uri).build()

    // Создаём Stream
    environment.streamCreator().stream(stream).create()

    // Создаём Consumer и читаем Stream
    environment
      .consumerBuilder()
      .stream(stream)
      .offset(OffsetSpecification.first())
      .messageHandler((context, message) => {
        println(s"offset: ${context.offset()}, message: ${message.getBody.toString}") 
      })
      .build()
  }
}
