package org.example.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import com.mongodb.*
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode

import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import kotlin.collections.HashMap

enum class MongoTableCollector(
    val table: String
) {
    Bank("bank"),;
}

@Configuration
@EnableMongoAuditing
class MongoConfig(
    @Value("\${database.mongo.uri}") val url: String,
) {
    @Bean
    fun template(): HashMap<String, MongoTemplate> {
        val mapper = HashMap<String, MongoTemplate>(MongoTableCollector.entries.size)

        for (c in MongoTableCollector.entries) {
            val settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD) // BSON 변화 표준 -> 기본을 따른다.
                .applyConnectionString(ConnectionString(url)) // 접속 uri
                .readPreference(ReadPreference.primary()) // 모든 읽기 작업이 primary 노드에서 수행이 된다. -> 일관된 읽기를 보장
                .build()
            try {
                val mongoClient = MongoClients.create(settings)
                mapper[c.table] = MongoTemplate(
                    SimpleMongoClientDatabaseFactory(mongoClient, c.table)
                )
            } catch (e: Exception) {
                throw  CustomException(ErrorCode.FAILED_TO_CREATE_MONGO_CONFIG)
            }
        }

        return mapper
    }
}