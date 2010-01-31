public class Main {
    public static void main(String[] args) {
        if (args.length == 0
                || (!"client".equals(args[0])
                    && !"server".equals(args[0]))) {
            System.out.println("first arg must be client or server");
            return;
        }

        if ("client".equals(args[0])) {
            Connection conn = new Connection(); // actual: client connection
            ClientPhase phase = new ClientPhase();
            phase.execute(conn);
        } else {
            Connection conn = new Connection(); // actual: server connection
            ServerPhase phase = new ServerPhase();
            phase.execute(conn);
        }
    }
}
