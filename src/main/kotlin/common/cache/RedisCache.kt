package org.example.common.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisClient (
    private val template : RedisTemplate<String, String>,
) {

    fun get(key : String) : String? {
        return template.opsForValue().get(key)
    }

    fun <T> get(
        key : String,
        kSerializer : (Any) -> T?
    ): T? {
        val value = template.opsForValue().get(key)

        value?.let {
            return kSerializer(it)
        } ?: run {
            return null
        }
    }

    fun set(key : String, value : String)  {
        return template.opsForValue().set(key, value)
    }


}