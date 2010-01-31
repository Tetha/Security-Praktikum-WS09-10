import java.net.Socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
public class ClientSide implements IConnection {
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private Socket socket;

    public ClientSide(Socket connection)
        throws IOException {
        System.out.println("Initializing Clientside connection");
        socket = connection;
        toServer = new ObjectOutputStream(
                                socket.getOutputStream());
        toServer.flush();
        System.out.println("toServer set");
        fromServer = new ObjectInputStream(
                                socket.getInputStream());
        System.out.println("fromClient set");
    }

    @Override
    public int receiveInteger()
        throws IOException {
        int result = fromServer.readInt();
        System.out.println("Receive Integer: " + result);
        return result;
    }

    @Override
    public void sendInteger(int value)
        throws IOException {
        System.out.println("Send Integer: " + value);
        toServer.writeInt(value);
        toServer.flush();
    }

    @Override
    public void close()
        throws IOException {
        socket.close();
    }
}
