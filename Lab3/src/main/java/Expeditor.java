import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Expeditor {

    private static final String exchangeName = "EX4";

    public static void main(String[] args) {

        Random random = new Random();
        int id = random.nextInt(1000, 9999);
        System.out.println("Expeditor id " + id);

        // connection & channel
        Channel channel;
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.basicQos(1);
        }catch(IOException | TimeoutException e){
            System.out.println("Error establishing a connection. " + e.getMessage());
            return;
        }

        // exchange
        try {
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);

            String key = "*." + String.valueOf(id);
            String queueName = channel.queueDeclare(key, false, false, true, null).getQueue();
            channel.queueBind(queueName, exchangeName, key);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    if(message.startsWith("$"))
                        System.out.println("Order finished: " + message.substring(1));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queueName, false, consumer);
        } catch (IOException e) {
            System.out.println("Error creating exchange. " + e.getMessage());
            return;
        }

        while (true) {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter order: ");
            String order;
            try {
                order = br.readLine();
            } catch (IOException e) {
                System.out.println("Error reading from user. " + e.getMessage());
                return;
            }

            if ("exit".equals(order)) {
                break;
            }

            order = order + '.' + id;
            try {
                channel.basicPublish(exchangeName, order, null, order.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Error sending the message. " + e.getMessage());
            }
            System.out.println("Sent: " + order);
        }
    }
}
