import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Supplier extends Thread {

    private final String[] supplies;
    private final String exchangeName;
    private final String name;
    private int orderNo = 0;

    public Supplier(String[] supplies, String exchangeName, String name){
        this.supplies = supplies;
        for(int i = 0; i < supplies.length; i++){
            supplies[i] += ".*";
        }
        this.exchangeName = exchangeName;
        this.name = name;
    }

    public void run() {

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

        String queueName;
        try{
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);

            for(String key : supplies) {
                queueName = channel.queueDeclare(key, false, false, true, null).getQueue();
                channel.queueBind(queueName, exchangeName, key);

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(name + " received order: " + message + " order no: " + orderNo);
                        orderNo++;
                        String response = '$' + name + '-' + orderNo + '-' + message;
                        System.out.println(name + " sending order: " + response);
                        channel.basicPublish(exchangeName, response, null, response.getBytes(StandardCharsets.UTF_8));
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                };
                channel.basicConsume(queueName, false, consumer);
            }
        }catch(IOException e){
            System.out.println("Error creating queues. " + e.getMessage());
            return;
        }

        System.out.println(name + " waiting for orders...");
    }
}
