import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    private static String hostname = "localhost";
    private static int port = 12345;
    private int multicastPort = 123456;
    private String userName;

    private BufferedReader in;
    private PrintWriter out;

    private String multicastAddressName = "230.1.1.1";
    private InetAddress serverInetAddress;
    private InetAddress multicastInetAddress;


    private int bufferLength = 1024;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socketTCP = new Socket(hostname, port);
            DatagramSocket socketUDP = new DatagramSocket();
            MulticastSocket socketMC = new MulticastSocket(multicastPort);
            multicastInetAddress = InetAddress.getByName(multicastAddressName);
            socketMC.joinGroup(multicastInetAddress);

            serverInetAddress = InetAddress.getByName(hostname);

            InputStream input = socketTCP.getInputStream();
            OutputStream output = socketTCP.getOutputStream();

            in = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(output, true);

            System.out.println("Connected to the chat server");

            Thread receiverTCP = new Thread(() -> {
                while (true) {
                    try {
                        String message = in.readLine();
                        System.out.println(message + " (TCP)");

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                        break;
                    }
                }
            });
            receiverTCP.start();

            Thread receiverUDP = new Thread(() -> {
                byte[] buffer = new byte[bufferLength];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    try {
                        socketUDP.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(message + " (UDP)");

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                        break;
                    }
                }
            });
            receiverUDP.start();

            Thread receiverMC = new Thread(() -> {
                byte[] buffer = new byte[bufferLength];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    try {
                        socketMC.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        String nickName = message.substring(1).split("]")[0];
                        if(!nickName.equals(userName))
                            System.out.println(message + " (MC)");

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                        break;
                    }
                }
            });
            receiverMC.start();

            Thread sender = new Thread(() -> {

                Scanner scanner = new Scanner(System.in);

                userName = scanner.nextLine();
                out.println(userName);

                byte[] helloBuffer = "!Hello".getBytes();
                DatagramPacket helloPacket = new DatagramPacket(helloBuffer, helloBuffer.length, serverInetAddress, port);
                try {
                    socketUDP.send(helloPacket);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

                String message;
                do {
                    message = scanner.nextLine();

                    if(message.startsWith("U")){
                        message = "[" + userName + "]:" + message.substring(1);
                        byte[] buffer = message.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverInetAddress, port);
                        try {
                            socketUDP.send(packet);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    else if(message.startsWith("M")){
                        message = "[" + userName + "]:" + message.substring(1);
                        byte[] buffer = message.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastInetAddress, multicastPort);
                        try {
                            socketMC.send(packet);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }else{
                        out.println(message);
                    }

                } while (!message.equals("exit"));

                try {
                    socketTCP.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            });
            sender.start();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {

        Client client = new Client(hostname, port);
        client.execute();
    }
}
