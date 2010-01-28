public class Main {
    public static void main(String[] args) {
        if (args.length == 0
                || (!"client".equals(args[0])
                    && !"server".equals(args[0]))) {
            System.out.println("first arg must be client or server");
            return;
        }

        // actual: distinguish server, client
        Connection conn = new Connection();
        Phase phase = new Phase();

        if ("client".equals(args[0])) {
            phase.clientExecute(conn);
        } else {
            phase.serverExecute(conn);
        }
    }
}
