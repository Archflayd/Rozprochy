import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String hostname = "localhost";
    private static int port = 12345;
    private String userName;

    private BufferedReader in;
    private PrintWriter out;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            in = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(output, true);

            System.out.println("Connected to the chat server");

            Thread receiver = new Thread(() -> {
                while (true) {
                    try {
                        String response = in.readLine();
                        System.out.println(response);

                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                        break;
                    }
                }
            });
            receiver.start();

            Thread sender = new Thread(() -> {

                Scanner scanner = new Scanner(System.in);

                userName = scanner.nextLine();
                out.println(userName);

                String message;
                do {
                    message = scanner.nextLine();
                    out.println(message);

                } while (!message.equals("exit"));

                try {
                    socket.close();
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
