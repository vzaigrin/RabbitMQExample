import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import java.util.concurrent.atomic.AtomicLong;

public class SConsumer {
    private static String uri = "rabbitmq-stream://user:password@rmq1:5552";
    private static String stream = "test";

    public static void main(String[] args) {

        if (args.length > 0) uri = args[0];
        if (args.length > 1) stream = args[1];

        try (Environment environment = Environment.builder().uri(uri).build()) {
            environment.streamCreator().stream(stream).create();

            AtomicLong messageConsumed = new AtomicLong(0);

            // Создаём Consumer
            Consumer consumer = environment
                    .consumerBuilder()
                    .stream(stream)
                    .name("Consumer")
                    .offset(OffsetSpecification.first())
                    .autoTrackingStrategy()
                    .builder()
                    .messageHandler(
                            (ctx, msg) -> {
                                messageConsumed.incrementAndGet();
                                System.out.printf("Offset = %d\t Message = %s\n",
                                        ctx.offset(), msg.getBody().toString());
                            }
                    )
                    .build();

            System.out.printf("Consumed %d offsets", messageConsumed.get());
            consumer.close();
            System.exit(0);

        } catch (IllegalMonitorStateException e) {
            System.exit(-1);
        }
    }
}
