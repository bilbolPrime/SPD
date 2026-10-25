package android.util;

import java.util.ArrayList;
import java.util.TreeMap;

public class SparseArray<E> {

    private final TreeMap<Integer, E> values = new TreeMap<Integer, E>();

    public SparseArray() {
    }

    public SparseArray(int initialCapacity) {
    }

    public E get(int key) {
        return values.get(key);
    }

    public E get(int key, E valueIfKeyNotFound) {
        E value = values.get(key);
        return value != null || values.containsKey(key) ? value : valueIfKeyNotFound;
    }

    public void put(int key, E value) {
        values.put(key, value);
    }

    public void append(int key, E value) {
        put(key, value);
    }

    public void remove(int key) {
        values.remove(key);
    }

    public void delete(int key) {
        remove(key);
    }

    public void removeAt(int index) {
        values.remove(keyAt(index));
    }

    public void clear() {
        values.clear();
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(int key) {
        return values.containsKey(key);
    }

    public int keyAt(int index) {
        return new ArrayList<Integer>(values.keySet()).get(index);
    }

    public E valueAt(int index) {
        return new ArrayList<E>(values.values()).get(index);
    }

    public int indexOfKey(int key) {
        int index = 0;
        for (Integer current : values.keySet()) {
            if (current == key) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
