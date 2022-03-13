import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static int port = 12345;
    private Set<String> userNames = new HashSet<>();
    private Set<ClientThread> ClientThreads = new HashSet<>();

    public Server(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server running on port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                ClientThread newUser = new ClientThread(socket, this);
                ClientThreads.add(newUser);
                newUser.start();

            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {

        Server server = new Server(port);
        server.execute();
    }

    void broadcast(String message, ClientThread sender) {
        System.out.println(">>> " + message);
        for (ClientThread clientThread : ClientThreads) {
            if (clientThread != sender) {
                clientThread.sendMessage(message);
            }
        }
    }

    void addUserName(String userName) {
        userNames.add(userName);
    }

    void removeUser(String userName, ClientThread sender) {
        userNames.remove(userName);
        ClientThreads.remove(sender);
    }

}
