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
    // Bean 주입시에 스캔을 통해 자동 주입으로 발생하는 잘못된 키 값 맵핑을 방지하기 위해 Bean 이름을 명시한다.
    @Qualifier("factoryHandlerMapper") private val kafkaListenerContainerFactories: Map<String, ConcurrentKafkaListenerContainerFactory<String, Any>>
) {
    private val logger = LoggerFactory.getLogger(ConsumerFactory::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun setupConsumers() {

        kafkaListenerContainerFactories.forEach { (topicName, factory) ->
            try {
                factory.createContainer(topicName).start()
                logger.info("Started consumer for topic: {}", topicName)
            } catch (e: Exception) {
                logger.error("Failed to start consumer for topic: {}", topicName, e)
            }
        }
    }


}