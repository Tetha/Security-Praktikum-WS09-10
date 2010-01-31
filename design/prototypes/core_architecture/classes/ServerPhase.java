public class ServerPhase {
    public void execute(Connection conn) {
        conn.sendInteger(42);
        int foo = conn.receiveInteger();
    }
}
