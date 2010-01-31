public class Port<V> {
    private V value;

    public void put(V pValue) {
        value = pValue;
    }

    public V get() {
        return value;
    }
}
