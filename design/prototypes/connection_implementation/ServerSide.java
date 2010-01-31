import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
public class ServerSide implements IConnection {
    private ObjectInputStream fromClient;
    private ObjectOutputStream toClient;
    private Socket clientSocket;
    private ServerSocket socket;

    public ServerSide(ServerSocket pSocket)
        throws IOException {
        System.out.println("Initializing Serverside connection");
        socket = pSocket;
        clientSocket = socket.accept();
        System.out.println("Connection construction: client connected");
        toClient = new ObjectOutputStream(
                            clientSocket.getOutputStream());
        toClient.flush();
        System.out.println("toClient set");
        fromClient = new ObjectInputStream(
                            clientSocket.getInputStream());
        System.out.println("fromClient set");
    }

    @Override
    public int receiveInteger()
        throws IOException {
        int result = fromClient.readInt();
        System.out.println("Receive Integer: " + result);
        return result;
    }

    @Override
    public void sendInteger(int value)
        throws IOException {
        System.out.println("Send Integer: " + value);
        toClient.writeInt(value);
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
        socket.close();
    }
}
