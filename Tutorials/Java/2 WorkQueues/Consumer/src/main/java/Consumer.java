import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Consumer {
    private final static String TASK_QUEUE_NAME = "task_queue";
    private final static String user = "user";
    private final static String password = "password";
    private final static String virtualHost = "/";

    public static void main(String[] argv) throws Exception {
        String host;

        if (argv.length > 0) host = argv[0];
        else host = "localhost";

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 3600000);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(1);
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, arguments);
        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            System.out.print(" [x] Received '" + message + "'");
            try {
                doWork(message);
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });

    }

    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
