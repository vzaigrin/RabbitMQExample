import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.Producer;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
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

        AtomicLong messageConfirmed   = new AtomicLong(0);
        AtomicLong messageUnconfirmed = new AtomicLong(0);

        byte[] value = new byte[messageSize];

        try (Environment environment = Environment.builder().uri(uri).build()) {
            // Создаём Stream
            environment.streamCreator().stream(stream).create();

            // Создаём Producer
            Producer producer = environment
                    .producerBuilder()
                    .stream(stream)
                    .name("PerfTestProducer")
                    .batchSize(batchSize)
                    .batchPublishingDelay(Duration.ZERO)
                    .maxUnconfirmedMessages(batchSize)
                    .build();

            // Отправляем в RabbitMQ
            long start = System.nanoTime();

            IntStream.range(0, messageCount).forEach(i -> {
                Message message = producer
                        .messageBuilder()
                        .properties()
                        .messageId(UUID.randomUUID())
                        .messageBuilder()
                        .addData(value)
                        .build();

                producer.send(message,
                        confirmationStatus -> {
                        if (confirmationStatus.isConfirmed()) messageConfirmed.incrementAndGet();
                        else messageUnconfirmed.incrementAndGet();
                    });
                }
            );

            long end = System.nanoTime();
            long duration = Duration.ofNanos(end - start).toMillis();

            // Выводим результат
            System.out.printf("Published %d messages with size %d by batch %d in %d ms\n", messageCount, messageSize, batchSize, duration);
            System.out.printf("Confirmed %d, Unconfirmed %d\n", messageConfirmed.get(), messageUnconfirmed.get());

            // Закрываем и выходим
            producer.close();
        } catch (IllegalMonitorStateException e) {
            System.exit(-1);
        }
        System.exit(0);
    }
}
