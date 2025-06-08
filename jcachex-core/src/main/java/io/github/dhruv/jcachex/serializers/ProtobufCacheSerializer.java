package io.github.dhruv.jcachex.serializers;

import io.github.dhruv.jcachex.CacheEntry;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A CacheSerializer that uses Protocol Buffers serialization.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class ProtobufCacheSerializer<K, V> implements CacheSerializer<K, V> {
    @Override
    public void serialize(CacheEntry<V> entry, OutputStream out) {
        // TODO: Implement Protocol Buffers serialization
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CacheEntry<V> deserialize(K key, InputStream in) {
        // TODO: Implement Protocol Buffers deserialization
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getFileExtension() {
        return "proto";
    }
}
