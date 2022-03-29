import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Expeditor extends Thread{

    private final String exchangeName;

    public Expeditor(String exchangeName){
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

        // exchange
        try {
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
        } catch (IOException e) {
            System.out.println("Error creating exchange. " + e.getMessage());
            return;
        }

        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter order: ");
            String order;
            try {
                order = br.readLine();
            } catch (IOException e) {
                System.out.println("Error reading from user. " + e.getMessage());
                return;
            }

            // break condition
            if ("exit".equals(order)) {
                break;
            }

            // publish
            try {
                channel.basicPublish(exchangeName, order, null, order.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Error sending the message. " + e.getMessage());
            }
            System.out.println("Sent: " + order);
        }
    }
}
