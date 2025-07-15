import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Real-world E-commerce Product Catalog Service
 * Demonstrates multi-layer caching strategy for high-traffic retail
 * applications
 */
public class EcommerceProductCatalog {
    private static final Logger log = Logger.getLogger(EcommerceProductCatalog.class.getName());

    // Simulated repositories (in real app, these would be Spring Data JPA
    // repositories)
    private static final ProductRepository productRepository = new ProductRepository();
    private static final CategoryRepository categoryRepository = new CategoryRepository();

    private final Cache<String, Product> productCache;
    private final Cache<String, List<Product>> categoryCache;
    private final Cache<String, ProductAnalytics> analyticsCache;

    public EcommerceProductCatalog() {
        // High-read product cache with 1-hour TTL
        // Products change infrequently but are accessed constantly
        this.productCache = JCacheXBuilder.forReadHeavyWorkload()
                .name("products")
                .maximumSize(100000L) // Can hold 100k products
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofHours(2)) // Keep popular items longer
                .recordStats(true)
                .evictionListener(this::onProductEviction)
                .build();

        // Category listings with shorter TTL (more dynamic)
        // Categories change more frequently due to inventory updates
        this.categoryCache = JCacheXBuilder.forApiResponseCaching()
                .name("categories")
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats(true)
                .build();

        // Analytics cache for real-time metrics
        this.analyticsCache = JCacheXBuilder.forWriteHeavyWorkload()
                .name("analytics")
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    /**
     * Get product with intelligent caching
     * First checks cache, then loads from database if needed
     */
    public Product getProduct(String productId) {
        return productCache.get(productId, this::loadProductFromDB);
    }

    /**
     * Get products by category with cache-aside pattern
     */
    public List<Product> getProductsByCategory(String categoryId) {
        String cacheKey = "category:" + categoryId;

        return categoryCache.get(cacheKey, () -> {
            log.info("Loading category {} from database", categoryId);

            // Simulate database call
            List<Product> products = categoryRepository.findProductsByCategory(categoryId);

            // Pre-populate individual product cache
            for (Product product : products) {
                productCache.put(product.getId(), product);
            }

            return products;
        });
    }

    /**
     * Update product with cache invalidation
     */
    public void updateProduct(Product product) {
        // Update database
        productRepository.save(product);

        // Update cache
        productCache.put(product.getId(), product);

        // Invalidate related category caches
        invalidateCategoryCache(product.getCategoryId());

        log.info("Updated product {} and invalidated related caches", product.getId());
    }

    /**
     * Delete product with proper cache cleanup
     */
    public void deleteProduct(String productId) {
        Product product = productCache.getIfPresent(productId);

        // Delete from database
        productRepository.deleteById(productId);

        // Remove from cache
        productCache.invalidate(productId);

        // Invalidate category cache if we know the category
        if (product != null) {
            invalidateCategoryCache(product.getCategoryId());
        }

        log.info("Deleted product {} and cleaned up caches", productId);
    }

    /**
     * Get real-time analytics with high-frequency updates
     */
    public ProductAnalytics getProductAnalytics(String productId) {
        return analyticsCache.get(productId, () -> {
            // In real app, this would aggregate from analytics service
            return new ProductAnalytics(productId, 0, 0, 0.0);
        });
    }

    /**
     * Track product view for analytics
     */
    public void trackProductView(String productId) {
        // Update analytics asynchronously to avoid blocking user experience
        CompletableFuture.runAsync(() -> {
            ProductAnalytics analytics = getProductAnalytics(productId);
            analytics.incrementViews();
            analyticsCache.put(productId, analytics);
        });
    }

    /**
     * Get cache statistics for monitoring
     */
    public void printCacheStats() {
        System.out.println("📊 E-commerce Cache Statistics");
        System.out.println("===============================");

        CacheStats productStats = productCache.stats();
        System.out.printf("Product Cache - Size: %d, Hit Rate: %.2f%%, Evictions: %d%n",
                productCache.size(), productStats.hitRate() * 100, productStats.evictionCount());

        CacheStats categoryStats = categoryCache.stats();
        System.out.printf("Category Cache - Size: %d, Hit Rate: %.2f%%, Evictions: %d%n",
                categoryCache.size(), categoryStats.hitRate() * 100, categoryStats.evictionCount());

        CacheStats analyticsStats = analyticsCache.stats();
        System.out.printf("Analytics Cache - Size: %d, Hit Rate: %.2f%%, Evictions: %d%n",
                analyticsCache.size(), analyticsStats.hitRate() * 100, analyticsStats.evictionCount());
    }

