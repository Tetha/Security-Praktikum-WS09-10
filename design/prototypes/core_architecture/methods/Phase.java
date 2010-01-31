public class Phase {
    public void clientExecute(Connection conn) {
        int foo = conn.receiveInteger();
        foo *= 2;
        conn.sendInteger(foo);
    }

    public void serverExecute(Connection conn) {
        conn.sendInteger(42);
        int foo = conn.receiveInteger();
    }
}
