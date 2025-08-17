# RabbitMQ Work queues

Распределение сообщений между потребителями

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

Пример использования:
* Отправляем сообщение в RabbitMQ на хост rmq1:
  java -jar jars/producer.jar rmq1
* Получаем сообщения с хоста rmq1:
  java -jar jars/consumer.jar rmq1

При одновременном запуске нескольких *consumer* сообщения распределяются между ними циклически.
