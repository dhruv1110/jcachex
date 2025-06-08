package io.github.dhruv.jcachex.serializers;

import io.github.dhruv.jcachex.CacheEntry;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for serializing cache entries to persistent storage.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public interface CacheSerializer<K, V> {
    /**
     * Serializes a cache entry to an output stream.
     *
     * @param entry the entry to serialize
     * @param out   the output stream to write to
     */
    void serialize(CacheEntry<V> entry, OutputStream out);

    /**
     * Deserializes a cache entry from an input stream.
     *
     * @param key the key of the entry
     * @param in  the input stream to read from
     * @return the deserialized entry
     */
    CacheEntry<V> deserialize(K key, InputStream in);

    /**
     * Returns the file extension to use for serialized entries.
     */
    String getFileExtension();

    /**
     * Creates a new CacheSerializer that uses Java serialization.
     */
    static <K, V> CacheSerializer<K, V> java() {
        return new JavaCacheSerializer<>();
    }

    /**
     * Creates a new CacheSerializer that uses JSON serialization.
     */
    static <K, V> CacheSerializer<K, V> json() {
        return new JsonCacheSerializer<>();
    }

    /**
     * Creates a new CacheSerializer that uses Protocol Buffers serialization.
     */
    static <K, V> CacheSerializer<K, V> protobuf() {
        return new ProtobufCacheSerializer<>();
    }
}
