package org.example.consumer.handler

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.common.cache.RedisClient
import org.example.common.cache.RedisKeyProvider
import org.example.common.cache.RedisKeyProvider.historyCacheKey
import org.example.common.json.JsonUtil
import org.example.consumer.repository.TransactionHandlerRepository
import org.example.interfaces.MessageHandler
import org.example.types.dto.History
import org.example.types.dto.TransactionsMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BankTransactionalHandler(
    private val logger: Logger = LoggerFactory.getLogger(BankTransactionalHandler::class.java),
    private val transactionHandlerRepository: TransactionHandlerRepository,
    private val redisClient: RedisClient
) : MessageHandler {

    // 히스토리성 토픽이기 떄문에 mongoDB를 사용할꺼고, 추가적으로 objectId 값의 유효성을 위해 동기처리로 변경
    @Synchronized
    override fun handle(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment?) {

        acknowledgment?.let { it
            try {
                val rawMessage = record.value()
                logger.info("Received Order message: {}", rawMessage)

                try {
                    val messageStr = when (rawMessage) {
                        is String -> rawMessage
                        else -> rawMessage.toString()
                    }

                    val eventData = JsonUtil.decodeFromJson(messageStr, TransactionsMessage.serializer())
                    val document = eventData.toEntity()

                    transactionHandlerRepository.saveTransactionHistory(document)

                    val cacheKey = historyCacheKey(eventData.fromUlid, eventData.fromAccountID)
                    val cachedValue = redisClient.get(cacheKey)

                    if (cachedValue != null) {
                        val cachedData : List<History> = JsonUtil.decodeFromJson(cachedValue, ListSerializer(History.serializer()))

                        val updatedList = buildList {
                            add(eventData.toCacheDTO())

                            if (cachedData.isNotEmpty()) {
                                addAll(cachedData.dropLast(1))
                            }
                        }


                        redisClient.set(cacheKey, JsonUtil.encodeToJson(updatedList, ListSerializer(History.serializer())))
                    }

                    it.acknowledge()

                    logger.info("message processed successfully and acknowledged")
                } catch (e: SerializationException) {
                    logger.warn("Failed to deserialize message to TransactionsMessage: {}", e.message)
                } finally {
                }

            } catch (e: Exception) {
                logger.error("Error processing message", e)
            }
        }?: run {
            logger.info("Acknowledgment was not found")
        }

    }


    override fun handleDLQ(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment?) {
        logger.error("Received DLQ message: {}", record.value())
    }
}