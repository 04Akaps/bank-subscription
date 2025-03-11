package org.example.common.cache

object RedisKeyProvider {
    private const val HISTORY_CACHE_KEY = "history"

    fun historyCacheKey(ulid: String, accountUlid: String): String {
        return "$HISTORY_CACHE_KEY:$ulid:$accountUlid"
    }
}