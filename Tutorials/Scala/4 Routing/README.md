# RabbitMQ Routing

Сообщения доставляются потребителями в соответствии с их подписками

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При запуске *consumer* указываются темы, из которых он получает сообщения.

## Запуск

* Терминал 1 - java -jar jars/consumer.jar rmq1 username password error
* Терминал 2 - java -jar jars/consumer.jar rmq1 username password info warning
* Терминал 3:
  * java -jar jars/producer.jar rmq1 username password error Message
  * java -jar jars/producer.jar rmq1 username password info Message
  * java -jar jars/producer.jar rmq1 username password warning Message
