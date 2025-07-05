package io.github.dhruv1110.jcachex.security;

import io.github.dhruv1110.jcachex.exceptions.CacheOperationException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Security validation utility for cache operations.
 * <p>
 * This class provides comprehensive security validation to prevent:
 * </p>
 * <ul>
 * <li>Key injection attacks</li>
 * <li>Value serialization vulnerabilities</li>
 * <li>Resource exhaustion attacks</li>
 * <li>Unauthorized cache access</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Configure security validator
 * CacheSecurityValidator validator = CacheSecurityValidator.builder()
 *         .maxKeyLength(256)
 *         .maxValueSize(1024 * 1024) // 1MB
 *         .allowedKeyPattern("^[a-zA-Z0-9_-]+$")
 *         .blacklistKeywords("script", "eval", "function")
 *         .build();
 *
 * // Validate cache operations
 * try {
 *     validator.validateKey(key);
 *     validator.validateValue(value);
 *     cache.put(key, value);
 * } catch (CacheOperationException e) {
 *     // Handle security violation
 *     logger.warn("Security validation failed: " + e.getMessage());
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheSecurityValidator {

    private final int maxKeyLength;
    private final long maxValueSize;
    private final Pattern allowedKeyPattern;
    private final Set<String> blacklistedKeywords;
    private final Predicate<Object> keyValidator;
    private final Predicate<Object> valueValidator;
    private final boolean enableSizeValidation;
    private final boolean enablePatternValidation;
    private final boolean enableBlacklistValidation;

    private CacheSecurityValidator(Builder builder) {
        this.maxKeyLength = builder.maxKeyLength;
        this.maxValueSize = builder.maxValueSize;
        this.allowedKeyPattern = builder.allowedKeyPattern;
        ConcurrentHashMap<String, Boolean> tempMap = new ConcurrentHashMap<>();
        for (String keyword : builder.blacklistedKeywords) {
            tempMap.put(keyword, Boolean.TRUE);
        }
        this.blacklistedKeywords = tempMap.keySet();
        this.keyValidator = builder.keyValidator;
        this.valueValidator = builder.valueValidator;
        this.enableSizeValidation = builder.enableSizeValidation;
        this.enablePatternValidation = builder.enablePatternValidation;
        this.enableBlacklistValidation = builder.enableBlacklistValidation;
    }

    /**
     * Creates a new security validator builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default security validator with reasonable defaults.
     *
     * @return a default security validator
     */
    public static CacheSecurityValidator defaultValidator() {
        return builder()
                .maxKeyLength(256)
                .maxValueSize(10 * 1024 * 1024) // 10MB
                .allowedKeyPattern("^[a-zA-Z0-9_\\-\\.:/]+$")
                .blacklistKeywords("javascript:", "data:", "eval", "script")
                .build();
    }

    /**
     * Validates a cache key for security compliance.
     *
     * @param key the key to validate
     * @throws CacheOperationException if validation fails
     */
    public void validateKey(Object key) {
        if (key == null) {
            throw CacheOperationException.invalidKey(key);
        }

        String keyString = key.toString();

        // Size validation
        if (enableSizeValidation && keyString.length() > maxKeyLength) {
            throw new CacheOperationException(
                    "Key length (" + keyString.length() + ") exceeds maximum allowed (" + maxKeyLength + ")",
                    "KEY_TOO_LONG",
                    false);
        }

        // Pattern validation
        if (enablePatternValidation && allowedKeyPattern != null) {
            if (!allowedKeyPattern.matcher(keyString).matches()) {
                throw new CacheOperationException(
                        "Key '" + sanitizeForLogging(keyString) + "' does not match allowed pattern",
                        "INVALID_KEY_PATTERN",
                        false);
            }
        }

        // Blacklist validation
        if (enableBlacklistValidation) {
            String lowerKey = keyString.toLowerCase();
            for (String keyword : blacklistedKeywords) {
                if (lowerKey.contains(keyword.toLowerCase())) {
                    throw new CacheOperationException(
                            "Key contains blacklisted keyword: " + keyword,
                            "BLACKLISTED_KEY",
                            false);
                }
            }
        }

        // Custom validator
        if (keyValidator != null && !keyValidator.test(key)) {
            throw new CacheOperationException(
                    "Key failed custom validation: " + sanitizeForLogging(keyString),
                    "CUSTOM_KEY_VALIDATION_FAILED",
                    false);
        }
    }

    /**
     * Validates a cache value for security compliance.
     *
     * @param value the value to validate
     * @throws CacheOperationException if validation fails
     */
    public void validateValue(Object value) {
        if (value == null) {
            // Null values are generally allowed
            return;
        }

        // Size validation (approximate)
        if (enableSizeValidation) {
            long approximateSize = estimateObjectSize(value);
            if (approximateSize > maxValueSize) {
                throw new CacheOperationException(
                        "Value size (" + approximateSize + " bytes) exceeds maximum allowed (" + maxValueSize + ")",
                        "VALUE_TOO_LARGE",
                        false);
            }
        }

        // Check for potentially dangerous types
        if (isDangerousType(value)) {
            throw new CacheOperationException(
                    "Value type '" + value.getClass().getName() + "' is not allowed for security reasons",
                    "DANGEROUS_VALUE_TYPE",
                    false);
        }

        // String-specific validations
        if (value instanceof String) {
            validateStringValue((String) value);
        }

        // Custom validator
        if (valueValidator != null && !valueValidator.test(value)) {
            throw new CacheOperationException(
                    "Value failed custom validation",
                    "CUSTOM_VALUE_VALIDATION_FAILED",
                    false);
        }
    }

    /**
     * Validates both key and value in a single call.
     *
     * @param key   the key to validate
     * @param value the value to validate
     * @throws CacheOperationException if validation fails
     */
    public void validateOperation(Object key, Object value) {
        validateKey(key);
        validateValue(value);
    }

    /**
     * Sanitizes a string for safe logging (removes potential sensitive data).
     *
     * @param input the input string
     * @return a sanitized version safe for logging
     */
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }

        // Truncate long strings
        if (input.length() > 100) {
            input = input.substring(0, 97) + "...";
        }

        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "*");
    }

    private void validateStringValue(String value) {
        // Check for script injection patterns
        String lowerValue = value.toLowerCase();
        String[] dangerousPatterns = {
                "<script", "javascript:", "data:", "vbscript:", "onload=", "onerror=", "eval("
        };

        for (String pattern : dangerousPatterns) {
            if (lowerValue.contains(pattern)) {
                throw new CacheOperationException(
                        "Value contains potentially dangerous pattern: " + pattern,
                        "DANGEROUS_VALUE_PATTERN",
                        false);
            }
        }

        // Check for SQL injection patterns (if value might be used in queries)
        String[] sqlPatterns = {
                "' or '1'='1", "'; drop table", "union select", "-- ", "/*", "*/"
        };

        for (String pattern : sqlPatterns) {
            if (lowerValue.contains(pattern)) {
                throw new CacheOperationException(
                        "Value contains potentially dangerous SQL pattern",
                        "DANGEROUS_SQL_PATTERN",
                        false);
            }
        }
    }

    private boolean isDangerousType(Object value) {
        // Check for types that could be dangerous to serialize/deserialize
        Class<?> clazz = value.getClass();

        // Dangerous classes (this is a basic list - extend as needed)
        String[] dangerousClasses = {
                "java.lang.Runtime",
                "java.lang.Process",
                "java.lang.ProcessBuilder",
                "java.io.ObjectInputStream",
                "java.net.URL",
                "javax.script.ScriptEngine"
        };

        String className = clazz.getName();
        for (String dangerousClass : dangerousClasses) {
            if (className.equals(dangerousClass) || className.startsWith(dangerousClass + "$")) {
                return true;
            }
        }

        return false;
    }

    private long estimateObjectSize(Object value) {
        if (value instanceof String) {
            return ((String) value).length() * 2; // Approximate Unicode size
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        } else if (value instanceof Number) {
            return 8; // Most numbers are <= 8 bytes
        } else if (value instanceof Boolean) {
            return 1;
        } else {
            // For other objects, use toString length as rough estimate
            return value.toString().length() * 2;
        }
    }

    /**
     * Builder for creating security validators.
     */
    public static class Builder {
        private int maxKeyLength = 256;
        private long maxValueSize = 10 * 1024 * 1024; // 10MB
        private Pattern allowedKeyPattern = null;
        private Set<String> blacklistedKeywords = ConcurrentHashMap.newKeySet();
        private Predicate<Object> keyValidator = null;
        private Predicate<Object> valueValidator = null;
        private boolean enableSizeValidation = true;
        private boolean enablePatternValidation = false;
        private boolean enableBlacklistValidation = false;

        /**
         * Sets the maximum allowed key length.
         *
         * @param maxKeyLength the maximum key length
         * @return this builder
         */
        public Builder maxKeyLength(int maxKeyLength) {
            if (maxKeyLength <= 0) {
                throw new IllegalArgumentException("Max key length must be positive");
            }
            this.maxKeyLength = maxKeyLength;
            return this;
        }

        /**
         * Sets the maximum allowed value size in bytes.
         *
         * @param maxValueSize the maximum value size
         * @return this builder
         */
        public Builder maxValueSize(long maxValueSize) {
            if (maxValueSize <= 0) {
                throw new IllegalArgumentException("Max value size must be positive");
            }
            this.maxValueSize = maxValueSize;
            return this;
        }

        /**
         * Sets the allowed key pattern (regex).
         *
         * @param pattern the allowed pattern
         * @return this builder
         */
        public Builder allowedKeyPattern(String pattern) {
            this.allowedKeyPattern = Pattern.compile(pattern);
            this.enablePatternValidation = true;
            return this;
        }

        /**
         * Adds blacklisted keywords for keys and values.
         *
         * @param keywords the keywords to blacklist
         * @return this builder
         */
        public Builder blacklistKeywords(String... keywords) {
            for (String keyword : keywords) {
                this.blacklistedKeywords.add(keyword);
            }
            this.enableBlacklistValidation = true;
            return this;
        }

        /**
         * Sets a custom key validator.
         *
         * @param validator the key validator predicate
         * @return this builder
         */
        public Builder keyValidator(Predicate<Object> validator) {
            this.keyValidator = validator;
            return this;
        }

        /**
         * Sets a custom value validator.
         *
         * @param validator the value validator predicate
         * @return this builder
         */
        public Builder valueValidator(Predicate<Object> validator) {
            this.valueValidator = validator;
            return this;
        }

        /**
         * Enables or disables size validation.
         *
         * @param enabled whether to enable size validation
         * @return this builder
         */
        public Builder enableSizeValidation(boolean enabled) {
            this.enableSizeValidation = enabled;
            return this;
        }

        /**
         * Builds the security validator.
         *
         * @return a new security validator
         */
        public CacheSecurityValidator build() {
            return new CacheSecurityValidator(this);
        }
    }
}
