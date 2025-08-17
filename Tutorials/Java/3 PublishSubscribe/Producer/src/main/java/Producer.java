import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Producer {
    private final static String EXCHANGE_NAME   = "logs";
    private final static String user            = "user";
    private final static String password        = "password";
    private final static String virtualHost     = "/";

    public static void main(String[] argv) throws Exception {
        String host;
        String message;

        if (argv.length > 0) host = argv[0];
        else host = "localhost";

        if (argv.length < 2) message = "info: Hello World!";
        else message = String.join(" ", Arrays.copyOfRange(argv, 1, argv.length));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
