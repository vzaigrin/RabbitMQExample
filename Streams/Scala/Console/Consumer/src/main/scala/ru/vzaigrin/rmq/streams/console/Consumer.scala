package ru.vzaigrin.rmq.streams.console

import com.rabbitmq.stream.{Environment, OffsetSpecification}
import org.apache.qpid.proton.amqp.messaging.Data

object Consumer {
  def main(args: Array[String]): Unit = {
    val uri    = if (args.length > 0) args(0) else "rabbitmq-stream://user:password@rmq1:5552"
    val stream = if (args.length > 1) args(1) else "test"
    val offset = if (args.length > 2) args(2) else "last"

    // Подключаем к RabbitMQ
    // Создаём Environment
    val environment = Environment.builder().uri(uri).build()

    // Создаём Stream
    environment.streamCreator().stream(stream).create()

    // С какого смещения читать
    val offsetSpecification = {
      if (offset.eq("first")) OffsetSpecification.first()
      else if (offset.eq("last")) OffsetSpecification.last()
      else OffsetSpecification.offset(offset.toLong)
    }

    environment
      .consumerBuilder()
      .stream(stream)
      .offset(offsetSpecification)
      .messageHandler((context, message) => {
        val value = message.getBody.asInstanceOf[Data].getValue
        println(s"${context.offset}\t${value.toString}")
      })
      .build()
  }
}
