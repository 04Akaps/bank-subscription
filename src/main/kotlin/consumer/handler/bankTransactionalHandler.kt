package org.example.consumer.handler

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.interfaces.MessageHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BankTransactionalHandler(
    private val logger: Logger = LoggerFactory.getLogger(BankTransactionalHandler::class.java)
) : MessageHandler {

    // 히스토리성 토픽이기 떄문에 mongoDB를 사용할꺼고, 추가적으로 objectId 값의 유효성을 위해 동기처리로 변경
    @Synchronized
    override fun handle(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment?) {

        acknowledgment?.let { it
            try {
                val message = record.value()
                logger.info("Received Order message: {}", message)

                // 메시지 처리 로직 진행

//            it.acknowledge() // commit
                logger.info("Order message processed successfully and acknowledged")
            } catch (e: Exception) {
                logger.error("Error processing Order message", e)
            }
        }?: run {
            logger.info("Acknowledgment was not found")
        }

    }


    override fun handleDLQ(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment?) {
        logger.error("Received DLQ message: {}", record.value())
    }
}