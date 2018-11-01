package orchi.HHCloud.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementacion de cache LRU (least recently used) (menos usado recientemente)
 * <pre>
 *  max cache size = 3
 *  put 1
 *      | 1 |
 *  put 2
 *      | 2 | 1 |
 *  put 3
 *      | 3 | 2 | 1 |
 *  put 4
 *      | 4 | 3 | 2 |
 *
 *      -- el elemento (1) se elimina de el cache
 *  put 3
 *      | 3 | 4 | 2 |
 *
 *      -- si el elemento ya existe se coloca de primero en el cache
 *  put 5
 *      | 5 | 3 | 4 |
 *
 *  get 3
 *      | 3 | 5 | 4 |
 * </pre>
 */
public class LRUCache<K, V> implements Cache<K, V> {
    Deque<Node<K, V>> dq = new LinkedList<>();
    Map<K, Node<K, V>> map = new ConcurrentHashMap<>();
    private Logger log = LoggerFactory.getLogger(LRUCache.class);
    private long SIZE;
    private String name;
    private Lock lock = new ReentrantLock();


    public LRUCache(String name) {
        this.name = name;
        SIZE = 20;
    }


    public LRUCache(String name, long size) {
        this.name = name;
        SIZE = size;
    }

    @Override
    public V remove(K key) {
        V r = null;
        lock.lock();
        try {
            log.debug("Remove {} {}", name, key);

            Node<K, V> result = map.remove(key);
            if (result != null) {
                dq.remove(result);
                r= result.value;
            }
        } finally {
            lock.unlock();
        }
        return r;
    }

    @Override
    public void removeByKeyWith(FilterKey with) {
        lock.lock();
        try {
            ArrayList<K> toRemove = new ArrayList<>();
            Iterator<Map.Entry<K, Node<K, V>>> iter = map.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<K, Node<K, V>> next = iter.next();
                if (with.test(next.getValue().key)) {
                    toRemove.add(next.getValue().key);
                }
            }

            toRemove.forEach(key -> {
                remove(key);
            });
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void removeByValueWith(FilterValue with) {
        lock.lock();
        try {
            ArrayList<K> toRemove = new ArrayList<>();
            Iterator<Map.Entry<K, Node<K, V>>> iter = map.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<K, Node<K, V>> next = iter.next();
                if (with.test(next.getValue().value)) {
                    toRemove.add(next.getValue().key);
                }
            }

            toRemove.forEach(key -> {
                remove(key);
            });
        } finally {
            lock.unlock();
        }
    }


    public V get(K key) {
        V r = null;
        log.debug("Geting " + name + " " + key + "");
        lock.lock();
        try {
            Node<K, V> result = map.get(key);
            if (result != null) {
                dq.remove(result);
                dq.addFirst(result);
                log.debug("Get " + name + " " + key + "");
                r = result.value;
            } else {
                log.debug("Cache miss " + name + " " + key + "");
            }
        } finally {
            lock.unlock();
        }
        return r;
    }

    @Override
    public Map<K, V> getAll() {
        lock.lock();
        try {
            return dq.stream().collect(Collectors.toMap(k -> k.key, v -> v.value));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<K, V> getByKeyWith(FilterKey with) {
        Map<K, V> r = new HashMap<>();
        lock.lock();
        try {
            Stream<Node<K, V>> f = dq.stream().filter(new Predicate<Node<K, V>>() {
                @Override
                public boolean test(Node<K, V> kvNode) {
                    return with.test(kvNode.key);
                }
            });

            r = f.collect(Collectors.toMap(k -> k.key, v -> v.value));
        } finally {
            lock.unlock();
        }
        return r;
    }

    @Override
    public Map<K, V> getByValueWith(FilterValue with) {
        Map<K, V> r = new HashMap<>();
        lock.lock();
        try {
            Stream<Node<K, V>> f = dq.stream().filter(new Predicate<Node<K, V>>() {
                @Override
                public boolean test(Node<K, V> kvNode) {
                    return with.test(kvNode.value);
                }
            });

            r = f.collect(Collectors.toMap(k -> k.key, v -> v.value));

        } finally {
            lock.unlock();
        }

        return r;
    }


    @Override
    public void putAll(Map<K, V> values) {
        lock.lock();
        try {
            values.forEach(new BiConsumer<K, V>() {
                @Override
                public void accept(K k, V v) {
                    put(k, v);
                }
            });
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getSize() {
        return map.size();
    }

    @Override
    public long getSizeInBytes() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getMaxSize() {
        return SIZE;
    }

    @Override
    public void setMaxSize(long newMaxSize) {
        log.debug("setMaxSize  elements {} size {} newSize {}", dq.size(), SIZE, newMaxSize);
        lock.lock();
        try {
            if (newMaxSize != -1) {
                while (dq.size() > newMaxSize) {
                    Node<K, V> toRemove = dq.removeLast();
                    map.remove(toRemove.key);

                    log.debug("Eviction {}, {}", name, toRemove);
                }
            }

            SIZE = newMaxSize;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void clear() {
        log.warn("Cleaning {} in {} ",dq.size(),name);
        lock.lock();
        try {
            dq.clear();
            map.clear();
        } finally {
            lock.unlock();
        }
        log.warn("Clean {}",name);
    }

    @Override
    public void put(K key, V value) {
        log.debug("Put " + name + " " + key);
        lock.lock();
        try {
            Node<K, V> result = map.get(key);
            if (result != null) {
                result.value = value;
                map.put(key, result);
                dq.remove(result);
                dq.addFirst(result);
                log.debug("Updated frame " + key);
            } else {
                if (SIZE > -1) {
                    if (dq.size() >= SIZE) {
                        Node<K, V> toRemove = dq.removeLast();
                        map.remove(toRemove.key);
                        log.debug("Eviction frame  " + toRemove.key);
                    }
                }
                Node<K, V> newNode = new Node(key, value);
                dq.addFirst(newNode);
                map.put(key, newNode);
                log.debug("Frame added " + key);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "LRUCache{" +
                "dq=" + dq +
                ", map=" + map +
                ", SIZE=" + SIZE +
                ", name='" + name + '\'' +
                "}@" + hashCode();
    }
}


class Node<K, V> {
    K key;
    V value;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Node{" +
                "key=" + key +
                ", value=" + value +
                "}@" + hashCode();
    }
}