package org.example.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "kafka")
data class TopicConfig(
    var topics: Map<String, TopicProperties> = emptyMap(),
    var info: KafkaInfo = KafkaInfo()
)

data class TopicProperties(
    var pollInterval: Long = 5000, // 폴링 주기 (ms)
    var concurrency: Int = 1, // 동시 처리 스레드 수
    var maxPollRecords: Int = 500, // 한 번에 가져올 최대 레코드 수
    var enabled: Boolean = true // 활성화 여부
)

data class KafkaInfo(
    var bootstrapServers: String = "localhost:9092",
    var consumer: ConsumerConfig = ConsumerConfig()
)

data class ConsumerConfig(
    var groupId: String = "my-consumer-group",
    var autoOffsetReset: String = "earliest",
    var autoCommit: Boolean = false
)