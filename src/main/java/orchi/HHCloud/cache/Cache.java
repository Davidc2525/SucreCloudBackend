package orchi.HHCloud.cache;

import java.util.Map;

/**
 * Interface cache
 */
public interface Cache<K, V> {

    /**
     * Remover un elemento del cache
     *
     * @param key (T) llave de el elemento a eliminar
     */
    public V remove(K key);

    /**
     * Remover elementos que coimsidan con el filtro de llave
     */
    public void removeByKeyWith(FilterKey with);

    /**
     * Remover elementos que coinsidan con el filtro de valor
     */
    public void removeByValueWith(FilterValue with);

    /**
     * Obtener un valor del cache a partir de una lave
     */
    public V get(K key);

    /**
     * Obtener todos los valores del cache
     */
    public Map<K, V> getAll();

    /**
     * Otener todos los valores del cache que coinsidan con el filtro de llave
     */
    public Map<K, V> getByKeyWith(FilterKey with);

    /**
     * Otener todos los valores del cache que coinsidan con el filtro de valor
     */
    public Map<K, V> getByValueWith(FilterValue with);


    /**
     * Agregar o reemplazar (si existe) un valor al cache
     *
     * @param key llave del valor
     * @param value valor
     */
    public void put(K key, V value);

    /**
     * Agregar valores al cache
     */
    public void putAll(Map<K, V> values);

    /**
     * Obtener el numero de elementos en el chache
     */
    public long getSize();


    /**
     * Obtener el tamaño en bytes del cache
     */
    public long getSizeInBytes();

    /**
     * obtener el nombre del cache
     */
    public String getName();

    /**
     * Obtener el tamaño maximo en numero de elementos del cache
     */
    public long getMaxSize();

    /**
     * Setear el tamaño maximo en numero de elementos en el cache
     */
    public void setMaxSize(long newMaxSize);

    public void clear();

}