    // Private helper methods
    private Product loadProductFromDB(String productId) {
        log.info("Loading product {} from database", productId);
        return productRepository.findById(productId);
    }

    private void invalidateCategoryCache(String categoryId) {
        categoryCache.invalidate("category:" + categoryId);
    }

    private void onProductEviction(String key, Product product, RemovalCause cause) {
        log.info("Product {} evicted due to {}", key, cause);

        // In production, you might:
        // 1. Log to metrics system
        // 2. Pre-load related products
        // 3. Send alerts for unexpected evictions
    }

    // Demo method
    public static void main(String[] args) {
        EcommerceProductCatalog catalog = new EcommerceProductCatalog();

        System.out.println("🛍️  E-commerce Product Catalog Demo");
        System.out.println("====================================");

        // Simulate typical e-commerce traffic patterns

        // 1. Browse products (cache misses initially)
        System.out.println("\n1. Initial product browsing:");
        Product product1 = catalog.getProduct("PROD-001");
        Product product2 = catalog.getProduct("PROD-002");
        System.out.println("   Loaded: " + product1.getName() + " and " + product2.getName());

        // 2. Browse same products again (cache hits)
        System.out.println("\n2. Browsing same products (cache hits):");
        catalog.getProduct("PROD-001");
        catalog.getProduct("PROD-002");
        System.out.println("   Products retrieved from cache");

        // 3. Browse by category
        System.out.println("\n3. Category browsing:");
        List<Product> electronics = catalog.getProductsByCategory("electronics");
        System.out.println("   Found " + electronics.size() + " electronics products");

        // 4. Track analytics
        System.out.println("\n4. Analytics tracking:");
        catalog.trackProductView("PROD-001");
        catalog.trackProductView("PROD-001");
        catalog.trackProductView("PROD-002");

        // 5. Update product (cache invalidation)
        System.out.println("\n5. Product update:");
        product1.setPrice(89.99);
        catalog.updateProduct(product1);

        // 6. Show cache statistics
        System.out.println("\n6. Cache performance:");
        catalog.printCacheStats();

        System.out.println("\n✅ Demo completed successfully!");
    }
}

// Supporting classes (in real app, these would be in separate files)
class Product {
    private String id;
    private String name;
    private String categoryId;
    private double price;
    private int inventory;

    public Product(String id, String name, String categoryId, double price, int inventory) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.inventory = inventory;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }
}

class ProductAnalytics {
    private String productId;
    private int views;
    private int purchases;
    private double conversionRate;

    public ProductAnalytics(String productId, int views, int purchases, double conversionRate) {
        this.productId = productId;
        this.views = views;
        this.purchases = purchases;
        this.conversionRate = conversionRate;
    }

    public void incrementViews() {
        this.views++;
        updateConversionRate();
    }

    public void incrementPurchases() {
        this.purchases++;
        updateConversionRate();
    }

    private void updateConversionRate() {
        this.conversionRate = views > 0 ? (double) purchases / views : 0.0;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public int getViews() {
        return views;
    }

    public int getPurchases() {
        return purchases;
    }

    public double getConversionRate() {
        return conversionRate;
    }
}

// Simulated repository classes
class ProductRepository {
    public Product findById(String id) {
        // Simulate database lookup
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }
        return new Product(id, "Product " + id, "electronics", 99.99, 100);
    }

    public void save(Product product) {
        // Simulate database save
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
        }
    }

    public void deleteById(String id) {
        // Simulate database delete
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
        }
    }
}

class CategoryRepository {
    public List<Product> findProductsByCategory(String categoryId) {
        // Simulate database query
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
        }
        return Arrays.asList(
                new Product("PROD-001", "Smartphone", categoryId, 699.99, 50),
                new Product("PROD-002", "Laptop", categoryId, 999.99, 25),
                new Product("PROD-003", "Headphones", categoryId, 199.99, 100));
    }
}
