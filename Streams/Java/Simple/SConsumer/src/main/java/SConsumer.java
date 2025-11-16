import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SConsumer {
    private static String uri = "rabbitmq-stream://user:password@rmq1:5552";
    private static String stream = "test";
    private static int messageCount = 20000;

    public static void main(String[] args) {
        if (args.length > 0) uri = args[0];
        if (args.length > 1) stream = args[1];
        if (args.length > 2) messageCount = Integer.parseInt(args[2]);

        try (Environment environment = Environment.builder().uri(uri).build()) {
            environment.streamCreator().stream(stream).create();

            AtomicLong messageConsumed = new AtomicLong(0);
            CountDownLatch confirmLatch = new CountDownLatch(messageCount);

            // Создаём Consumer
            Consumer consumer = environment
                    .consumerBuilder()
                    .stream(stream)
                    .offset(OffsetSpecification.first())
                    .messageHandler(
                            (ctx, msg) -> {
                                messageConsumed.incrementAndGet();
                                confirmLatch.countDown();
                            }
                    )
                    .build();

            confirmLatch.await(5, TimeUnit.MINUTES);
            System.out.printf("Consumed %d offsets", messageConsumed.get());
            consumer.close();
            System.exit(0);
        } catch (InterruptedException | IllegalMonitorStateException e) {
            System.exit(-1);
        }
    }
}
