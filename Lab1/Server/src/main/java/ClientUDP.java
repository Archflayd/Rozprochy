import java.net.InetAddress;

public class ClientUDP {

    public int clientPort;
    public InetAddress clientAddress;

    public ClientUDP(int clientPort, InetAddress clientAddress) {
        this.clientPort = clientPort;
        this.clientAddress = clientAddress;
    }

}
