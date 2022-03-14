import java.net.InetAddress;

public class ClientUDP {

    public int clientPort;
    public InetAddress clientAddress;

    public ClientUDP(int clientPort, InetAddress clientAddress) {
        this.clientPort = clientPort;
        this.clientAddress = clientAddress;
    }

    @Override
    public boolean equals(Object o){
        if(this.getClass() == o.getClass()){
            ClientUDP c = (ClientUDP) o;
            return c.clientPort == this.clientPort && c.clientAddress == this.clientAddress;
        }
        return false;
    }
}
