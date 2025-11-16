# RabbitMQ Remote procedure call (RPC)

Реализация шаблона Request/Reply

* **server** принимает запрос на вычисление числа Фибоначчи, вычисляет его и возвращает результат клиенту 
* **client** отсылает 32 запроса на вычисление чисел Фибоначчи (от 0 до 31), получает результат и печатает его

Пример использования:
* java -jar jars/server.jar rmq1 user password
* java -jar jars/client.jar rmq1 user password

При одновременном запуске нескольких *server* запросы распределяются между ними циклически.
