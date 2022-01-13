# RabbitMQ Topics

Сообщения доставляются потребителями в соответствии с их подписками по шаблонам

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При запуске *consumer* указываются темы, из которых он получает сообщения.

## Запуск

* Терминал 1 - *java -jar consumer.jar "#"*
* Терминал 2 - *java -jar consumer.jar "kern.\*"*
* Терминал 3 - *java -jar consumer.jar "kern.\*" "\*.critical"*
* Терминал 4:
  * *java -jar producer.jar info Message*
  * *java -jar producer.jar kern.critical Message*
  * *java -jar producer.jar kern Message*
  * *java -jar producer.jar kern.info Message*
  * *java -jar producer.jar log.critical Message*
