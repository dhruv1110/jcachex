import type { CodeTab } from '../types';

// Real-world use case examples
export const REAL_WORLD_EXAMPLES: CodeTab[] = [
    {
        id: 'ecommerce',
        label: 'E-commerce Catalog',
        language: 'java',
        code: `// Multi-layer caching for high-traffic retail
public class ProductCatalog {
    private final Cache<String, Product> productCache;
    private final Cache<String, List<Product>> categoryCache;

    public ProductCatalog() {
        // Products: 1-hour TTL, read-optimized
        productCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(100000L)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

        // Categories: 15-min TTL, more dynamic
        categoryCache = JCacheXBuilder.forApiResponseCaching()
            .name("categories")
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofMinutes(15))
            .build();
    }

    public Product getProduct(String id) {
        return productCache.get(id, this::loadFromDatabase);
    }
}`
    },
    {
        id: 'api-gateway',
        label: 'API Gateway',
        language: 'java',
        code: `// Microservices API Gateway with circuit breaker
@RestController
public class ApiGateway {
    private final Cache<String, ApiResponse> responseCache;

    public ApiGateway() {
        responseCache = JCacheXBuilder.forApiResponseCaching()
            .name("gateway-responses")
            .maximumSize(20000L)
            .expireAfterWrite(Duration.ofSeconds(30))
            .circuitBreaker(CircuitBreakerConfig.default())
            .build();
    }

    @GetMapping("/api/users/{id}")
    public ApiResponse getUser(@PathVariable String id) {
        return responseCache.get("user:" + id,
            () -> userService.fetchUser(id));
    }
}`
    }
];
