package io.github.dhruv1110.jcachex.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for MetricsRegistry class.
 */
class MetricsRegistryTest {

    @Test
    @DisplayName("MetricsRegistry class exists")
    void testMetricsRegistryClassExists() {
        // Just verify the class exists and can be referenced
        assertNotNull(MetricsRegistry.class);
        assertFalse(MetricsRegistry.class.isInterface());
    }
}
