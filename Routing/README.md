# RabbitMQ Routing

Сообщения доставляются потребителями в соответствии с их подписками

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При запуске *consumer* указываются темы, из которых он получает сообщения.

## Запуск

* Терминал 1 - *java -jar consumer.jar <host> error*
* Терминал 2 - *java -jar consumer.jar <host> info warning*
* Терминал 3:
  * *java -jar producer.jar <host> error Message*
  * *java -jar producer.jar <host> info Message*
  * *java -jar producer.jar <host> warning Message*
