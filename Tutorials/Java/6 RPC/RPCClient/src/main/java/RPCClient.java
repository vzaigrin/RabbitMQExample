import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;

public class RPCClient implements AutoCloseable {
    private static Connection connection;
    private static Channel channel;
    private final static String requestQueueName = "rpc_queue";

    public static void main(String[] argv) throws Exception {
        String hostname;
        String username;
        String password;
        String virtualHost = "/";

        if (argv.length < 3) {
            System.out.println("Usage: RPCClient hostname username password");
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

        connection = factory.newConnection();
        channel = connection.createChannel();

        try (RPCClient fibonacciRpc = new RPCClient()) {
            for (int i = 0; i < 31; i++) {
                String i_str = Integer.toString(i);
                System.out.print(" [x] Requesting fib(" + i_str + ") ..");
                String response = fibonacciRpc.call(i_str);
                System.out.println(" Got '" + response + "'");
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public String call(String message) throws IOException, InterruptedException, ExecutionException {
        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes(StandardCharsets.UTF_8));

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {});

        String result = response.get();
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }
}
