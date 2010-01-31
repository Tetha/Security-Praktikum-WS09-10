import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1
            || (!"client".equals(args[0])
                && !"server".equals(args[0]))) {
            System.out.println("First arg must be client or server");
            return;
        }

        if ("client".equals(args[0])) {
            System.out.println("Client start");
            Socket clientSocket = new Socket("localhost", 31337);
            IConnection conn = new ClientSide(clientSocket);
            /* Phase stuff */
            System.out.println("client enters phase");
            int foo = conn.receiveInteger();
            conn.sendInteger(foo*2);
            System.out.println("client leaves phase");
            /* End of Phase stuff */
            conn.close();
            System.out.println("Client end");
        } else {
            System.out.println("Server start");
            ServerSocket serverSocket = new ServerSocket(31337);
            IConnection conn = new ServerSide(serverSocket);
            /* Phase stuff */
            System.out.println("server enters phase");
            conn.sendInteger(42);
            int result = conn.receiveInteger();
            System.out.println("Overall result at server: " + result);
            /* End of Phase stuff */
            conn.close();
            System.out.println("Server end");
        }
    }
}
