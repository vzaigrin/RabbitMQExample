# RabbitMQ Routing

Сообщения доставляются потребителями в соответствии с их подписками

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При запуске *consumer* указываются темы, из которых он получает сообщения.

## Запуск

* Терминал 1 - java -jar jars/consumer.jar rmq1 error
* Терминал 2 - java -jar jars/consumer.jar rmq1 info warning
* Терминал 3:
  * java -jar jars/producer.jar rmq1 error Message
  * java -jar jars/producer.jar rmq1 info Message
  * java -jar jars/producer.jar rmq1 warning Message
