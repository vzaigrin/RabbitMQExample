import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Producer {
    private final static String EXCHANGE_NAME = "topic_logs";
    private final static String user = "user";
    private final static String password = "password";
    private final static String virtualHost = "/";

    public static void main(String[] argv) throws Exception {
        String host;
        if (argv.length > 0) host = argv[0];
        else host = "localhost";

        String[] strings = Arrays.copyOfRange(argv, 1, argv.length);
        String routingKey = getRouting(strings);
        String message = getMessage(strings);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
        }
    }

    private static String getRouting(String[] strings) {
        if (strings.length < 1)
            return "anonymous.info";
        return strings[0];
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 2)
            return "Hello World!";
        return joinStrings(strings);
    }

    private static String joinStrings(String[] strings) {
        String delimiter = " ";
        int startIndex = 1;
        int length = strings.length;

        if (length == 0) return "";
        if (length == startIndex) return "";

        StringBuilder words = new StringBuilder(strings[startIndex]);
        for (int i = startIndex + 1; i < length; i++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}