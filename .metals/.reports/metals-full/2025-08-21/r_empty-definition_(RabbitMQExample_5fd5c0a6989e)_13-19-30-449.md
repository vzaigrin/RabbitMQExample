file://<WORKSPACE>/Streams/Scala/StreamPerfTest/src/main/scala/ru/example/rmq/streamperftest/StreamPerfTest.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:
	 -stream.
	 -stream#
	 -stream().
	 -scala/Predef.stream.
	 -scala/Predef.stream#
	 -scala/Predef.stream().
offset: 736
uri: file://<WORKSPACE>/Streams/Scala/StreamPerfTest/src/main/scala/ru/example/rmq/streamperftest/StreamPerfTest.scala
text:
```scala
package ru.example.rmq.streamperftest

import com.rabbitmq.stream.Environment
import java.time.Duration

object StreamPerfTest {
  def main(args: Array[String]): Unit = {
    if (args.length != 5) {
      println("Usage: StreamPergTest uri stream messageCount messageSize batchSize")
      println("\turi = rabbitmq-stream://guest:guest@localhost:5552")
      sys.exit(-1)
    }

    val uri          = args(0)
    val stream       = args(1)
    val messageCount = args(2).toLong
    val messageSize  = args(3).toInt
    val batchSize    = args(4).toInt

    // Подключаем к RabbitMQ
    // Создаём Environment
    val environment = Environment.builder().uri(uri).build()

    // Создаём Stream
    environment.streamCreator().stream(st@@ream).create()

    // Создаём Producer
    val producer = environment
      .producerBuilder()
      .name("PerfTest")
      .stream(stream)
      .batchSize(batchSize)
      .build()

    // Создаём сообщение, которое будем отправлять
    val value = Array.fill[Byte](messageSize)(0)

    // Отправляем в RabbitMQ
    val start = System.nanoTime
    (0L until messageCount) foreach { _ =>
      producer.send(
        producer
          .messageBuilder()
          .addData(value)
          .build(),
        _ => {}
      )
    }
    val end = System.nanoTime

    // Выводим результат
    println(
      s"Published $messageCount messages with size $messageSize by batch $batchSize in ${Duration.ofNanos(end - start).toMillis} ms"
    )

    // Закрываем и выходим
    producer.close()
    environment.close()
    sys.exit(0)
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 