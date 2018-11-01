package orchi.HHCloud.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementacion simple de cache en memoria HashMap
 */
public class MapCache<K, V> implements Cache<K, V> {//TODO control de concurrencia con Lock

    private Logger log = LoggerFactory.getLogger(MapCache.class);
    private HashMap<K, V> cache = new HashMap<K, V>();
    private String name;
    private long maxSize = -1;

    public MapCache(String name) {
        log.debug("- Nuevo simple Cache {}", name);
        this.name = name;
    }

    @Override
    public V remove(K key) {
        return cache.remove(key);
    }

    @Override
    public void removeByKeyWith(FilterKey with) {
        ArrayList<K> toRemove = new ArrayList<>();
        Iterator<Map.Entry<K, V>> iter = cache.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<K, V> next = iter.next();
            if (with.test(next.getKey())) {
                toRemove.add(next.getKey());
            }
        }

        toRemove.forEach(key -> {
            remove(key);
        });
    }

    @Override
    public void removeByValueWith(FilterValue with) {
        ArrayList<K> toRemove = new ArrayList<>();
        Iterator<Map.Entry<K, V>> iter = cache.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<K, V> next = iter.next();
            if (with.test(next.getValue())) {
                toRemove.add(next.getKey());
            }
        }

        toRemove.forEach(key -> {
            remove(key);
        });
    }


    @Override
    public V get(K key) {
        log.debug(" - obtener valor por {}", key.toString());
        return cache.get(key);
    }

    @Override
    public Map<K, V> getAll() {
        log.debug(" - obtener todo");
        return cache;
    }

    @Override
    public Map<K, V> getByKeyWith(FilterKey with) {
        Stream<Map.Entry<K, V>> f = cache.entrySet().stream().filter(new Predicate<Map.Entry<K, V>>() {
            @Override
            public boolean test(Map.Entry<K, V> kvEntry) {
                return with.test(kvEntry.getKey());
            }
        });

        return f.collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

    }

    @Override
    public Map<K, V> getByValueWith(FilterValue with) {
        Stream<Map.Entry<K, V>> f = cache.entrySet().stream().filter(new Predicate<Map.Entry<K, V>>() {
            @Override
            public boolean test(Map.Entry<K, V> kvEntry) {
                return with.test(kvEntry.getValue());
            }
        });

        return f.collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
    }


    @Override
    public void put(K key, V value) {
        log.debug(" - setear valor {} {}", key.toString(), value.toString());
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> values) {
        log.debug(" - setear valores {} ", values.toString());
        cache.putAll(values);
    }

    @Override
    public long getSize() {
        long size = cache.size();
        log.debug(" - obtener cantidad de elementos en cache {}", size);
        return size;
    }

    @Override
    public long getSizeInBytes() {
        log.debug(" - obtener tamaño de cache");
        return 0;
    }

    @Override
    public String getName() {
        log.debug(" - obtener nombre de cache {}", name);
        return this.name;
    }

    @Override
    public long getMaxSize() {
        log.debug(" - obtener tamaño maximo de cache {}", maxSize);
        return maxSize;
    }

    @Override
    public void setMaxSize(long newMaxSize) {

    }

    @Override
    public void clear() {
        cache.clear();
    }
}
