import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.annotation.JCacheXCacheEvict;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final ExternalApiService externalApiService;

    public UserController(UserService userService, ExternalApiService externalApiService) {
        this.userService = userService;
        this.externalApiService = externalApiService;
    }

    @GetMapping("/users/{id}")
    @JCacheXCacheable(cacheName = "userResponses", key = "#id", expireAfterWrite = 10, expireAfterWriteUnit = TimeUnit.MINUTES)
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.findById(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(user);
    }

    @GetMapping("/users")
    @JCacheXCacheable(cacheName = "userListResponses", key = "#page + '-' + #size + '-' + #sort", expireAfterWrite = 5, expireAfterWriteUnit = TimeUnit.MINUTES)
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        List<User> users = userService.findUsers(page, size, sort);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(users);
    }

    @PostMapping("/users")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/users/{id}")
    @JCacheXCacheEvict(cacheName = "userResponses", key = "#id")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @JCacheXCacheEvict(cacheName = "userResponses", key = "#id")
    @JCacheXCacheEvict(cacheName = "userListResponses")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/profile")
    @JCacheXCacheable(cacheName = "userProfiles", key = "#id + '-' + #includeDetails", expireAfterWrite = 30, expireAfterWriteUnit = TimeUnit.MINUTES)
    public ResponseEntity<UserProfile> getUserProfile(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        UserProfile profile = userService.getUserProfile(id, includeDetails);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(profile);
    }
}

@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @JCacheXCacheable(cacheName = "externalApiResponses", key = "#endpoint + '-' + #params.toString()", expireAfterWrite = 15, expireAfterWriteUnit = TimeUnit.MINUTES)
    public ApiResponse callExternalApi(String endpoint, Map<String, String> params) {
        String url = buildUrl(endpoint, params);

        try {
            return restTemplate.getForObject(url, ApiResponse.class);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to call external API: " + endpoint, e);
        }
    }

    @JCacheXCacheable(cacheName = "weatherData", key = "#city + '-' + #country", expireAfterWrite = 30, expireAfterWriteUnit = TimeUnit.MINUTES)
    public WeatherData getWeatherData(String city, String country) {
        String endpoint = "https://api.weather.com/v1/current";
        Map<String, String> params = Map.of(
                "city", city,
                "country", country,
                "apikey", "your-api-key");

        ApiResponse response = callExternalApi(endpoint, params);
        return parseWeatherData(response);
    }

    private String buildUrl(String endpoint, Map<String, String> params) {
        StringBuilder url = new StringBuilder(endpoint);
        if (!params.isEmpty()) {
            url.append("?");
            url.append(params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")));
        }
        return url.toString();
    }

    private WeatherData parseWeatherData(ApiResponse response) {
        // Parse weather data from API response
        return new WeatherData(
                response.getTemperature(),
                response.getHumidity(),
                response.getDescription());
    }
}
