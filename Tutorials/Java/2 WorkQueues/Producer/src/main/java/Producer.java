import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Producer {
    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String message ;
        String QUEUE_NAME = "task_queue";
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

        if (argv.length > 3) message = String.join(" ", Arrays.copyOfRange(argv, 3, argv.length));
        else message = "Message";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
