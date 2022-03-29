import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Supplier extends Thread {

    private final String[] supplies;
    private final String exchangeName;

    public Supplier(String[] supplies, String exchangeName){
        this.supplies = supplies;
        this.exchangeName = exchangeName;
    }

    public void run() {

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

        // exchange, queues
        String queueName;
        try{
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
            queueName = channel.queueDeclare().getQueue();

            for(String key : supplies)
                channel.queueBind(queueName, exchangeName, key);
        }catch(IOException e){
            System.out.println("Error creating queues. " + e.getMessage());
            return;
        }

        // consumption
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        try {
            channel.basicConsume(queueName, false, consumer);
        } catch (IOException e) {
            System.out.println("Error consuming. " + e.getMessage());
        }
    }
}
