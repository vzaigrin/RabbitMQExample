# RabbitMQ Work queues

Распределение сообщений между потребителями

* **producer** отправляет одно сообщение в RabbitMQ
* **consumer** читает и выводит сообщения из RabbitMQ

При одновременном запуске нескольких *consumer* сообщения распределяются между ними циклически.