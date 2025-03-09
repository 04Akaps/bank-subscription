package org.example.interfaces

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.support.Acknowledgment

interface MessageHandler {
    fun handle(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment)
}