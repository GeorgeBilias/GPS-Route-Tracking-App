import java.util.ArrayList;

public class SynchronizedArrayList<T> {

    private final ArrayList<T> list;

    public SynchronizedArrayList() {
        list = new ArrayList<>();
    }

    public void add(T item) {
        synchronized (list) {
            list.add(item);
        }
    }

    public void remove(int item) {
        synchronized (list) {
            list.remove(item);
        }
    }

    public int size() {
        synchronized (list) {
            return list.size();
        }
    }

    public T get(int index) {
        synchronized (list) {
            return list.get(index);
        }
    }
}