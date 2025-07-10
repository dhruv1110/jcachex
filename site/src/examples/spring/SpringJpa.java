import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private boolean active;

    // Constructors, getters, setters
    public User() {
    }

    public User(String id, String name, String email, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @JCacheXCacheable(cacheName = "usersByEmail", expireAfterWrite = 30, expireAfterWriteUnit = TimeUnit.MINUTES)
    Optional<User> findByEmail(String email);

    @JCacheXCacheable(cacheName = "activeUsers", expireAfterWrite = 15, expireAfterWriteUnit = TimeUnit.MINUTES)
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    @JCacheXCacheable(cacheName = "usersByName", key = "#name", expireAfterWrite = 20, expireAfterWriteUnit = TimeUnit.MINUTES)
    List<User> findByNameContaining(String name);
}

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @JCacheXCacheable(cacheName = "users", expireAfterWrite = 60, expireAfterWriteUnit = TimeUnit.MINUTES, maximumSize = 10000)
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @JCacheXCacheable(cacheName = "userProfiles", key = "#userId", condition = "#includeDetails == true", expireAfterWrite = 45, expireAfterWriteUnit = TimeUnit.MINUTES)
    public UserProfile getUserProfile(String userId, boolean includeDetails) {
        User user = findById(userId);

        if (includeDetails) {
            return buildDetailedProfile(user);
        } else {
            return buildBasicProfile(user);
        }
    }

    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail", key = "#user.email")
    @JCacheXCacheEvict(cacheName = "activeUsers", condition = "#user.active")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @JCacheXCacheEvict(cacheName = "users")
    @JCacheXCacheEvict(cacheName = "usersByEmail")
    @JCacheXCacheEvict(cacheName = "activeUsers")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    // Bulk operations with cache management
    @JCacheXCacheEvict(cacheName = "activeUsers")
    public void activateUsers(List<String> userIds) {
        userIds.forEach(id -> {
            User user = findById(id);
            user.setActive(true);
            userRepository.save(user);
        });
    }

    private UserProfile buildDetailedProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                loadUserPermissions(user.getId()),
                loadUserPreferences(user.getId()));
    }

    private UserProfile buildBasicProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                null,
                null);
    }

    private List<String> loadUserPermissions(String userId) {
        // Load user permissions from database
        return List.of("READ", "WRITE");
    }

    private Map<String, String> loadUserPreferences(String userId) {
        // Load user preferences from database
        return Map.of("theme", "dark", "language", "en");
    }
}
