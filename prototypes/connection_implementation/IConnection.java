import java.io.IOException;

public interface IConnection {
    int receiveInteger() throws IOException;
    void sendInteger(int value) throws IOException;
    void close() throws IOException;
}
