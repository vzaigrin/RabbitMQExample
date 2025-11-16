import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Consumer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String EXCHANGE_NAME = "topic_logs";
        String virtualHost = "/";

        if (argv.length < 4) {
            System.err.println("Usage: Consumer host username password binding_key [binding_key...]");
            System.exit(1);
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

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            String queueName = channel.queueDeclare().getQueue();

            for (String bindingKey : Arrays.copyOfRange(argv, 3, argv.length)) {
                channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
            }
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };

            while(true)
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        }
    }
}