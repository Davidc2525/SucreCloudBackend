package orchi.HHCloud.cache;

public interface FilterValue<V> {
    public boolean test(V value);
}
