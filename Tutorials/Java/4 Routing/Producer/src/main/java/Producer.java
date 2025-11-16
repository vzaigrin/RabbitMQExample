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
        String EXCHANGE_NAME = "direct_logs";
        String severity;
        String message;

        if (argv.length < 3) {
            System.out.println("Usage: Producer hostname username password [severity] [message...]");
            System.exit(-1);
        }

        hostname = argv[0];
        username = argv[1];
        password = argv[2];

        if (argv.length > 3) severity = argv[3];
        else severity = "info";

        if (argv.length > 4) message = String.join(" ", Arrays.copyOfRange(argv, 4, argv.length));
        else message = "Hello World!";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
        }
    }
}