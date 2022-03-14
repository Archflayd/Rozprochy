import java.io.*;
import java.net.Socket;

public class ClientThreadTCP extends Thread {
    private Socket socket;
    private Server server;

    private BufferedReader in;
    private PrintWriter out;

    public ClientThreadTCP(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            in = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(output, true);

            out.println("Enter your username: ");
            String userName = in.readLine();
            server.addUserName(userName);

            server.broadcastTCP("[Server]: " + userName + " connected to the chat", this);

            String message;

            while(true){
                message = in.readLine();
                if(message.equals("exit"))
                    break;
                server.broadcastTCP("[" + userName + "]: " + message, this);
            }

            server.removeUser(userName, this);
            socket.close();

            server.broadcastTCP("[Server]: " + userName + " left the chat", this);

        } catch (IOException ex) {
            System.out.println("Client TCP left: " + ex.getMessage());
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }
}
