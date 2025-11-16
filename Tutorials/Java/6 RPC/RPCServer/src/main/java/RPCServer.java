import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RPCServer {
    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String virtualHost = "/";
        String RPC_QUEUE_NAME = "rpc_queue";

        if (argv.length < 3) {
            System.out.println("Usage: RPCServer hostname username password");
            System.exit(-1);
        }

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 3600000);

        hostname = argv[0];
        username = argv[1];
        password = argv[2];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, arguments);
        channel.queuePurge(RPC_QUEUE_NAME);
        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                int n = Integer.parseInt(message);

                System.out.println(" [.] fib(" + message + ")");
                response += fib(n);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e.getLocalizedMessage());
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
        }));
    }
}
