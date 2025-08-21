import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.Producer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class SProducer {
    private static String uri = "rabbitmq-stream://user:password@rmq1:5552";
    private static String stream = "test";
    private static int messageCount = 10000;
    private static int messageSize = 1024;
    private static int batchSize = 100;

    public static void main(String[] args) {
        if (args.length > 0) uri = args[0];
        if (args.length > 1) stream = args[1];
        if (args.length > 2) messageCount = Integer.parseInt(args[2]);
        if (args.length > 3) messageSize = Integer.parseInt(args[3]);
        if (args.length > 4) batchSize = Integer.parseInt(args[4]);

        try {
            // Подключаем к RabbitMQ
            // Создаём Environment
            Environment environment = Environment.builder().uri(uri).build();

            // Создаём Stream
            environment.streamCreator().stream(stream).create();

            // Создаём Producer
            Producer producer = environment
                    .producerBuilder()
                    .name("PerfTest")
                    .stream(stream)
                    .batchSize(batchSize)
                    .build();

            byte[] value = new byte[messageSize];
            Message message = producer.messageBuilder()
                    .addData(value)
                    .build();

            // Отправляем в RabbitMQ
            long start = System.nanoTime();

            CountDownLatch confirmLatch = new CountDownLatch(messageCount);
            IntStream.range(0, messageCount).forEach(i -> {
                // send one message
                producer.send(message, confirmationStatus -> confirmLatch.countDown());
            });
            confirmLatch.await(1, TimeUnit.MINUTES);

            long end = System.nanoTime();
            long duration = Duration.ofNanos(end - start).toMillis();

            // Выводим результат
            System.out.printf("Published %d messages with size %d by batch %d in %d ms", messageCount, messageSize, batchSize, duration);
            producer.wait();

            // Закрываем и выходим
            producer.close();
            environment.close();
            System.exit(0);

        } catch (InterruptedException | IllegalMonitorStateException e) {
            System.exit(-1);
        }
    }
}
