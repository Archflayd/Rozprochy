import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    private static int port = 12345;
    private Set<String> userNames = new HashSet<>();
    private Set<ClientThreadTCP> clientThreadsTCP = new HashSet<>();
    public Set<ClientUDP> clientsUDP = new HashSet<>();

    public Server(int port) {
        this.port = port;
    }

    public void execute() throws SocketException {

            Thread listenerTCP = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {

                    System.out.println("Server running on port: " + port);

                    while (true) {
                        Socket socket = serverSocket.accept();
                        ClientThreadTCP newUser = new ClientThreadTCP(socket, this);
                        clientThreadsTCP.add(newUser);
                        newUser.start();
                    }

                } catch (IOException ex) {
                    System.out.println("Server tcp listener error: " + ex.getMessage());
                }
            });
            listenerTCP.start();


        DatagramSocket socketUDP = new DatagramSocket(port);

        ClientsThreadUDP clientsThreadUDP = new ClientsThreadUDP(socketUDP, this);
        clientsThreadUDP.start();
    }

    public static void main(String[] args) throws SocketException {

        Server server = new Server(port);
        server.execute();
    }

    void broadcastTCP(String message, ClientThreadTCP sender) {
        System.out.println(message + " (TCP)");
        for (ClientThreadTCP clientThreadTCP : clientThreadsTCP) {
            if (clientThreadTCP != sender) {
                clientThreadTCP.sendMessage(message);
            }
        }
    }

    void addUserName(String userName) {
        userNames.add(userName);
    }

    void removeUser(String userName, ClientThreadTCP sender) {
        userNames.remove(userName);
        clientThreadsTCP.remove(sender);
    }

}
