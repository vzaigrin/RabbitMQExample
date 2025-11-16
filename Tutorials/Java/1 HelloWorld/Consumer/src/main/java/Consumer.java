import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Consumer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String QUEUE_NAME = "hello";
        String virtualHost = "/";

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 3600000);

        if (argv.length < 3) {
            System.out.println("Usage: Consumer hostname username password");
            System.exit(-1);
        }

        hostname = argv[0];
        username = argv[1];
        password = argv[2];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };

            while(true)
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        }
    }
}
