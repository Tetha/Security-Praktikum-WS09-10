public class ClientPhase {
    public void execute(Connection conn) {
        int foo = conn.receiveInteger();
        foo *= 2;
        conn.sendInteger(foo);
    }
}
