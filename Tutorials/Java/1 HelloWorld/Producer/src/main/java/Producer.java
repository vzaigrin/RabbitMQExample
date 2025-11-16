import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Producer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String message ;
        String QUEUE_NAME = "hello";
        String virtualHost = "/";

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 3600000);

        if (argv.length < 3) {
            System.out.println("Usage: Producer hostname username password [message]");
            System.exit(-1);
        }

        hostname = argv[0];
        username = argv[1];
        password = argv[2];

        if (argv.length > 3) message = argv[3];
        else message = "Hello World!";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
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
