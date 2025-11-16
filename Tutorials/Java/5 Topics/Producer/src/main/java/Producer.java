import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Producer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String virtualHost = "/";
        String EXCHANGE_NAME = "topic_logs";
        String routingKey;
        String message;

        if (argv.length < 3) {
            System.out.println("Usage: Producer hostname username password [routingKey] [message...]");
            System.exit(-1);
        }

        hostname = argv[0];
        username = argv[1];
        password = argv[2];

        if (argv.length > 3) routingKey = argv[3];
        else routingKey = "anonymous.info";

        if (argv.length > 4) message = String.join(" ", Arrays.copyOfRange(argv, 4, argv.length));
        else message = "Hello World!";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
        }
    }
}
