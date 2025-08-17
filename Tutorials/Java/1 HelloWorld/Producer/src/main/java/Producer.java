import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Producer {
    private final static String QUEUE_NAME = "hello";
    private final static String user        = "user";
    private final static String password    = "password";
    private final static String virtualHost = "/";

    public static void main(String[] argv) throws Exception {
        String host;
        String message ;

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 3600000);

        if (argv.length > 0) host = argv[0];
        else host = "localhost";

        if (argv.length > 1) message = argv[1];
        else message = "Hello World!";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
