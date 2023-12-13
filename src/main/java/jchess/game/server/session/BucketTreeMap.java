package jchess.game.server.session;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * A TreeMap that can store multiple values for any given key
 */
public class BucketTreeMap<K, V> {

    private final TreeMap<K, Set<V>> treeMap = new TreeMap<>();

    public boolean remove(K key, V value) {
        Set<V> bucket = treeMap.get(key);
        if (bucket == null || !bucket.remove(value)) {
            return false;
        }

        if (bucket.isEmpty()) {
            treeMap.remove(key);
        }

        return true;
    }

    public void put(K key, V value) {
        Set<V> bucket = treeMap.computeIfAbsent(key, k -> new HashSet<>());
        bucket.add(value);
    }

    public Set<V> get(K key) {
        return treeMap.get(key);
    }

    public TreeMap<K, Set<V>> getUnderlyingMap() {
        return treeMap;
    }
}
