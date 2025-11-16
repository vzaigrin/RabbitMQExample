import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;

public class Consumer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String EXCHANGE_NAME = "logs";
        String virtualHost = "/";

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

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };

            while(true)
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        }
    }
}
