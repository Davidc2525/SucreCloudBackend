package orchi.HHCloud.cache;

public interface FilterKey<K> {
    public boolean test(K key);
}
