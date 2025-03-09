package org.example.consumer


import org.example.config.TopicConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.interfaces.MessageHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ConsumerFactory(
    private val topicConfig: TopicConfig,
    // Bean 주입시에 스캔을 통해 자동 주입으로 발생하는 잘못된 키 값 맵핑을 방지하기 위해 Bean 이름을 명시한다.
    @Qualifier("topicHandlerMapper") private val handlerMap: Map<String, MessageHandler>,
    @Qualifier("factoryHandlerMapper") private val kafkaListenerContainerFactories: Map<String, ConcurrentKafkaListenerContainerFactory<String, Any>>
) {
    private val logger = LoggerFactory.getLogger(ConsumerFactory::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun setupConsumers() {
        topicConfig.topics.forEach { (topicName, properties) ->
            if (properties.enabled) {

                val handler = handlerMap[topicName]
                val factory = kafkaListenerContainerFactories[topicName]

                if (handler != null && factory != null) {
                    try {
                        // 컨테이너 속성 최적화
                        val containerProperties = factory.containerProperties
                        containerProperties.setPollTimeout(properties.pollInterval)

                        val container = factory.createContainer(topicName)

                        container.setupMessageListener(AcknowledgingMessageListener<String, Any> { record, acknowledgment ->
                            try {
                                logger.debug("Received message from topic: {}, partition: {}, offset: {}",
                                    record.topic(), record.partition(), record.offset())
                                @Suppress("UNCHECKED_CAST")
                                processRecord(record, acknowledgment, handler as MessageHandler)
                            } catch (e: Exception) {
                                logger.error("Error processing record", e)
                                // 메시지 처리 실패 로직 (재시도나 DLQ 전송 등을 구현)
                            }
                        })

                        container.start()
                        logger.info("Started consumer for topic: {} with poll interval: {} ms",
                            topicName, properties.pollInterval)
                    } catch (e: Exception) {
                        logger.error("Failed to start consumer for topic: {}", topicName, e)
                    }

//
//                    val container = factory.createContainer(topicName)
//
//                    container.setupMessageListener(AcknowledgingMessageListener<String, Any> { record, acknowledgment ->
//                        @Suppress("UNCHECKED_CAST")
//                        processRecord(record, acknowledgment, handler as MessageHandler<Any>)
//                    })
//
//                    container.start()
//
//                    logger.info("Started consumer for topic: {} with poll interval: {} ms",
//                        topicName, properties.pollInterval)

                } else {
                    logger.error("Handler not found for topic: {}", topicName)
                }
            }
        }
    }

    private fun  processRecord(
        record: ConsumerRecord<String, Any>,
        acknowledgment: Acknowledgment,
        handler: MessageHandler
    ) {
        try {
            handler.handle(record, acknowledgment)
        } catch (e: Exception) {
            logger.error("Error processing message from topic: {}", record.topic(), e)
        }
    }
}