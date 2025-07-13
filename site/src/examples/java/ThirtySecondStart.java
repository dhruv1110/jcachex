import io.github.dhruv1110.jcachex.*;

public class ThirtySecondStart {
    public static void main(String[] args) {
        // Single line cache creation - show this FIRST
        Cache<String, String> cache = JCacheX.create().build();

        // Immediate success - put and get in 3 lines
        cache.put("user1", "Alice");
        String user = cache.get("user1"); // Alice retrieved instantly

        System.out.println("✅ Success! Retrieved user: " + user);

        // That's it! You now have a working cache
        // Ready for production with sensible defaults
    }
}
