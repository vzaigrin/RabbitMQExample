// Copyright (c) 2020-2025 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

// tag::sample-imports[]
import com.rabbitmq.stream.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
// end::sample-imports[]

public class SampleApplication {
    private static String uri = "rabbitmq-stream://user:password@rmq1:5552";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) uri = args[0];

        // tag::sample-environment[]
        System.out.println("Connecting...");
        Environment environment = Environment.builder().uri(uri).build();  // <1>

        String stream = UUID.randomUUID().toString();
        environment.streamCreator().stream(stream).create();  // <2>
        // end::sample-environment[]

        // tag::sample-publisher[]
        System.out.println("Starting publishing...");
        int messageCount = 10000;

        CountDownLatch publishConfirmLatch = new CountDownLatch(messageCount);
        Producer producer = environment.producerBuilder()  // <1>
                .stream(stream)
                .build();

        IntStream.range(0, messageCount)
                .forEach(i -> producer.send(  // <2>
                        producer.messageBuilder()                    // <3>
                                .addData(String.valueOf(i).getBytes())   // <3>
                                .build(),                                // <3>
                        confirmationStatus -> publishConfirmLatch.countDown()  // <4>
                ));
        publishConfirmLatch.await(10, TimeUnit.SECONDS);  // <5>
        producer.close();  // <6>
        System.out.printf("Published %,d messages%n", messageCount);
        // end::sample-publisher[]

        // tag::sample-consumer[]
        System.out.println("Starting consuming...");
        AtomicLong sum = new AtomicLong(0);

        CountDownLatch consumeLatch = new CountDownLatch(messageCount);
        Consumer consumer = environment.consumerBuilder()  // <1>
                .stream(stream)
                .offset(OffsetSpecification.first()) // <2>
                .messageHandler((offset, message) -> {  // <3>
                    sum.addAndGet(Long.parseLong(new String(message.getBodyAsBinary())));  // <4>
                    consumeLatch.countDown();  // <5>
                })
                .build();

        consumeLatch.await(10, TimeUnit.SECONDS);  // <6>

        System.out.println("Sum: " + sum.get());  // <7>
        consumer.close();  // <8>
        // end::sample-consumer[]

        // tag::sample-environment-close[]
        environment.deleteStream(stream);  // <1>
        environment.close();  // <2>
        // end::sample-environment-close[]
    }
}