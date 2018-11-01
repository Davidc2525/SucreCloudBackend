package orchi.HHCloud.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CacheFactory {
    private static Map<Object, Cache> caches = new HashMap<Object, Cache>();
    private static Logger log = LoggerFactory.getLogger(CacheFactory.class);

    /**
     * Creator MapCache
     */
    public static synchronized <T extends Cache> T createMapCache(String name) throws CacheAleardyExistException {
        log.debug("create cache SIMPLE {}", name);
        T cache = null;
        if (caches.containsKey(name)) {
            log.debug("-- get cache {}", name);
            throw new CacheAleardyExistException("cache with name aleardy exist " + name);
        } else {
            log.debug("-- create cache {}", name);
            cache = (T) new MapCache<>(name);
            caches.put(name, cache);
        }
        return cache;
    }

    /**
     * Creator LRUCache
     */
    public static synchronized <T extends Cache> T createLRUCache(String name) throws CacheAleardyExistException {
        log.debug("create cache LRU {}", name);
        T cache = null;
        if (caches.containsKey(name)) {
            log.debug("-- get cache {}", name);
            throw new CacheAleardyExistException("cache with name aleardy exist " + name);
        } else {
            log.debug("-- create cache {}", name);
            cache = (T) new LRUCache<>(name);
            caches.put(name, cache);
        }
        return cache;
    }


    /**
     * get Cache
     */
    public static synchronized <T extends Cache> T get(String name) throws CacheDontExistException {
        log.debug("get cache {}", name);
        T cache = null;
        if (caches.containsKey(name)) {
            log.debug("-- get cache {}", name);
            cache = (T) caches.get(name);
        } else {
            log.warn("-- cache no exist {}", name);
            throw new CacheDontExistException("-- cache no exist " + name);
        }
        return cache;
    }


}
