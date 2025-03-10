package org.example.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.protocol.Message
import org.apache.kafka.common.serialization.StringDeserializer
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.consumer.ConsumerFactory
import org.example.consumer.handler.BankTransactionalHandler
import org.example.interfaces.MessageHandler
import org.example.types.dto.KafkaTopicMaps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
@EnableConfigurationProperties(TopicConfig::class)
class KafkaConsumerConfig(
    private val topicConfig: TopicConfig,
    private val bankTransactionalHandler: BankTransactionalHandler,
    private val logger : Logger = LoggerFactory.getLogger(KafkaConsumerConfig::class.java)
) {

    @Bean
    fun consumerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()

        // 브로커 주소 확인 - 정확한 호스트:포트 설정
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = topicConfig.info.bootstrapServers

        // 재시도 설정 추가 (값 조정)
        props[ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG] = "500"  // 재연결 시도 간격 줄이기
        props[ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG] = "5000" // 최대 재연결 간격
        props[ConsumerConfig.RETRY_BACKOFF_MS_CONFIG] = "500" // 재시도 간격

        // 세션 타임아웃과 하트비트 설정 조정
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "30000"
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = "3000"

        // 오프셋 설정
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = topicConfig.info.consumer.autoOffsetReset
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = topicConfig.info.consumer.autoCommit

        // 그룹 ID 설정 - 간단한 이름으로 변경
        props[ConsumerConfig.GROUP_ID_CONFIG] = topicConfig.info.consumer.groupId

        // 직렬화/역직렬화 설정
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java

        // JsonDeserializer 설정
        props[JsonDeserializer.TRUSTED_PACKAGES] = "*"
        props[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = "java.util.Map"

        props[ConsumerConfig.METADATA_MAX_AGE_CONFIG] = "5000"

        return props
    }


    @Bean(name = ["factoryHandlerMapper"])
    fun kafkaListenerContainerFactories(): Map<String, ConcurrentKafkaListenerContainerFactory<String, Any>> {
        val factories = mutableMapOf<String, ConcurrentKafkaListenerContainerFactory<String, Any>>()

        topicConfig.topics.forEach { (topicName, properties) ->
            if (properties.enabled) {
                var handler : MessageHandler

                when(topicName) {
                    "transactions" -> handler = bankTransactionalHandler
                    // 필요한 경우 topic에 대한 핸들러 맵핑
                    else ->{
                        throw  CustomException(ErrorCode.FAILED_TO_FIND_TOPIC_HANDLER, topicName)
                    }
                }

                factories[topicName] = createKafkaListenerContainerFactory(topicName, handler, properties)
            }
        }

        return factories
    }



    private fun createKafkaListenerContainerFactory(
        topicName : String,
        handler : MessageHandler,
        properties: TopicProperties
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val configs = consumerConfigs().toMutableMap()
        configs[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = properties.maxPollRecords

        val consumerFactory = DefaultKafkaConsumerFactory<String, Any>(configs)
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()

        factory.consumerFactory = consumerFactory

        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.setAutoStartup(true)
        factory.containerProperties.pollTimeout = properties.pollInterval

        val container = factory.createContainer(topicName)

        container.setupMessageListener(AcknowledgingMessageListener { record, acknowledgment ->
            try {
               handler.handle(record, acknowledgment)
            } catch (e: Exception) {
                // TODO 만약 메시지에 대해서 에러가 발생하면 어떻게 처리 하면 좋을까??
                // Seeking기능은 해당 토픽을 처리하는 핸들러가 들어오는 이벤트를 추가적으로 핸들링 할 수 없기 떄문에 해당 컨슈머를 통해서 seeking구현은 무리라고 생각
                handler.handleDLQ(record, acknowledgment)
            }
        })

        return factory
    }

}