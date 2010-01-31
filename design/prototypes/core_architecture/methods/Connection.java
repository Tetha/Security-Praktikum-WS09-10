public class Connection {
    public int receiveInteger() {
        System.out.println("receive -> 84");
        return 84;
    }
    public void sendInteger(int i) {
        System.out.println("send -> " + i);
    }
}
