package org.example.types.dto

import org.example.interfaces.MessageHandler
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory

data class KafkaTopicMaps(
    val handlerMap: Map<String, MessageHandler>,
    val factoryMap: Map<String, ConcurrentKafkaListenerContainerFactory<String, Any>>
)