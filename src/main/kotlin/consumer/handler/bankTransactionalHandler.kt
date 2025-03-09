package org.example.consumer.handler

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.interfaces.MessageHandler
import org.example.types.dto.TransactionsMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BankTransactionalHandler : MessageHandler {
    private val logger = LoggerFactory.getLogger(BankTransactionalHandler::class.java)

    override fun handle(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment) {
        try {
            val message = record.value()
            logger.info("Received Order message: {}", message)

            // 메시지 처리 진행

//            acknowledgment.acknowledge() // commit
            logger.info("Order message processed successfully and acknowledged")
        } catch (e: Exception) {
            logger.error("Error processing Order message", e)
        }
    }
}