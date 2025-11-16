# RabbitMQ Topics

Сообщения доставляются потребителями в соответствии с их подписками по шаблонам

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При запуске *consumer* указываются темы, из которых он получает сообщения.

## Запуск

* Терминал 1 - java -jar jars/consumer.jar rmq1 user password "#"
* Терминал 2 - java -jar jars/consumer.jar rmq1 user password kern.*"
* Терминал 3 - java -jar jars/consumer.jar rmq1 user password "*.critical"
* Терминал 4:
  * java -jar jars/producer.jar rmq1 user password info Message
  * java -jar jars/producer.jar rmq1 user password kern.critical Message
  * java -jar jars/producer.jar rmq1 user password kern Message
  * java -jar jars/producer.jar rmq1 user password kern.info Message
  * java -jar jars/producer.jar rmq1 user password log.critical Message
