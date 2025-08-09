package io.github.dhruv1110.jcachex.example.springboot.model

data class User(
    val id: String,
    val name: String,
    val email: String
)

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
) {
    fun toJson(): String =
        """{"userId":"$userId","displayName":"$displayName","email":"$email","preferences":${preferences}}"""

    companion object {
        fun fromJson(json: String): UserProfile {
            return UserProfile("demo", "Demo User", "demo@example.com")
        }
    }
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String
)

data class UserSession(
    val sessionId: String,
    val userId: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

data class AnalyticsData(
    val metric: String,
    val value: Double,
    val timestamp: Long
)


