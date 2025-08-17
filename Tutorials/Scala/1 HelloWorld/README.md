# RabbitMQ Hello World

Простое приложение для RabbitMQ

* **producer** отправляет одно сообщение в RabbitMQ в очередь 'hello'
* **consumer** читает и выводит сообщения из RabbitMQ из очереди 'hello'

Пример использования:
* Отправляем сообщение "Hello World!" в RabbitMQ на хост rmq1:
java -jar jars/producer.jar rmq1
* Получаем сообщения с хоста rmq1:
java -jar jars/consumer.jar rmq1
