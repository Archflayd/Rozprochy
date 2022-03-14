import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ClientsThreadUDP extends Thread {
    private DatagramSocket socketUDP;
    private Server server;
    private int bufferLength = 1024;

    public ClientsThreadUDP(DatagramSocket socketUDP, Server server) {
        this.socketUDP = socketUDP;
        this.server = server;
    }

    @Override
    public void run() {
        while (true){
            try {
                byte[] receiveBuffer = new byte[bufferLength];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socketUDP.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                System.out.println(message + " (UDP)");

                ClientUDP clientUDP = new ClientUDP(clientPort, clientAddress);

                    if(message.startsWith("!Hello")){
                        server.clientsUDP.add(clientUDP);
                        continue;
                    }

                    for (ClientUDP client : server.clientsUDP) {
                        if (client.clientPort != clientPort || !client.clientAddress.equals(clientAddress)) {
                            sendMessage(message, client);
                        }
                    }

            }
            catch (IOException ex) {
                System.out.println("Clients UPD thread error: " + ex.getMessage());
                break;
            }

        }
        socketUDP.close();
    }

    private void sendMessage(String message, ClientUDP client) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, client.clientAddress, client.clientPort);
        socketUDP.send(sendPacket);
    }

}
